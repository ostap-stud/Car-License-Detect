package com.github.ostap_stud.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Image::class, DetectionEntity::class], version= 1)
@TypeConverters(Converters::class)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun imageDetectionDao(): ImageDetectionDAO

    companion object{
        @Volatile
        private var INSTANCE: ApplicationDatabase? = null
        fun getDatabase(context: Context): ApplicationDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context, ApplicationDatabase::class.java, "image_detection_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}