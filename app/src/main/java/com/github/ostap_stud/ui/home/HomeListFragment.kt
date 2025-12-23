package com.github.ostap_stud.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.ostap_stud.data.ImageDetectionRepository
import com.github.ostap_stud.data.db.ApplicationDatabase
import com.github.ostap_stud.databinding.FragmentHomeListBinding

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeListBinding.inflate(inflater, container, false)

        adapter = ImageDetectionListAdapter(
            viewModel.imageDetectionList.value ?: emptyList()
        )

        binding.apply {
            rvImageDetections.layoutManager = LinearLayoutManager(requireContext())
            rvImageDetections.adapter = adapter
        }

        checkListEmptiness()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.imageDetectionList.observe(viewLifecycleOwner) {
            adapter.submitData(it)
            checkListEmptiness()
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