package com.mycompany.redditclient

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val imageView: ImageView = findViewById(R.id.fullImageView)
        val imageUrl = intent.getStringExtra("IMAGE_URL")

        // Завантажуємо зображення, якщо URL доступний
        imageUrl?.let {
            Glide.with(this)
                .load(it)
                .into(imageView)
        }
    }
}
