package com.github.ostap_stud.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.ostap_stud.data.ImageDetectionRepository
import com.github.ostap_stud.analysis.Detection
import com.github.ostap_stud.analysis.LicenseDetection
import com.github.ostap_stud.data.db.DetectionEntity
import com.github.ostap_stud.data.db.Image
import com.github.ostap_stud.data.db.ImageDetection
import kotlinx.coroutines.launch
import java.util.Date

class HomeListViewModel(
    private val imageDetectionRepository: ImageDetectionRepository
) : ViewModel() {

    val isProcessing: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>(false) }

    var imageDetectionList: LiveData<List<ImageDetection>> = imageDetectionRepository.getAllImageDetections()

    fun insertImageDetections(imagePath: String, detections: List<Detection>, licenseDetections: List<LicenseDetection>){
        viewModelScope.launch {
            val detectionEntities = mutableListOf<DetectionEntity>()
            detections.forEach { det ->
                detectionEntities.add(
                    DetectionEntity(
                        imageId = null,
                        x1 = det.x1, y1 = det.y1,
                        x2 = det.x2, y2 = det.y2,
                        score = det.score, cls = det.cls
                    )
                )
            }
            licenseDetections.forEach { det ->
                detectionEntities.add(
                    DetectionEntity(
                        imageId = null,
                        x1 = det.x1, y1 = det.y1,
                        x2 = det.x2, y2 = det.y2,
                        score = det.score, cls = det.cls,
                        text = det.numberText
                    )
                )
            }
            val image = Image(imagePath = imagePath, createdAt = Date())
            imageDetectionRepository.insertImageDetections(image, detectionEntities)
        }
    }

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