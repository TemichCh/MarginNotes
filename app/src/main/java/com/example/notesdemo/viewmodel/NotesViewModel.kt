package com.example.notesdemo.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.example.notesdemo.CreateOrEditNoteActivity
import com.example.notesdemo.DAO.NotesRepository
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
    val selectedNotes = MutableLiveData<MutableList<Int>>()

    val selectionMode: MutableLiveData<Boolean> = Transformations.map(selectedNotes) {
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


    fun delete(note: Note) = viewModelScope.launch {
        notesRep.deleteNote(note)
    }


    fun onNotePressed(context: Context, noteId: Int) {
        if (selectionMode.value != true) {
            context.startActivity(CreateOrEditNoteActivity.createIntent(context, noteId))
        } else {
            if (selectedNotes.value?.contains(noteId) == true) {
                selectedNotes.value?.remove(noteId)
            } else {
                selectedNotes.value?.add(noteId)
            }
        }
    }

    fun onNoteLongClick(context: Context, noteId: Int) {
        selectionMode.value = true
        onNotePressed(context, noteId)
    }

    fun deselectAllItems() {
        selectedNotes.value?.clear()
        selectionMode.value = false
    }

}

// FIXME вместо фиксированной фабрики можно сделать класс общего назначения (с Generic), который
//  принимает лямбду создающую нужный тип вьюмодели и тогда использование будет
//  LambdaViewModelFactory { NotesViewModel(notesRepo) }
//  и из этого файла данную фабрику надо будет вынести
//class NotesViewModelFactory(private val notesRep: NotesRepository) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return NotesViewModel(notesRep) as T
//        }
//        throw IllegalArgumentException("Unknown VieModel Class")
//    }
//}
