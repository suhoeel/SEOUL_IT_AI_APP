package com.portfolio.school;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.portfolio.school.adapter.GeminiAdapter;
import com.portfolio.school.db.ChatDatabase;
import com.portfolio.school.db.DataResponseDao;
import com.portfolio.school.model.DataResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText editText;
    private FloatingActionButton button;
    private ImageView imageView;
    private RecyclerView recyclerView;

    private Bitmap bitmap = null;
    private Uri imageUri = null; // 이제 영구적인 내부 파일 경로를 가리키게 됩니다.

    private GeminiAdapter adapter;
    private DataResponseDao chatDao;

    // 모든 백그라운드 작업을 관리하는 전용 스레드 풀
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    // 선택된 이미지(임시 URI)를 백그라운드에서 처리
                    backgroundExecutor.execute(() -> {
                        try {
                            // 1. UI에 표시할 Bitmap 로드
                            final Bitmap loadedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

                            // 2. 이미지를 앱 내부 저장소에 복사하고 영구 경로를 얻음
                            String internalPath = saveImageToInternalStorage(uri);

                            // 3. 메인 스레드에서 UI 업데이트 및 멤버 변수 설정
                            runOnUiThread(() -> {
                                bitmap = loadedBitmap;
                                imageUri = Uri.fromFile(new File(internalPath)); // 영구 경로로 URI 설정
                                imageView.setImageTintList(null);
                                imageView.setImageBitmap(bitmap);
                            });

                        } catch (IOException e) {
                            Log.e("PhotoPicker", "Error processing selected image", e);
                        }
                    });
                } else {
                    Log.d("PhotoPicker", "No media selected");
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Insets 처리
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 뷰 초기화
        editText = findViewById(R.id.ask_edit_text);
        button = findViewById(R.id.ask_button);
        imageView = findViewById(R.id.select_iv);
        recyclerView = findViewById(R.id.recycler_view_id);

        // 1. DB 및 DAO 초기화
        chatDao = ChatDatabase.getDatabase(this).chatDao();

        // 2. 어댑터 초기화 (빈 리스트로 시작)
        adapter = new GeminiAdapter(this);
        recyclerView.setAdapter(adapter);

        // 3. 이전 대화 기록을 백그라운드에서 불러오기
        loadChatHistory();

        // 리스너 설정
        imageView.setOnClickListener(v ->
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));

        button.setOnClickListener(v -> {
            String prompt = editText.getText().toString().trim();

            if (!prompt.isEmpty() || bitmap != null) {
                String finalPrompt = (prompt.isEmpty() && bitmap != null) ? "이 이미지에 대해 설명해줘." : prompt;
                String imagePath = (imageUri != null) ? imageUri.getPath() : "";

                DataResponse userRequest;
                if (bitmap != null) {
                    userRequest = new DataResponse(0, finalPrompt, bitmap, imagePath, System.currentTimeMillis());
                } else {
                    userRequest = new DataResponse(0, finalPrompt, imagePath, System.currentTimeMillis());
                }

                addResponseToUiAndDb(userRequest);
                resetInput();
                runGenerativeModel(finalPrompt, bitmap);
            }
        });
    }

    private void runGenerativeModel(String prompt, final Bitmap imageBitmap) {
        backgroundExecutor.execute(() -> {
            try {
                GenerativeModel gm = new GenerativeModel(
                        "gemini-2.5-flash",
                        getString(R.string.api_key)
                );
                GenerativeModelFutures model = GenerativeModelFutures.from(gm);

                Content.Builder contentBuilder = new Content.Builder();
                if (imageBitmap != null) {
                    contentBuilder.addImage(imageBitmap);
                }
                contentBuilder.addText(prompt);
                Content content = contentBuilder.build();

                ListenableFuture<GenerateContentResponse> responseFuture = model.generateContent(content);
                GenerateContentResponse response = responseFuture.get();
                String resultText = response.getText();

                DataResponse aiResponse = new DataResponse(1, resultText, "", System.currentTimeMillis());
                runOnUiThread(() -> addResponseToUiAndDb(aiResponse));

            } catch (Exception e) {
                Log.e("GeminiAPI", "Error generating content", e);
                runOnUiThread(() -> {
                    DataResponse errorResponse = new DataResponse(1, "오류가 발생했습니다: " + e.getMessage(), "", System.currentTimeMillis());
                    addResponseToUiAndDb(errorResponse);
                });
            }
        });
    }

    private void loadChatHistory() {
        backgroundExecutor.execute(() -> {
            List<DataResponse> history = chatDao.getAll();
            for (DataResponse data : history) {
                data.setAnimate(false);
                if (data.getImageUri() != null && !data.getImageUri().isEmpty()) {
                    try {
                        Bitmap loadedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(data.getImageUri())));
                        data.setBitmap(loadedBitmap);
                    } catch (IOException e) {
                        Log.e("LoadHistory", "Error loading bitmap from internal storage: " + data.getImageUri(), e);
                    }
                }
            }
            runOnUiThread(() -> {
                adapter.addAll(history);
                if (adapter.getItemCount() > 0) {
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            });
        });
    }

    /**
     * 새로운 채팅 데이터를 UI에 추가하고, 백그라운드에서 DB에 저장하는 헬퍼 메서드
     */
    private void addResponseToUiAndDb(DataResponse dataResponse) {
        // UI 업데이트는 메인 스레드에서 즉시 실행
        runOnUiThread(() -> {
            adapter.add(dataResponse);
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
        });
        // DB 저장은 백그라운드 스레드에서 실행
        backgroundExecutor.execute(() -> chatDao.insert(dataResponse));
    }

    /**
     * 입력창, 이미지, 관련 변수들을 초기 상태로 되돌리는 메서드
     */
    private void resetInput() {
        editText.setText("");
        bitmap = null;
        imageUri = null;
        imageView.setImageResource(R.drawable.ic_add_photo);
    }

    /**
     * 임시 URI로부터 이미지 데이터를 읽어 앱의 내부 저장소에 파일로 복사하고,
     * 그 파일의 영구적인 절대 경로를 반환하는 메서드
     * @param uri Photo Picker에서 받은 임시 content URI
     * @return 내부 저장소에 저장된 파일의 절대 경로 (String)
     * @throws IOException 파일 복사 중 오류 발생 시
     */
    private String saveImageToInternalStorage(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        String fileName = UUID.randomUUID().toString() + ".jpg";
        File file = new File(getFilesDir(), fileName);

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return file.getAbsolutePath();
    }
}
