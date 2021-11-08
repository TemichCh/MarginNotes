package com.example.notesdemo.model

import android.media.Image
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.notesdemo.DAO.Converters
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity(tableName = "Notes")
@Parcelize
data class Notes(
    @PrimaryKey(autoGenerate = true)
    var noteId: Int? = null,
    val userId: String? = null, //на будущее для разделения по юзерам
    val noteName: String,
    val noteText: String,
    val image: String? = null, //TODO: media.Image может быть заменить на путь к файлу локально
    val createDate: Date,
    var modifiedDate: Date? = null
) : Parcelable /*{
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString(),
        Date(parcel.readLong()),
        Date(parcel.readLong())
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(noteId)
        parcel.writeString(userId)
        parcel.writeString(noteName)
        parcel.writeString(noteText)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Notes> {
        override fun createFromParcel(parcel: Parcel): Notes {
            return Notes(parcel)
        }

        override fun newArray(size: Int): Array<Notes?> {
            return arrayOfNulls(size)
        }
    }
}*/