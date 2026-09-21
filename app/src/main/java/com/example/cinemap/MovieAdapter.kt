package com.example.cinemap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cinemap.data.Movie

class MovieAdapter(private val onClick: (Movie) -> Unit) : ListAdapter<Movie, MovieAdapter.VH>(DiffCallback()) {
    class VH(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val movie = getItem(position)
        holder.view.findViewById<TextView>(R.id.txtTitle).text = movie.title
        val img = holder.view.findViewById<ImageView>(R.id.imgPoster)
        // Glide.with(holder.view).load(movie.getPosterUrl()).into(img)
        holder.view.setOnClickListener { onClick(movie) }
    }

    class DiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(a: Movie, b: Movie) = a.id == b.id
        override fun areContentsTheSame(a: Movie, b: Movie) = a == b
    }
}
