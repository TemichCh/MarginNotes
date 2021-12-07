package com.example.notesdemo.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*


@Entity(tableName = "Notes")
// FIXME @Parcelize излишен - не надо эти данные в парселабл упаковывать. вместо этого между экранами
//  надо слать noteId, а дальше в любом месте по id из базы вытащим
@Parcelize
// FIXME нейминг некорректный. сам датакласс это Note - заметка. а не заметки.
data class Notes(
    @PrimaryKey(autoGenerate = true) var noteId: Int?,
    // FIXME лучше позже и добавить. да и вообще откуда в мобильном приложении то userId в базе данных
    //  окажется?) юзер самого приложения же один, это на сервере много юзеров
    var userId: String? = null, //на будущее для разделения по юзерам
    var noteName: String,
    var noteText: String,
    // FIXME мы не можем полагаться на ссылки до изображений вне нашего приложения - они в любой
    //  момент могут стать мертвыми (юзер просто удалит изображение в галерее например).
    //  мы должны здесь хранить путь до файла в данных нашего же приложения - в его dataDir.
    //  оттуда никто не сможет удалить файлы кроме нашего же приложения (или очистки всех данных
    //  нашего приложения)
    //  и при выборе изображения мы должны записывать копию его себе в dataDir а в базу писать имя
    //  этого файла в нашей dataDir
    var image: String? = null, //TODO: media.Image может быть заменить на путь к файлу локально
    var createDate: Date,
    var modifiedDate: Date? = null,
    // FIXME не стоит вписывать в сущность заметки факт "выбрана ли она на конкретном экране" - надо
    //  просто во вьюмодели соответствующего экрана держать список айдишников выбранных заметок.
    //  выбор это чисто ui детали, которые ниже viewmodel спуститься не могут
    @Ignore var selected: Boolean = false
) : Parcelable {
    // FIXME почему не использованы дефолтные аргументы датакласса, зачем отдельный конструктор?
    //  я вижу что лучше было просто для noteId тоже null по умолчанию указать
    //  это избавит от поддержки еще и конструктора дополнительного
    constructor (noteName: String, noteText: String, image: String?, createDate: Date) : this(
        null,
        null,
        noteName = noteName,
        noteText = noteText,
        image = image,
        createDate = createDate
    )
}