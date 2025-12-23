package com.github.ostap_stud.ui.home

import android.content.Intent
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.ostap_stud.analysis.CarLicenseDetector
import com.github.ostap_stud.data.ImageDetectionRepository
import com.github.ostap_stud.data.db.ApplicationDatabase
import com.github.ostap_stud.databinding.FragmentHomeListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeListFragment : Fragment() {

    private lateinit var binding: FragmentHomeListBinding

    private val viewModel: HomeListViewModel by viewModels {
        HomeListViewModelFactory(
            ImageDetectionRepository(
                ApplicationDatabase.getDatabase(requireContext()).imageDetectionDao()
            )
        )
    }
    private lateinit var adapter: ImageDetectionListAdapter

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()){ uri ->
        if (uri != null){
            requireContext().contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
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
            viewModel.imageDetectionList.value ?: emptyList(),
            { Toast.makeText(binding.root.context, "Clicked on image - ${it.image.createdAt}", Toast.LENGTH_SHORT).show() }
        )

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
            val imagePath = uri.path!!
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val image = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            withContext(Dispatchers.IO){
                CarLicenseDetector.process(image) { det, licDet, _ ->
                    viewModel.insertImageDetections(imagePath, det, licDet)
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