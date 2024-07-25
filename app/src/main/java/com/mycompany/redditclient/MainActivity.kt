package com.mycompany.redditclient

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postsAdapter: PostsAdapter
    private var after: String? = null
    private var isLoading = false


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchTopPosts()

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (!isLoading && layoutManager.findLastCompletelyVisibleItemPosition() == postsAdapter.itemCount - 1) {
                    isLoading = true
                    loadMore()
                }
            }
        })
    }

    private fun fetchTopPosts(after: String? = null) {
        val call = RetrofitInstance.api.getTopPosts(after)

        call.enqueue(object: Callback<RedditResponse> {
            override fun onResponse(call: Call<RedditResponse>, response: Response<RedditResponse>) {
                if(response.isSuccessful){
                    response.body()?.data?.children?.let { redditChildren ->
                        val posts = redditChildren.map { it.data }
                        if (after == null) {
                            postsAdapter = PostsAdapter(posts.toMutableList())
                            recyclerView.adapter = postsAdapter
                        } else {
                            val positionStart = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                            val topView = recyclerView.getChildAt(0)
                            val topOffset = topView?.top ?: 0

                            postsAdapter.addPosts(posts)
                            recyclerView.scrollToPosition(positionStart)
                            recyclerView.scrollBy(0, topOffset)
                        }
                        this@MainActivity.after = response.body()?.data?.after
                    }
                } else {
                    Log.e("MainActivity", "Response not successful!")
                }
                isLoading = false
            }

            override fun onFailure(call: Call<RedditResponse>, t: Throwable){
                Log.e("MainActivity", "Error fetching posts", t)
                isLoading = false
            }
        })
    }

    private fun loadMore() {
        fetchTopPosts(after)  // Вызов функции для получения следующей страницы с параметром `after`
    }
}


data class RedditResponse(
    val data: RedditData,
)

data class RedditData(
    val children: List<RedditChildren>,
    val after: String?,
)

data class RedditChildren(
    val data: RedditPost,
)

data class RedditPost(
    val title: String,
    val author: String,
    val created_utc: Long,
    val thumbnail: String,
    val num_comments: Int,
    val url: String,
)