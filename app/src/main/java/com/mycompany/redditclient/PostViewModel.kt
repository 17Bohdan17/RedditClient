package com.mycompany.redditclient

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class PostViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    // Живі дані для постів
    private val _posts = MutableLiveData<List<RedditPost>>()
    val posts: LiveData<List<RedditPost>> get() = _posts

    private val afterKey = "AFTER_KEY"

    // Ключ для збереження стану
    var after: String?
        get() = savedStateHandle.get(afterKey)
        set(value) {
            savedStateHandle.set(afterKey, value)
        }

    // Завантажує топ-пости з Reddit
    fun fetchTopPosts(after: String? = null) {
        val call = RetrofitInstance.api.getTopPosts(after)

        call.enqueue(object: Callback<RedditResponse> {
            override fun onResponse(
                call: Call<RedditResponse>,
                response: Response<RedditResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.data?.children?.let { redditChildren ->
                        val posts = redditChildren.map { it.data }
                        _posts.postValue(posts)
                        this@PostViewModel.after = response.body()?.data?.after
                    }
                }
            }

            // Обробка помилок при завантаженні постів
            override fun onFailure(call: Call<RedditResponse>, t: Throwable) {
                Log.e("PostViewModel", "Error fetching posts", t)
            }
        })
    }
}
