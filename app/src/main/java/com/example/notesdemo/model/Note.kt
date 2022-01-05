package com.example.notesdemo.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity(tableName = "Notes")
data class Note(
    @PrimaryKey(autoGenerate = true) var noteId: Int? = null,
    var noteName: String,
    var noteText: String,
    var image: String? = null,
    var createDate: Date,
    var modifiedDate: Date? = null,
)