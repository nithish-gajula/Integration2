package com.example.integration2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.makeramen.roundedimageview.RoundedImageView

class ImageAdapter(private val imageList: ArrayList<Int>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: RoundedImageView = itemView.findViewById(R.id.imageSlide)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_slide_container, parent, false)
        return ImageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageResource(imageList[position])
    }

}