package com.example.notesdemo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesdemo.dao.NotesRepository
import com.example.notesdemo.model.Note
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class CreateOrEditViewModel(private val notesRep: NotesRepository) : ViewModel() {

    private var _noteId: Int = 0

    val isEditMode = MutableLiveData(false)
    val noteName = MutableLiveData<String>()
    val noteText = MutableLiveData<String>()
    val noteImage = MutableLiveData<String?>(null)
    //??? Первоначально хотел сохранить картинку в stream и в момент записи в БД уже сохранять
    // в файл, но тут тогда тоже нужен context наверно, не смог достучаться до filesDir
    // val imageStream = MutableLiveData<ByteArray?>(null)
    val createDate = MutableLiveData<Date>()
    val modifiedDate = MutableLiveData<Date?>()


    private var isNewNote = true

    private var isNoteLoaded = false

    fun load(noteId: Int) {
        this._noteId = noteId
        if (noteId == 0) {
            isNewNote = true
            isEditMode.value = true
            return
        }
        if (isNoteLoaded) {
            return
        }

        isNewNote = false

        viewModelScope.launch {
            notesRep.getNoteById(noteId).collect { note ->
                onNoteLoaded(note)
            }
        }
    }

    /* private fun onDataNotAvailable() {
         //Toast.makeText(it,, Toast.LENGTH_LONG).show()
         println("****** no data available")
     }*/


    private fun onNoteLoaded(note: Note) {
        noteName.value = note.noteName
        noteText.value = note.noteText
        noteImage.value = note.image
        /*if (note.image != null) {
            val imageFile = File(note.image)
            imageStream.value = imageFile.readBytes()
        }
        */
        createDate.value = note.createDate
        modifiedDate.value = note.modifiedDate
        isNoteLoaded = true
    }


    fun saveNote():Boolean  {
        if (isEditMode.value == false){
            isEditMode.value=true
            return false
        }

        val currentName = noteName.value
        val currenText = noteText.value
        val currCreateDate = createDate.value
        val currImageFile = noteImage.value

        if (currentName == null || currenText == null) {
            return false
        }

        val currentNoteId = _noteId
        if (isNewNote && currentNoteId == 0) {
            insertNote(
                Note(
                    noteName = currentName,
                    noteText = currenText,
                    image = currImageFile,
                    createDate = Date()
                )
            )
        } else {
            val note = Note(
                noteId = currentNoteId,
                noteName = currentName,
                noteText = currenText,
                image = currImageFile,
                createDate = currCreateDate ?: Date(), //не факт, надо подумать
                modifiedDate = Date()
            )
            updateNote(note)
        }
        return true
    }

    fun deleteOrCancelEdit() {
        when {
            (isEditMode.value == false && ! isNewNote) -> {
                deleteNoteFile()
                deleteNote(_noteId)
            }
            (isEditMode.value == true && ! isNewNote) -> {
                isNoteLoaded = false
                load(_noteId)
                isEditMode.value = false
            }
            (isEditMode.value == true && isNewNote && ! noteImage.value.isNullOrEmpty()) -> {
                deleteNoteFile()
                isEditMode.value = false
            }
        }
    }

    fun insertNote(note: Note) = viewModelScope.launch {
        notesRep.insertNote(note)
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        notesRep.updateNote(note)
    }

    fun deleteNote(noteId: Int) = viewModelScope.launch {
        notesRep.deleteNoteById(noteId)
    }

    fun deleteNoteFile() {
        val fileName = noteImage.value
        if (fileName.isNullOrEmpty()) return
        //viewModelScope.launch {
        //TODO надо подумать: удаление картинки идет в разрез с кнопкой отмены изменений
        val noteFile = File(fileName)
        val deleted = noteFile.delete()
        if (deleted) {
            noteImage.value = ""
        }
    }
}
