package com.example.monitrack.data.util

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun localDateToEpochDay(date: LocalDate): Long = date.toEpochDay()

    @TypeConverter
    fun epochDayToLocalDate(value: Long): LocalDate = LocalDate.ofEpochDay(value)

    @TypeConverter
    fun localTimeToSecondOfDay(time: LocalTime): Int = time.toSecondOfDay()

    @TypeConverter
    fun secondOfDayToLocalTime(value: Int): LocalTime = LocalTime.ofSecondOfDay(value.toLong())
}
