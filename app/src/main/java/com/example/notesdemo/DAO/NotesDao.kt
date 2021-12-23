package com.example.notesdemo.dao

import androidx.room.*
import com.example.notesdemo.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    // FIXME некорректное имя - даже IDE пишет что не соответствует кодстайлу
    //fixed?
    @Insert
    suspend fun insertNote(note:Note)

    // FIXME не можем мы получать MutableList - не мутабельный мы получаем, List просто
    //fixed?
    @Query("Select * from notes")
    fun gelAllNotes(): Flow<List<Note>>

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    // FIXME не можем мы получать MutableList - не мутабельный мы получаем, List просто
    //fixed?
    @Query("Select * from notes where noteName like '%' || :search || '%'")
    fun searchNotes(search:String): Flow<List<Note>>

    @Query("select * from notes where noteId = :noteId")
    fun getNoteById(noteId:Int):Flow<Note>
}