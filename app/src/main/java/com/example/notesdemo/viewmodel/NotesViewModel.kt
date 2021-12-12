package com.example.notesdemo.viewmodel

import androidx.lifecycle.*
import com.example.notesdemo.DAO.NotesRepository
import com.example.notesdemo.model.Note
import kotlinx.coroutines.launch

class NotesViewModel(private val notesRep: NotesRepository) : ViewModel() {
    val searchQuery = MutableLiveData("")

    val allNotes: LiveData<List<Note>> =
        Transformations.switchMap(searchQuery) { searchText ->
            if (searchText.isNullOrEmpty()) {
                notesRep.allNotes.asLiveData()
            } else {
                notesRep.searchNotes(searchText).asLiveData()
            }
        }

    fun insert(note: Note) = viewModelScope.launch {
        notesRep.insertNote(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        notesRep.deleteNote(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        notesRep.updateNote(note)
    }

    // FIXME не нужен сам метод, как выше в комменте к лайвдате сказано. и также не нужно делать
    //  изменение значения лайвдаты внутри скоупа - корутина тут не нужна
    fun handleSearchQuery(text: String) = viewModelScope.launch {
        searchQuery.value = text
    }
}

// FIXME вместо фиксированной фабрики можно сделать класс общего назначения (с Generic), который
//  принимает лямбду создающую нужный тип вьюмодели и тогда использование будет
//  LambdaViewModelFactory { NotesViewModel(notesRepo) }
//  и из этого файла данную фабрику надо будет вынести
class NotesViewModelFactory(private val notesRep: NotesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotesViewModel(notesRep) as T
        }
        throw IllegalArgumentException("Unknown VieModel Class")
    }
}
