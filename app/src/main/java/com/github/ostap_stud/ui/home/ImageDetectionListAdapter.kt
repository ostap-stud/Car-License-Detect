package com.github.ostap_stud.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.github.ostap_stud.analysis.CarLicenseDetector
import com.github.ostap_stud.data.ImageDetectionItem
import com.github.ostap_stud.databinding.ImageDetectionItemBinding
import com.github.ostap_stud.util.DateSerializer

class ImageDetectionListAdapter(
    private var imageDetectionItems: MutableList<ImageDetectionItem>,
    private val onItemClicked: (ImageDetectionItem) -> Unit,
    private val onItemSelected: (position: Int, selected: Boolean) -> Unit
) : RecyclerView.Adapter<ImageDetectionListAdapter.ViewHolder>() {

    inner class ViewHolder(
        val binding: ImageDetectionItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.itemCard.apply{
                setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION){
                        onItemClicked(imageDetectionItems[position])
                    }
                }
                setOnLongClickListener {
                    val position = adapterPosition
                    setItemsSelectable(position)
                    onItemSelected(position, true)
                    true
                }
            }
        }

        fun bind(item: ImageDetectionItem) = with(binding){
            val imageDetection = item.imageDetection
            val carsNum =
                imageDetection.detectionEntities.count { it.cls == CarLicenseDetector.LABELS[2] }
            val licNum = imageDetection.detectionEntities.count { it.cls == CarLicenseDetector.LABELS[0] }
            val formattedDate = DateSerializer.formatter.format(imageDetection.image.createdAt)
            tvFileName.text = formattedDate
            tvCarsNum.text = "Cars: $carsNum"
            tvLicNum.text = "Licenses: $licNum"
            cbSelected.setOnCheckedChangeListener(null)
            cbSelected.apply {
                visibility = if (imageDetectionItems.any { it.isSelected }) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                isChecked = item.isSelected
            }
        }

        fun setItemSelection(pos: Int, selected: Boolean) {
            imageDetectionItems[pos] = imageDetectionItems[pos].copy(
                isSelected = selected
            )
        }

        private fun setItemsSelectable(selectedPos: Int) {
            setItemSelection(selectedPos, true)
            if (binding.cbSelected.isVisible) {
                notifyItemChanged(selectedPos)
            } else {
                notifyDataSetChanged()
            }
        }

    }

    fun submitData(newData: MutableList<ImageDetectionItem>){
        imageDetectionItems = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ImageDetectionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return imageDetectionItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imageDetectionItems[position])
        holder.binding.cbSelected.setOnCheckedChangeListener { _, checked ->
            holder.setItemSelection(position, checked)
            onItemSelected(position, checked)
            notifyItemChanged(position)
        }

    }

}