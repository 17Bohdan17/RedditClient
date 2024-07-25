package com.mycompany.redditclient

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.Manifest
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.OutputStream


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postsAdapter: PostsAdapter
    private var after: String? = null
    private var isLoading = false
    private var currentPage = 1

    private val REQUEST_WRITE_STORAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            checkPermissions()
        } else {
            initRecyclerView()
            fetchTopPosts()
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_STORAGE
            )
        } else {
            initRecyclerView()
            fetchTopPosts()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_WRITE_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    initRecyclerView()
                    fetchTopPosts()
                } else {
                    Toast.makeText(this, "Permission required to save images", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchTopPosts(after: String? = null) {
        val call = RetrofitInstance.api.getTopPosts(after)

        call.enqueue(object : Callback<RedditResponse> {
            override fun onResponse(call: Call<RedditResponse>, response: Response<RedditResponse>) {
                if (response.isSuccessful) {
                    response.body()?.data?.children?.let { redditChildren ->
                        val posts = redditChildren.map { it.data }
                        if (after == null) {
                            postsAdapter = PostsAdapter(posts.toMutableList(), this@MainActivity)
                            recyclerView.adapter = postsAdapter
                        } else {
                            postsAdapter.addPosts(posts)
                        }
                        this@MainActivity.after = response.body()?.data?.after
                    }
                } else {
                    Log.e("MainActivity", "Response not successful!")
                }
                isLoading = false
            }

            override fun onFailure(call: Call<RedditResponse>, t: Throwable) {
                Log.e("MainActivity", "Error fetching posts", t)
                isLoading = false
            }
        })
    }

    private fun loadMore() {
        fetchTopPosts(after)
    }

    fun saveImageToGallery(imageUrl: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(imageUrl).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("MainActivity", "Failed to download image", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Failed to download image", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.body()?.let { responseBody ->
                    val inputStream = responseBody.byteStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()

                    saveBitmapToGallery(bitmap)
                }
            }
        })
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.jpg"
        val outputStream: OutputStream?
        val contentResolver = contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        outputStream = imageUri?.let { contentResolver.openOutputStream(it) }

        outputStream?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            runOnUiThread {
                Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show()
            }
        }
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

