package com.example.notesdemo.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.notesdemo.model.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executors

//Попутал, во второй версии выпилен userId из таблицы, поэтому надо пересоздавать
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("alter table notes rename to notes_backup")
            execSQL(
                "CREATE TABLE IF NOT EXISTS notes (noteId INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "noteName TEXT NOT NULL, " +
                        "noteText TEXT NOT NULL, " +
                        "image TEXT, " +
                        "createDate INTEGER NOT NULL," +
                        "modifiedDate INTEGER)"
            )
            execSQL(
                "insert into notes(noteId,noteName,noteText,image,createDate,modifiedDate)" +
                        "select noteId,noteName,noteText,image,createDate,modifiedDate from notes_backup "
            )
            execSQL("drop table notes_backup")
        }
    }


}


@Database(
    entities = [Note::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class NotesLocalDb : RoomDatabase() {

    abstract fun localNotesDao(): NotesDao

    companion object {
        var INSTANCE: NotesLocalDb? = null

        fun getInstance(context: Context, scope: CoroutineScope): NotesLocalDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotesLocalDb::class.java, "notesLocal.db"
                ).addCallback(NotesItemsCallback(scope))
                    .addMigrations(MIGRATION_1_2)
                    .setQueryCallback({ sqlQuery, bindArgs ->
                        logQueryText(sqlQuery, bindArgs)
                    }, Executors.newSingleThreadExecutor())
                    .build()

                INSTANCE = instance
                instance
            }

        }

        private fun logQueryText(sqlQuery: String, bindArgs: MutableList<Any>) {
            println("SQL Query: $sqlQuery SQL Args: $bindArgs")
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}

private class NotesItemsCallback(val scope: CoroutineScope) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val initialNote = createInitialNote()
        //  выглядит хаком и ненадежно.
        //  задача пред-заполнения базы данных решается иначе, хоть и не так удобно (без дао)
        //  https://developer.android.com/training/data-storage/room/prepopulate
        //  я бы поискал более надежное и аккуратно выглядящее решение для заполнения данными через dao
        // По ссылке выше описание подключения уже имеющейся БД из внешнего файла
        // В данном случае задача просто добавить приветствие при создании БД, хотя не знаю насколько это актуально, может устарело
        // https://medium.com/androiddevelopers/7-pro-tips-for-room-fbadea4bfbd1#4785
        NotesLocalDb.INSTANCE?.let {
            scope.launch {
                it.localNotesDao().insertNote(initialNote)
            }
        }

    }

    private fun createInitialNote(): Note {
        val initialNote = Note(
            noteName = "Добро пожаловать!", noteText = """
                Перед Вами простое приложение для создания заметок.
                Это могут быть различные записи которые Вы хотели бы сохранить в Вашем телефоне.
                Записи могут содержать "Заголовок", "Текст" и "Изображение" из Вашей галереи.
                К сожаления т.к. это пока первая версия программы данные могут храниться только локально, на Вашем телефоне.
                Если приложение получит поддержку и будет востребованно оно обзаведётся различными возможностями по хранению данных 
                и их переносу в сеть между Вашими устройствами.
                Желаю приятной работы.
            """.trimIndent(), image = null, createDate = Date()
        )
        return initialNote
    }
}
