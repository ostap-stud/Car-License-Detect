package com.github.ostap_stud.ui.details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.ostap_stud.data.db.ImageDetection

class DetectionDetailsViewModel : ViewModel(){
    var imageDetection: MutableLiveData<ImageDetection> = MutableLiveData()
}