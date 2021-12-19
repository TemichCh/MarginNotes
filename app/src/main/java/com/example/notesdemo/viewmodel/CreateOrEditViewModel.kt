package com.example.notesdemo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesdemo.DAO.NotesRepository
import com.example.notesdemo.model.Note
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CreateOrEditViewModel(private val notesRep: NotesRepository) : ViewModel() {

    private var noteId: Int? = null

    val noteName = MutableLiveData<String>()
    val noteText = MutableLiveData<String>()
    val noteImage = MutableLiveData<String?>()

    private var isNewNote = true

    private var isNoteLoaded = false

    fun load(noteId: Int) {
        this.noteId = noteId
        if (noteId == 0) {
            isNewNote = true
            return
        }
        if (isNoteLoaded) {
            return
        }

        isNewNote = false

        viewModelScope.launch {
            notesRep.getNoteById(noteId).map { note ->
                if (note != null) onNoteLoaded(note) else onDataNotAvailable()
            }

        }
    }

    private fun onDataNotAvailable() {
        //Toast.makeText(, s, Toast.LENGTH_LONG).show()
        println("****** no data available")
    }


    private fun onNoteLoaded(note: Note) {
        noteName.value = note.noteName
        noteText.value = note.noteText
        noteImage.value = note.image
        isNoteLoaded = true
    }


    fun insert(note: Note) = viewModelScope.launch {
        notesRep.insertNote(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        notesRep.updateNote(note)
    }
}