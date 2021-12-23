package com.example.notesdemo.dao

import androidx.room.TypeConverter
import java.util.*

// FIXME это должен быть object - нам нет необходимости создавать объекты типа Converters
//  https://kotlinlang.org/docs/object-declarations.html#object-declarations-overview
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}