package com.github.ostap_stud.ui.live

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.ostap_stud.databinding.FragmentLiveCameraBinding

class LiveCameraFragment : Fragment() {

    private var _binding: FragmentLiveCameraBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LiveCameraViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveCameraBinding.inflate(inflater)

        viewModel.text.observe(viewLifecycleOwner){
            binding.apply {
                tvLiveCamera.text = it
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}