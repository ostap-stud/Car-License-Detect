package com.github.ostap_stud.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class Image(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imagePath: String
)

data class ImageDetection(
    @Embedded val image: Image,
    @Relation(
        parentColumn = "id",
        entityColumn = "imageId"
    )
    val detections: List<Detection>
)

@Entity
data class Detection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imageId: Long?,
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val score: Float,
    val cls: String,
    var text: String = ""
)