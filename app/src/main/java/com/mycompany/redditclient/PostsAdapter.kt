package com.mycompany.redditclient

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostsAdapter(private var posts: MutableList<RedditPost>, private val context: Context) :
    RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    // Додає нові пости до списку
    fun addPosts(newPosts: List<RedditPost>) {
        val currentSize = posts.size
        posts.addAll(newPosts)
        notifyItemRangeInserted(currentSize, newPosts.size)
    }

    // Створює новий ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.activity_main, parent, false)
        return PostViewHolder(view)
    }

    // Прив'язує дані до ViewHolder
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.postTitle.text = post.title
        holder.postAuthor.text = "Author: ${post.author}"

        // Форматує та відображає дату
        val date = Date(post.created_utc * 1000)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val formattedDate = sdf.format(date)
        holder.postDate.text = formattedDate

        holder.postComments.text = "Comments: ${post.num_comments}"

        // Перевіряє, чи є у поста зображення
        if (post.thumbnail.isNotEmpty() && post.thumbnail.startsWith("http")) {
            holder.postThumbnail.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(post.thumbnail)
                .into(holder.postThumbnail)

            // Відкриває зображення в новій активності
            holder.postThumbnail.setOnClickListener {
                val intent = Intent(holder.itemView.context, ImageActivity::class.java)
                intent.putExtra("IMAGE_URL", post.url)
                holder.itemView.context.startActivity(intent)
            }

            // Зберігає зображення в галерею
            holder.postThumbnail.setOnLongClickListener {
                (context as MainActivity).saveImageToGallery(post.thumbnail)
                true
            }

        } else {
            holder.postThumbnail.visibility = View.GONE
        }
    }

    // Повертає кількість постів
    override fun getItemCount(): Int = posts.size

    // Внутрішній клас для ViewHolder
    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postTitle: TextView = itemView.findViewById(R.id.postTitle)
        val postAuthor: TextView = itemView.findViewById(R.id.postAuthor)
        val postDate: TextView = itemView.findViewById(R.id.postDate)
        val postThumbnail: ImageView = itemView.findViewById(R.id.postThumbnail)
        val postComments: TextView = itemView.findViewById(R.id.postComments)
    }
}
