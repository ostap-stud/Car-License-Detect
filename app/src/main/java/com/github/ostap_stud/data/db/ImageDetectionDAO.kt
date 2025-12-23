package com.github.ostap_stud.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ImageDetectionDAO {

    @Transaction
    @Query("SELECT * FROM image")
    fun getAllImagesAndDetections(): LiveData<List<ImageDetection>>

    @Transaction
    @Query("SELECT * FROM image WHERE id = :imageId")
    suspend fun getImageAndDetections(imageId: Long): ImageDetection

    @Insert
    suspend fun insertImage(image: Image): Long

    @Insert
    suspend fun insertDetections(detections: List<Detection>)

    @Transaction
    suspend fun insertImageAndDetections(image: Image, detections: List<Detection>){
        val insertedImageId = insertImage(image)
        val imageDetections = detections.map { det ->
            det.copy(imageId = insertedImageId)
        }
        insertDetections(imageDetections)
    }

}