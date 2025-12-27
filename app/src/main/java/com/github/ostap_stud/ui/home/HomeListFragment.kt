package com.github.ostap_stud.ui.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.ostap_stud.R
import com.github.ostap_stud.analysis.CarLicenseDetector
import com.github.ostap_stud.data.ImageDetectionRepository
import com.github.ostap_stud.data.db.ApplicationDatabase
import com.github.ostap_stud.databinding.FragmentHomeListBinding
import com.github.ostap_stud.ui.details.DetectionDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class HomeListFragment : Fragment() {

    private lateinit var binding: FragmentHomeListBinding

    private val viewModel: HomeListViewModel by viewModels {
        HomeListViewModelFactory(
            ImageDetectionRepository.getRepository(
                ApplicationDatabase.getDatabase(requireContext())
            )
        )
    }
    private val detailsViewModel: DetectionDetailsViewModel by activityViewModels()
    private lateinit var adapter: ImageDetectionListAdapter

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()){ uri ->
        if (uri != null){
            analyzeThenSave(uri)
        } else{
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeListBinding.inflate(inflater, container, false)

        adapter = ImageDetectionListAdapter(
            viewModel.imageDetectionList.value ?: emptyList()
        ) {
            detailsViewModel.imageDetection.value = it
            findNavController().navigate(R.id.navigation_details)
        }

        binding.apply {
            rvImageDetections.layoutManager = LinearLayoutManager(requireContext())
            rvImageDetections.adapter = adapter

            btnImageAnalysis.setOnClickListener {
                pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }

        checkListEmptiness()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.imageDetectionList.observe(viewLifecycleOwner) {
            viewModel.isProcessing.value = true
            adapter.submitData(it)
            viewModel.isProcessing.value = false
        }
        viewModel.isProcessing.observe(viewLifecycleOwner) { isProcessing ->
            if (isProcessing){
                binding.apply {
                    pbProcessing.visibility = View.VISIBLE
                    btnImageAnalysis.visibility = View.GONE
                    tvEmpty.visibility = View.GONE
                    rvImageDetections.visibility = View.INVISIBLE
                }
            } else{
                binding.apply {
                    pbProcessing.visibility = View.GONE
                    btnImageAnalysis.visibility = View.VISIBLE
                    rvImageDetections.visibility = View.VISIBLE
                    checkListEmptiness()
                }
            }
        }
    }

    private fun analyzeThenSave(uri: Uri) {
        viewModel.isProcessing.value = true
        viewLifecycleOwner.lifecycleScope.launch {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val image = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val localCopy = File(requireContext().filesDir, "image_${System.currentTimeMillis()}.jpeg")
            val outputStream = FileOutputStream(localCopy)

            withContext(Dispatchers.IO){
                image.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                CarLicenseDetector.process(image) { det, licDet, _ ->
                    viewModel.insertImageDetections(localCopy.absolutePath, det, licDet)
                }
            }
            viewModel.isProcessing.value = false
        }
    }

    private fun checkListEmptiness(){
        binding.tvEmpty.visibility = if (adapter.itemCount == 0){
            View.VISIBLE
        } else{
            View.GONE
        }
    }
}