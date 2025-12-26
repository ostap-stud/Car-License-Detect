package com.github.ostap_stud.data

import androidx.lifecycle.LiveData
import com.github.ostap_stud.data.db.ApplicationDatabase
import com.github.ostap_stud.data.db.DetectionEntity
import com.github.ostap_stud.data.db.Image
import com.github.ostap_stud.data.db.ImageDetection
import com.github.ostap_stud.data.db.ImageDetectionDAO

class ImageDetectionRepository(
    private val imageDetectionDAO: ImageDetectionDAO
) {

    fun getAllImageDetections(): LiveData<List<ImageDetection>> = imageDetectionDAO.getAllImagesAndDetections()

    suspend fun getImageDetections(imageId: Long): ImageDetection = imageDetectionDAO.getImageAndDetections(imageId)

    suspend fun insertImageDetections(image: Image, detectionEntities: List<DetectionEntity>) =
        imageDetectionDAO.insertImageAndDetections(image, detectionEntities)

    companion object{
        private var INSTANCE: ImageDetectionRepository? = null
        fun getRepository(database: ApplicationDatabase): ImageDetectionRepository{
            return INSTANCE ?: synchronized(this){
                val instance = ImageDetectionRepository(database.imageDetectionDao())
                INSTANCE = instance
                instance
            }
        }
    }

}