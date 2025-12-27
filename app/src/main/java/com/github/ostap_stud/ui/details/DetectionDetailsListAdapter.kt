package com.github.ostap_stud.ui.details

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.ostap_stud.data.db.DetectionEntity
import com.github.ostap_stud.databinding.DetectionItemBinding
import java.math.BigDecimal
import java.math.RoundingMode

class DetectionDetailsListAdapter(
    private var imageBitmap: Bitmap? = null,
    private var imageDetections: List<DetectionEntity> = emptyList()
) : RecyclerView.Adapter<DetectionDetailsListAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: DetectionItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(detection: DetectionEntity) = with(binding) {
            tvClassAndConf.text = detection.cls.plus(
                " (${(
                        BigDecimal(detection.score.toDouble())
                            .setScale(2, RoundingMode.HALF_EVEN)
                            .toDouble() * 100
                        ).toInt()}%)"
            )
            tvText.also {
                if (detection.text.isNotBlank()){
                    it.text = detection.text
                } else {
                    it.visibility = View.GONE
                }
            }
            tvX1.text = "x1: ${BigDecimal(detection.x1.toDouble()).setScale(2, RoundingMode.HALF_EVEN)}"
            tvX2.text = "x2: ${BigDecimal(detection.x2.toDouble()).setScale(2, RoundingMode.HALF_EVEN)}"
            tvY1.text = "y1: ${BigDecimal(detection.y1.toDouble()).setScale(2, RoundingMode.HALF_EVEN)}"
            tvY2.text = "y2: ${BigDecimal(detection.y2.toDouble()).setScale(2, RoundingMode.HALF_EVEN)}"
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