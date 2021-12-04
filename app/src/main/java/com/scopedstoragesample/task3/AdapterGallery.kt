package com.scopedstoragesample.task3

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.scopedstoragesample.R
import com.scopedstoragesample.task3.util.ClickItem
import com.scopedstoragesample.databinding.ItemGalleryBinding

class AdapterGallery(private val photos: List<ImageModel>, private val clickItem: ClickItem) :
    RecyclerView.Adapter<AdapterGallery.ViewHolder>()  {

    var positionItem : Int = 0
    class ViewHolder(private val itemBinding: ItemGalleryBinding, private val context: Context) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bin(imageModel: ImageModel) {
            Glide.with(context)
                .load(imageModel.urlImage)
                .placeholder(R.color.black)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade(500))
                .into(itemBinding.imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding =
            ItemGalleryBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(itemBinding, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bin(photos[position])
        holder.itemView.setOnClickListener { clickItem.selectItem(photos[positionItem]) }
        positionItem = position
    }

    override fun getItemCount() = photos.size



}