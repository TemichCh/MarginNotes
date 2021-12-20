package com.example.notesdemo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.notesdemo.DAO.NotesRepository
import com.example.notesdemo.model.Note
import kotlinx.coroutines.launch
import java.util.*

class CreateOrEditViewModel(private val notesRep: NotesRepository) : ViewModel() {

    private var noteId: Int? = null

    val noteName = MutableLiveData<String>()
    val noteText = MutableLiveData<String>()
    val noteImage = MutableLiveData<String?>()
    val createDate = MutableLiveData<Date>()
    val modifiedDate = MutableLiveData<Date?>()


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
            val note = notesRep.getNoteById(noteId).asLiveData().value
            if (note != null) onNoteLoaded(note) else onDataNotAvailable()
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
        createDate.value = note.createDate
        modifiedDate.value = note.modifiedDate
        isNoteLoaded = true
    }


    fun saveNote() {
        val currentName = noteName.value
        val currenText = noteText.value
        val currCreateDate = createDate.value

        if (currentName == null || currenText == null) {
            return
        }


        val currentNoteId = noteId
        if (isNewNote && currentNoteId == null) {
            insertNote(Note(noteName = currentName, noteText = currenText, createDate = Date()))
        } else {
            val note = Note(
                noteId = currentNoteId,
                noteName = currentName,
                noteText = currenText,
                createDate = currCreateDate?:Date(), //не факт, надо подумать
                modifiedDate = Date()
            )
            updateNote(note)
        }
    }

    fun insertNote(note: Note) = viewModelScope.launch {
        notesRep.insertNote(note)
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        notesRep.updateNote(note)
    }
}