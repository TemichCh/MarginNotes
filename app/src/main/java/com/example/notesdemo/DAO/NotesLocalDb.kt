package com.example.notesdemo.DAO

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.notesdemo.model.Notes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors


@Database(entities = [Notes::class], version = 1)
@TypeConverters(Converters::class)
abstract class NotesLocalDb : RoomDatabase() {

    // FIXME нейминг невалидный - надо исправить в соответствии с кодстайлом kotlin
    abstract fun LocalNotesDao(): NotesDao

    companion object {
        var INSTANCE: NotesLocalDb? = null

        fun getInstance(context: Context, scope: CoroutineScope): NotesLocalDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotesLocalDb::class.java, "notesLocal.db"
                ).addCallback(NotesItemsCallback(scope))
                    .build()

                INSTANCE = instance
                instance
            }


        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}

private class NotesItemsCallback(val scope: CoroutineScope) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val initialNote = Notes(noteName = "Добро пожаловать!",noteText = """
            Перед Вами простое приложение для создания заметок.
            Это могут быть различные записи которые Вы хотели бы сохранить в Вашем телефоне.
            Записи могут содержать "Заголовок", "Текст" и "Изображение" из Вашей галереи.
            К сожаления т.к. это пока первая версия программы данные могут храниться только локально, на Вашем телефоне.
            Если приложение получит поддержку и будет востребованно оно обзаведётся различными возможностями по хранению данных 
            и их переносу в сеть между Вашими устройствами.
            Желаю приятной работы.
        """.trimIndent(), null ,Date())
        Executors.newSingleThreadExecutor().execute {
            NotesLocalDb.INSTANCE?.let {
                scope.launch {
                    it.LocalNotesDao().InsertNote(initialNote)
                }
            }
        }

    }
}
