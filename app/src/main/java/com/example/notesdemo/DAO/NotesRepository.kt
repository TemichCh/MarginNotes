// FIXME имя пакета не соответствует кодстайлу
package com.example.notesdemo.DAO

import androidx.annotation.WorkerThread
import com.example.notesdemo.model.Notes
import kotlinx.coroutines.flow.Flow

class NotesRepository(private val db: NotesDao) {

    // FIXME выдаем в Flow не мутабельные списки
    //fixed?
    val allNotes:Flow<List<Notes>> = db.gelAllNotes()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertNote(note:Notes){
        db.insertNote(note)
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
    //fixed? MutableLIst изменяемый, поэтому добавление/удаление строк не "фиксируется" т.е. при сравнении объект equals предыдущему
    // в отличии от List. При пересоздании это будет уже новый объект != предыдущему т.к. объект пересоздан
    fun searchNotes(text:String):Flow<List<Notes>> = db.searchNotes(text)

}