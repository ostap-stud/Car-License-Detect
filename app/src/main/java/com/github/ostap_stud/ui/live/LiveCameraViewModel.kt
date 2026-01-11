package com.github.ostap_stud.ui.live

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LiveCameraViewModel : ViewModel() {
    var capturedBitmap: Bitmap? = null
}