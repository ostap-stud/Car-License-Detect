package com.github.ostap_stud.ui.details

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images.Media
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.ostap_stud.R
import com.github.ostap_stud.data.db.ImageDetection
import com.github.ostap_stud.databinding.FragmentDetectionDetailsBinding
import java.io.File


class DetectionDetailsFragment : Fragment() {

    private lateinit var binding: FragmentDetectionDetailsBinding
    private val adapter: DetectionDetailsListAdapter = DetectionDetailsListAdapter()
    private val viewModel: DetectionDetailsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetectionDetailsBinding.inflate(inflater, container, false)
        binding.apply {
            rvDetections.layoutManager = LinearLayoutManager(requireContext())
            rvDetections.adapter = adapter
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.imageDetection.value?.let {
            bind(it)
        }
    }

    private fun bind(imageDetection: ImageDetection) {
        val inputStream = requireContext().contentResolver.openInputStream(
            Uri.parse(imageDetection.image.imagePath)
        )
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        binding.apply {
            ivSource.setImageBitmap(bitmap)
            adapter.submitAll(bitmap, imageDetection.detectionEntities)
        }
    }

}