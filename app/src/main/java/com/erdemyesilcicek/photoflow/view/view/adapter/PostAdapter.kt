package com.erdemyesilcicek.photoflow.view.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.erdemyesilcicek.photoflow.databinding.FragmentFeedBinding
import com.erdemyesilcicek.photoflow.databinding.RecyclerRowBinding
import com.erdemyesilcicek.photoflow.view.view.model.Post
import com.squareup.picasso.Picasso

class PostAdapter(private val postList : ArrayList<Post>) : RecyclerView.Adapter<PostAdapter.PostHolder>(){
    class PostHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.binding.recyclerViewMailText.text = postList[position].email
        holder.binding.recyclerViewDescriptionText.text = postList[position].comment
        Picasso.get().load(postList[position].downloadUrl).into(holder.binding.recyclerViewImageView)
    }
}