package com.portfolio.school;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface Github {

    @GET("/users/{id}")
    Call<User> getUser(
            @Path("id") String id
    );


}
