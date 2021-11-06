package com.example.notesdemo.model

import android.media.Image
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "Notes")
data class Notes(
    @PrimaryKey(autoGenerate = true)
    var noteId: Int? = null,
    val userId: String?=null, //на будущее для разделения по юзерам
    val noteName:String,
    val noteText:String,
    val image: Image, //TODO: media.Image может быть заменить на путь к файлу локально
    val createDate:Date,
    val modifiedDate:Date
)
