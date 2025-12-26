package com.github.ostap_stud.ui.details

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.ostap_stud.data.db.DetectionEntity
import com.github.ostap_stud.databinding.DetectionItemBinding

class DetectionDetailsListAdapter(
    private var imageBitmap: Bitmap? = null,
    private var imageDetections: List<DetectionEntity> = emptyList()
) : RecyclerView.Adapter<DetectionDetailsListAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: DetectionItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(detection: DetectionEntity) = with(binding) {
            tvClass.text = detection.cls
            tvText.text = detection.text
            tvX1.text = "x1: ${detection.x1}"
            tvX2.text = "x2: ${detection.x2}"
            tvY1.text = "y1: ${detection.y1}"
            tvY2.text = "y2: ${detection.y2}"
            val cropped = Bitmap.createBitmap(
                imageBitmap!!, detection.x1.toInt(), detection.y1.toInt(),
                (detection.x2 - detection.x1).toInt(), (detection.y2 - detection.y1).toInt()
            )
            ivCropped.setImageBitmap(cropped)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DetectionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = imageDetections.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imageDetections[position])
    }

    fun submitAll(imageBitmap: Bitmap, imageDetections: List<DetectionEntity>){
        if (this.imageBitmap == null){
            this.imageBitmap = imageBitmap
        }
        this.imageDetections = imageDetections
        notifyDataSetChanged()
    }

}