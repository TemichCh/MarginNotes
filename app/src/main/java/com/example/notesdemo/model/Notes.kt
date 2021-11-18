package com.example.notesdemo.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*


@Entity(tableName = "Notes")
@Parcelize
data class Notes(
    @PrimaryKey(autoGenerate = true) var noteId: Int?,
    var userId: String? = null, //на будущее для разделения по юзерам
    var noteName: String,
    var noteText: String,
    var image: String? = null, //TODO: media.Image может быть заменить на путь к файлу локально
    var createDate: Date,
    var modifiedDate: Date? = null,

    @Ignore var selected: Boolean = false
) : Parcelable {
    constructor (noteName: String, noteText: String, image: String?, createDate: Date) : this(
        null,
        null,
        noteName = noteName,
        noteText = noteText,
        image = image,
        createDate = createDate
    )
}