package com.mycompany.redditclient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PostsAdapter(private val posts: List<RedditPost>) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_main, parent, false)
        return PostViewHolder(view)
    }


    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.postTitle.text = post.title
        holder.postUrl.text = post.url

        if(post.url.endsWith("jpg") || post.url.endsWith(".png")){
            holder.postImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(post.url)
                .into(holder.postImage)
        } else {
            holder.postImage.visibility = View.GONE
        }
    }



    override fun getItemCount(): Int {
        return posts.size
    }


    class PostViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val postTitle: TextView = itemView.findViewById(R.id.postTitle)
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val postUrl: TextView = itemView.findViewById(R.id.postUrl)
    }
}


