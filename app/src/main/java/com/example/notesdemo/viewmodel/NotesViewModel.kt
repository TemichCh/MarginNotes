package com.example.notesdemo.viewmodel

import androidx.lifecycle.*
import com.example.notesdemo.DAO.NotesRepository
import com.example.notesdemo.model.Notes
import kotlinx.coroutines.launch

class NotesViewModel(private val notesRep: NotesRepository) : ViewModel() {
    // FIXME эта лайвдата вполне может быть публичной и ее значение будет менять поле ввода поиска.
    //  вместо вызова handleSearchQuery
    private val searchQuery = MutableLiveData<String>("")

    // FIXME на выходе мы получать должны лайвдату с немутабельным списком.
    val allNotes: LiveData<MutableList<Notes>> =
        Transformations.switchMap(searchQuery) { searchText ->
            // FIXME условия без фигурных скобок допускаются только если в одну строку влезают.
            //  как только на следующую строку уходят - надо ставить скобки чтобы обезопасить себя
            //  от ошибок когда добавишь какую либо строку / логгер или еще чего и логика условия
            //  подействует не на то выражение
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
