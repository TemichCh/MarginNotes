package com.example.notesdemo.DAO

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notesdemo.model.Notes

@Database(entities = [Notes::class], version = 1)
abstract class NotesLocalDb: RoomDatabase() {
    abstract fun LocalNotesDao():NotesDao
}