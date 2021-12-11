// FIXME имя пакета не соответствует кодстайлу
package com.example.notesdemo.DAO

import androidx.room.*
import com.example.notesdemo.model.Notes
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    // FIXME некорректное имя - даже IDE пишет что не соответствует кодстайлу
    //fixed?
    @Insert
    suspend fun insertNote(note:Notes)

    // FIXME не можем мы получать MutableList - не мутабельный мы получаем, List просто
    //fixed?
    @Query("Select * from notes")
    fun gelAllNotes(): Flow<List<Notes>>

    @Update
    suspend fun updateNote(note: Notes)

    @Delete
    suspend fun deleteNote(note: Notes)

    // FIXME не можем мы получать MutableList - не мутабельный мы получаем, List просто
    //fixed?
    @Query("Select * from notes where noteName like '%' || :search || '%'")
    fun searchNotes(search:String): Flow<List<Notes>>
}