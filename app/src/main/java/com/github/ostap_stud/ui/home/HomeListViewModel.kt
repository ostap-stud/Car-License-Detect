package com.github.ostap_stud.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.ostap_stud.data.ImageDetectionRepository
import com.github.ostap_stud.data.db.ImageDetection

class HomeListViewModel(
    private val imageDetectionRepository: ImageDetectionRepository
) : ViewModel() {

    var imageDetectionList: LiveData<List<ImageDetection>> = imageDetectionRepository.getAllImageDetections()

}

@Suppress("UNCHECKED_CAST")
class HomeListViewModelFactory(
    private val imageDetectionRepository: ImageDetectionRepository
) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeListViewModel::class.java)){
            return HomeListViewModel(imageDetectionRepository) as T
        }
        throw IllegalArgumentException("Can't create HomeListViewModel from ${modelClass.name}")
    }
}