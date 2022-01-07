package com.example.notesdemo.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.example.notesdemo.CreateOrEditNoteActivity
import com.example.notesdemo.dao.NotesRepository
import com.example.notesdemo.model.Note
import kotlinx.coroutines.launch


class NotesViewModel(private val notesRep: NotesRepository) : ViewModel() {

    /*
    private val _openNoteEvent = MutableLiveData<EventListener>()

    val openNoteEvent: LiveData<EventListener> = _openNoteEvent

    private val channel = Channel<String>(Channel.BUFFERED)
    val flow = channel.receiveAsFlow()
*/
    val searchQuery = MutableLiveData("")
    val selectedNotes = MutableLiveData<List<Int>>()

    val selectionMode: MutableLiveData<Boolean> = Transformations.map(selectedNotes) {
        // Наверно это лишнее.
        it.isNotEmpty()
    } as MutableLiveData<Boolean> //MutableLiveData(false)


    val allNotes: LiveData<List<Note>> =
        Transformations.switchMap(searchQuery) { searchText ->
            if (searchText.isNullOrEmpty()) {
                notesRep.allNotes.asLiveData()
            } else {
                notesRep.searchNotes(searchText).asLiveData()
            }
        }

    fun deleteSelected() = viewModelScope.launch {
        selectedNotes.value?.forEach { noteId ->
            notesRep.deleteNoteById(noteId)
        }
        selectionMode.value = false
    }

    fun onNotePressed(context: Context, noteId: Int) {
        if (selectionMode.value != true) {
            context.startActivity(CreateOrEditNoteActivity.createIntent(context, noteId))
        }
    }
}
