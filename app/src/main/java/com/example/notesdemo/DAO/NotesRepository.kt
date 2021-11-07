package com.example.notesdemo.DAO

import android.content.Context
import androidx.annotation.WorkerThread
import com.example.notesdemo.model.Notes
import kotlinx.coroutines.flow.Flow

class NotesRepository(private val db: NotesDao) {

    //var db:NotesDao = NotesLocalDb.getInstance(context)?.LocalNotesDao()!!

    val allNotes:Flow<MutableList<Notes>> = db.gelAllNotes()


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertNote(note:Notes){
        db.InsertNote(note)
    }
}