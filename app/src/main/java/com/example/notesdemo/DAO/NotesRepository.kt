// FIXME имя пакета не соответствует кодстайлу
package com.example.notesdemo.DAO

import androidx.annotation.WorkerThread
import com.example.notesdemo.model.Notes
import kotlinx.coroutines.flow.Flow

class NotesRepository(private val db: NotesDao) {

    // FIXME выдаем в Flow не мутабельные списки
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

    // FIXME выдаем в Flow не мутабельные списки
    //  https://dev.to/zachklipp/two-mutables-dont-make-a-right-2kgp тут кейс немного другой но
    //  проблема мутабельности таже
    fun searchNotes(text:String):Flow<MutableList<Notes>> = db.searchNotes(text)

}