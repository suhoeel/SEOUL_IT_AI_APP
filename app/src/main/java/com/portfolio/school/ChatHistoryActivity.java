package com.portfolio.school;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.portfolio.school.adapter.ChatHistoryAdapter;
import com.portfolio.school.adapter.ChatHistoryListener;
import com.portfolio.school.db.ChatDatabase;
import com.portfolio.school.db.DataResponseDao;
import com.portfolio.school.model.DataResponse;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DataResponseDao chatDao;
    private ChatHistoryAdapter adapter;

    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recycler_view_id);


        chatDao = ChatDatabase.getDatabase(this).chatDao();
        adapter = new ChatHistoryAdapter(this, new ChatHistoryListener() {
            @Override
            public void delete(String formattedDate) {
                backgroundExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        chatDao.deleteDate(Utils.loadStartOfDay(formattedDate), Utils.loadEndOfDay(formattedDate));
                    }
                });
            }
        });
        recyclerView.setAdapter(adapter);

        loadChatHistory();

    }

    private void loadChatHistory() {
        backgroundExecutor.execute(() -> {
            List<DataResponse> history = chatDao.getChatsNoDuplicateDate();
            Log.d("TEST", "history " + history);
            runOnUiThread(() -> {
                adapter.setList(history);
                if (adapter.getItemCount() > 0) {
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            });
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
