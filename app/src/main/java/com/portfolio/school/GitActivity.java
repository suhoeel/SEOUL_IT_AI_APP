package com.portfolio.school;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GitActivity extends AppCompatActivity {

    TextView id;
    TextView follower;
    TextView following;

    EditText editId;

    Button button;

    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_git);

        id = findViewById(R.id.id);
        follower = findViewById(R.id.follower);
        following = findViewById(R.id.following);
        editId = findViewById(R.id.edit_id);
        button = findViewById(R.id.send);
        imageView = findViewById(R.id.imageview);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchUser();
            }
        });


    }

    final String url = "https://api.github.com/";

    public void fetchUser() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Github github = retrofit.create(Github.class);

        Call<User> call = github.getUser(editId.getText().toString());

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()) {
                    User user = response.body();
                    if(user != null) {
                        id.setText("id : " + user.getLogin());
                        follower.setText("follower : " + user.getFollowers());
                        following.setText("following : " + user.getFollowing());

                        Glide.with(GitActivity.this)
                                .load(user.getAvatarUrl())
                                .into(imageView);

                    }
                } else {
                    Log.e("TEST", "에러 발생 " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("TEST", "에러 발생2 " + t.getMessage());
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
