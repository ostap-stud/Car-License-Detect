package com.github.ostap_stud.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.ostap_stud.analysis.CarLicenseDetector
import com.github.ostap_stud.data.db.ImageDetection
import com.github.ostap_stud.databinding.ImageDetectionItemBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ImageDetectionListAdapter(
    private var imageDetectionList: List<ImageDetection>,
    private val onItemClicked: (ImageDetection) -> Unit
) : RecyclerView.Adapter<ImageDetectionListAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: ImageDetectionItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.itemCard.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION){
                    onItemClicked(imageDetectionList[position])
                }
            }
        }

        fun bind(item: ImageDetection){
            binding.apply {
                val carsNum = item.detectionEntities.filter { it.cls == CarLicenseDetector.LABELS[2] }.count()
                val licNum = item.detectionEntities.filter { it.cls == CarLicenseDetector.LABELS[0] }.count()
                val formattedDate = FORMATTER.format(item.image.createdAt)
                tvFileName.text = formattedDate
                tvCarsNum.text = "Cars: $carsNum"
                tvLicNum.text = "Licenses: $licNum"
            }
        }

    }

    fun submitData(newData: List<ImageDetection>){
        imageDetectionList = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ImageDetectionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return imageDetectionList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imageDetectionList[position])
    }

    companion object{
        val LOCALE = Locale.getDefault()
        val PATTERN = "yyyy/MM/dd HH:mm:ss"
        val FORMATTER = SimpleDateFormat(PATTERN, LOCALE)
    }

}