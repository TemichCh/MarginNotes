package com.example.notesdemo.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity(tableName = "Notes")
data class Note(
    @PrimaryKey(autoGenerate = true) var noteId: Int? = null,
    var noteName: String,
    var noteText: String,
    // FIXME мы не можем полагаться на ссылки до изображений вне нашего приложения - они в любой
    //  момент могут стать мертвыми (юзер просто удалит изображение в галерее например).
    //  мы должны здесь хранить путь до файла в данных нашего же приложения - в его dataDir.
    //  оттуда никто не сможет удалить файлы кроме нашего же приложения (или очистки всех данных
    //  нашего приложения)
    //  и при выборе изображения мы должны записывать копию его себе в dataDir а в базу писать имя
    //  этого файла в нашей dataDir
    var image: String? = null,
    var createDate: Date,
    var modifiedDate: Date? = null,
    // FIXME не стоит вписывать в сущность заметки факт "выбрана ли она на конкретном экране" - надо
    //  просто во вьюмодели соответствующего экрана держать список айдишников выбранных заметок.
    //  выбор это чисто ui детали, которые ниже viewmodel спуститься не могут
)