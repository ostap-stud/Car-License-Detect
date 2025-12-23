package com.github.ostap_stud.data.db

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    @TypeConverter
    fun fromDate(date: Date): Long{
        return date.time
    }

    @TypeConverter
    fun fromTimestamp(value: Long): Date{
        return Date(value)
    }

}