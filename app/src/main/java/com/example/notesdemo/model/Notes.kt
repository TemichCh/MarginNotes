package com.example.notesdemo.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity(tableName = "Notes")
@Parcelize
data class Notes(
    @PrimaryKey(autoGenerate = true)
    var noteId: Int? = null,
    val userId: String? = null, //на будущее для разделения по юзерам
    var noteName: String,
    var noteText: String,
    var image: String? = null, //TODO: media.Image может быть заменить на путь к файлу локально
    var createDate: Date,
    var modifiedDate: Date? = null
) : Parcelable