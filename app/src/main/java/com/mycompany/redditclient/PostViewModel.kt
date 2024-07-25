package com.mycompany.redditclient

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostViewModel : ViewModel() {

    private val _posts = MutableLiveData<List<RedditPost>>()
    val posts: LiveData<List<RedditPost>> get() = _posts

    fun fetchTopPosts(){
        val call = RetrofitInstance.api.getTopPosts()

        call.enqueue(object: Callback<RedditResponse> {
            override fun onResponse(
                call: Call<RedditResponse>,
                response: Response<RedditResponse>
            ) {
                if(response.isSuccessful){
                    response.body()?.data?.children?.let { redditChildren ->
                        val posts = redditChildren.map {
                            it.data
                        }
                        _posts.postValue(posts)
                    }
                }
            }

            override fun onFailure(call: Call<RedditResponse>, t: Throwable) {
                Log.e("MainActivity", "Error fetching posts", t)
            }
        })


    }



}