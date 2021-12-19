// FIXME имя пакета не соответствует кодстайлу
package com.example.notesdemo.DAO

import androidx.annotation.WorkerThread
import com.example.notesdemo.model.Note
import kotlinx.coroutines.flow.Flow

class NotesRepository(private val db: NotesDao) {

    // FIXME выдаем в Flow не мутабельные списки
    //fixed?
    val allNotes:Flow<List<Note>> = db.gelAllNotes()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertNote(note:Note){
        db.insertNote(note)
    }

    suspend fun deleteNote(note: Note) {
        db.deleteNote(note)

    }

    suspend fun updateNote(note: Note) {
        db.updateNote(note)
    }

    // FIXME выдаем в Flow не мутабельные списки
    //  https://dev.to/zachklipp/two-mutables-dont-make-a-right-2kgp тут кейс немного другой но
    //  проблема мутабельности таже
    //fixed? MutableLIst изменяемый, поэтому добавление/удаление строк не "фиксируется" т.е. при сравнении объект equals предыдущему
    // в отличии от List. При пересоздании это будет уже новый объект != предыдущему т.к. объект пересоздан
    fun searchNotes(text: String): Flow<List<Note>> = db.searchNotes(text)

    fun getNoteById(noteId: Int): Flow<Note> {
        val noteById = db.getNoteById(noteId)
        return noteById
    }

}