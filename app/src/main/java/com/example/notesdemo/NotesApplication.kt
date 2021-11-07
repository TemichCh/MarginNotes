package com.example.notesdemo

import android.app.Application
import com.example.notesdemo.DAO.NotesLocalDb
import com.example.notesdemo.DAO.NotesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class NotesApplication:Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    private val database by lazy { NotesLocalDb.getInstance(this,applicationScope) }
    val repository by lazy { NotesRepository(database.LocalNotesDao()) }


}