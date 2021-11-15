package com.example.notesdemo.veiwmodel

import androidx.lifecycle.*
import com.example.notesdemo.DAO.NotesRepository
import com.example.notesdemo.model.Notes
import kotlinx.coroutines.launch

class NotesViewModel(private val notesRep: NotesRepository) : ViewModel() {

    private val searchQuery = MutableLiveData<String>("")

    val allNotes: LiveData<MutableList<Notes>> =
        Transformations.switchMap(searchQuery) { searchText ->
            if (searchText.isNullOrEmpty())
                notesRep.allNotes.asLiveData()
            else notesRep.searchNotes(searchText).asLiveData()
        }

    fun insert(note: Notes) = viewModelScope.launch {
        notesRep.insertNote(note)
    }

    fun delete(note: Notes) = viewModelScope.launch {
        notesRep.deleteNote(note)
    }

    fun update(note: Notes) = viewModelScope.launch {
        notesRep.updateNote(note)
    }

    fun handleSearchQuery(text: String) = viewModelScope.launch {
        searchQuery.value = text
    }
}

class NotesViewModelFactory(private val notesRep: NotesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotesViewModel(notesRep) as T
        }
        throw IllegalArgumentException("Unknown VieModel Class")
    }
}
