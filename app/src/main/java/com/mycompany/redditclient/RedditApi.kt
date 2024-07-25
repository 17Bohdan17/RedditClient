package com.mycompany.redditclient

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RedditApi {
    @GET("/top.json")
    fun getTopPosts(
        @Query("after") after: String? = null
    ): Call<RedditResponse>
}

object RetrofitInstance {
    private val retrofit by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl("https://www.reddit.com")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
    }

    val api: RedditApi by lazy {
        retrofit.create(RedditApi::class.java)
    }
}