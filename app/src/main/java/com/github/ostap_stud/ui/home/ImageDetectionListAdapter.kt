package com.github.ostap_stud.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.ostap_stud.data.db.ImageDetection
import com.github.ostap_stud.databinding.ImageDetectionItemBinding

class ImageDetectionListAdapter(
    private var imageDetectionList: List<ImageDetection>
) : RecyclerView.Adapter<ImageDetectionListAdapter.ViewHolder>() {

    class ViewHolder(
        private val binding: ImageDetectionItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ImageDetection){
            binding.apply {
                tvFileName.text = item.image.imagePath
            }
        }

    }

    fun submitData(newData: List<ImageDetection>){
        imageDetectionList = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ImageDetectionItemBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return imageDetectionList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imageDetectionList[position])
    }

}