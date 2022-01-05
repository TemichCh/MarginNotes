package com.example.notesdemo.dao

import androidx.room.*
import com.example.notesdemo.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    @Insert
    suspend fun insertNote(note:Note)

    @Query("Select * from notes")
    fun gelAllNotes(): Flow<List<Note>>

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("Select * from notes where noteName like '%' || :search || '%'")
    fun searchNotes(search:String): Flow<List<Note>>

    @Query("select * from notes where noteId = :noteId")
    fun getNoteById(noteId:Int):Flow<Note>

    @Query("delete from notes where noteId = :noteId")
    suspend fun deleteNoteById(noteId:Int)
}