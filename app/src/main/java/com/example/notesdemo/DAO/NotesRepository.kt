package com.example.notesdemo.DAO

import androidx.annotation.WorkerThread
import com.example.notesdemo.model.Notes
import kotlinx.coroutines.flow.Flow

class NotesRepository(private val db: NotesDao) {

    val allNotes:Flow<MutableList<Notes>> = db.gelAllNotes()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertNote(note:Notes){
        db.InsertNote(note)
    }

    suspend fun deleteNote(note: Notes) {
        db.deleteNote(note)

    }

    suspend fun updateNote(note: Notes) {
        db.updateNote(note)
    }

    fun searchNotes(text:String):Flow<MutableList<Notes>> = db.searchNotes(text)

}