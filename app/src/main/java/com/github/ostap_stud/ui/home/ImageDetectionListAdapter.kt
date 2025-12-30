package com.github.ostap_stud.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.ostap_stud.analysis.CarLicenseDetector
import com.github.ostap_stud.data.db.ImageDetection
import com.github.ostap_stud.databinding.ImageDetectionItemBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ImageDetectionListAdapter(
    private var imageDetectionList: List<ImageDetection>,
    private val itemLongClickManager: OnDetectionSelectManager,
    private val isSelecting: Boolean,
    private val currentSelectListeners: List<OnDetectionSelectListener>,
    private val onItemClicked: (ImageDetection) -> Unit
) : RecyclerView.Adapter<ImageDetectionListAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: ImageDetectionItemBinding
    ) : RecyclerView.ViewHolder(binding.root), OnDetectionSelectListener {

        init {
            itemLongClickManager.subscribe(this)
            binding.itemCard.apply{
                setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION){
                        onItemClicked(imageDetectionList[position])
                    }
                }
                setOnLongClickListener {
                    itemLongClickManager.updateAllSelectionAbility(true)
                    updateSelected(true)
                    true
                }
            }
        }

        fun bind(item: ImageDetection) = with(binding){
            val carsNum =
                item.detectionEntities.count { it.cls == CarLicenseDetector.LABELS[2] }
            val licNum = item.detectionEntities.count { it.cls == CarLicenseDetector.LABELS[0] }
            val formattedDate = FORMATTER.format(item.image.createdAt)
            tvFileName.text = formattedDate
            tvCarsNum.text = "Cars: $carsNum"
            tvLicNum.text = "Licenses: $licNum"
        }

        override fun isSelected() = binding.cbSelected.isChecked

        override fun updateSelected(selected: Boolean) {
            binding.cbSelected.isChecked = selected
        }
        
        override fun updateSelectionAbility(selectable: Boolean) {
            binding.cbSelected.apply {
                visibility = if (selectable) View.VISIBLE else View.GONE
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
        if (isSelecting) {
            holder.updateSelectionAbility(true)
            if (currentSelectListeners[position].isSelected()) {
                holder.updateSelected(true)
            }
        }
    }

    companion object{
        val FORMATTER = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
    }

}

interface OnDetectionSelectListener{
    fun updateSelectionAbility(selectable: Boolean)
    fun updateSelected(selected: Boolean)
    fun isSelected(): Boolean
}