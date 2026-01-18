package com.github.ostap_stud.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.github.ostap_stud.util.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date

@Entity
@Serializable
data class Image(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imagePath: String,
    @Serializable(with = DateSerializer::class)
    val createdAt: Date
)

@Serializable
data class ImageDetection(
    @Embedded val image: Image,
    @Relation(
        parentColumn = "id",
        entityColumn = "imageId"
    )
    val detectionEntities: List<DetectionEntity>
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Image::class,
            parentColumns = ["id"],
            childColumns = ["imageId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Serializable
data class DetectionEntity(
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