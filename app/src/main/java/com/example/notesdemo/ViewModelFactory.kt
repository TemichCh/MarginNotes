package com.example.notesdemo

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.example.notesdemo.dao.NotesRepository
import com.example.notesdemo.viewmodel.CreateOrEditViewModel
import com.example.notesdemo.viewmodel.NotesViewModel

class ViewModelFactory constructor(
    private val notesRepository: NotesRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) :
    AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ) = with(modelClass) {
        when {
            isAssignableFrom(NotesViewModel::class.java) -> NotesViewModel(notesRepository)
            isAssignableFrom(CreateOrEditViewModel::class.java)->CreateOrEditViewModel(notesRepository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T

}
