package com.example.notesdemo.DAO

import androidx.room.*
import com.example.notesdemo.model.Notes
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    @Insert
    suspend fun InsertNote(note:Notes)

    @Query("Select * from notes")
    fun gelAllNotes(): Flow<MutableList<Notes>>

    @Update
    suspend fun updateNote(note: Notes)

    @Delete
    suspend fun deleteNote(note: Notes)

    @Query("Select * from notes where noteName like '%' || :search || '%'")
    fun searchNotes(search:String): Flow<MutableList<Notes>>
}