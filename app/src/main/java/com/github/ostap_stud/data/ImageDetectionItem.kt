package com.github.ostap_stud.data

import com.github.ostap_stud.data.db.ImageDetection

data class ImageDetectionItem(
    val imageDetection: ImageDetection,
    val isSelected: Boolean
)