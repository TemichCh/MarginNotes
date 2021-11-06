package com.example.notesdemo.DAO

import androidx.room.*
import com.example.notesdemo.model.Notes

@Dao
interface NotesDao {
    @Insert
    fun InsertNote(note:Notes)

    @Query("Select * from notes")
    fun gelAllNotes(): List<Notes>

    @Update
    fun updateNote(note: Notes)

    @Delete
    fun deleteNote(note: Notes)
}