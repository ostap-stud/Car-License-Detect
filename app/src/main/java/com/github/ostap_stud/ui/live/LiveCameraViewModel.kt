package com.github.ostap_stud.ui.live

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LiveCameraViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is LiveCamera Fragment"
    }
    val text: LiveData<String> = _text
}