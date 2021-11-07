package com.example.notesdemo.DAO

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.notesdemo.model.Notes
import kotlinx.coroutines.CoroutineScope


@Database(entities = [Notes::class], version = 1)
@TypeConverters(Converters::class)
abstract class NotesLocalDb : RoomDatabase() {

    abstract fun LocalNotesDao(): NotesDao

    companion object {
        private var INSTANCE: NotesLocalDb? = null

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
    }
}
