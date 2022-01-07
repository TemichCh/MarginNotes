package com.example.notesdemo

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notesdemo.adapters.NotesListAdapter
import com.example.notesdemo.databinding.ActivityMainBinding
import com.example.notesdemo.utils.NoteItemDetailsLookup
import com.example.notesdemo.viewmodel.NotesViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var tracker: SelectionTracker<Long>? = null

    // это свойство тоже не должно быть публичным. это детали реализации конкретно этого активити
    private val notesVModel: NotesViewModel by viewModels {
        val repository = application.getNotesRepository()
        return@viewModels ViewModelFactory(repository, this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // это свойство точно не должно быть публичным. никто снаружи этого класса не должен знать
        //  об этой детали реализации. А также ссылка на adapter нужна нам только в течении создания view
        //  в onCreate, и только там нам надо создать адаптер, прицепить его к recyclerView и указать
        //  логику заполнения данных
        val adapter = NotesListAdapter()

        setSupportActionBar(binding.mainActivityToolbar)

        binding.recyclerviewNotes.layoutManager = LinearLayoutManager(this)
        binding.recyclerviewNotes.adapter = adapter

        notesVModel.allNotes.observe(this) { notes ->
            notes.let { adapter.setNotes(it) }
        }

        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.recyclerviewNotes.addItemDecoration(divider)

        adapter.setOnNoteTapListener {
            val noteId = it.noteId
            if (noteId != null) notesVModel.onNotePressed(this, noteId)
        }

        tracker = SelectionTracker.Builder<Long>(
            "NotesListTracker",
            binding.recyclerviewNotes,
            StableIdKeyProvider(binding.recyclerviewNotes),
            NoteItemDetailsLookup(binding.recyclerviewNotes),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()
        adapter.tracker = tracker

        /*
         adapter.setOnNoteTapListener { note ->

             // FIXME как я уже говорил выше лучше логику выбора вынести в viewmodel.
             //  и тогда тут у тебя будет просто вызов обработчика вьюмодели onNotePressed
             //  а уже viewModel будет определять надо ли добавить новую заметку в выбранные,
             //  или же надо пойти на другой экран. В случае если надо пойти на другой экран просто
             //  пульнет в Flow с событиями соответствующее событие `OpenNote(noteId)`
             //  https://proandroiddev.com/android-singleliveevent-redux-with-kotlin-flow-b755c70bb055
*/

        //  когда подключишь ViewBinding надо заменить на типобезопасное получение вьюхи
        //fixed ?
        val fab = binding.fabMainAddNote
        fab.setOnClickListener {
            startActivity(CreateOrEditNoteActivity.createIntent(this@MainActivity, 0))
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        //  вместо сохранения в свойства класса активити этих элементов - надо просто здась
        //  получить эти элементы и подписаться на изменение лайвдаты в viewModel, в зависимости от
        //  которой скрывать или показывать эти элементы
        val deleteItem = menu.findItem(R.id.action_delete)
        val searchItem = menu.findItem(R.id.action_search)

        //  эта пачка логики должна уйти в вьюмодель и по сути делать следующее:
        //  лайвдата selectionMode включается в true, в лайвдату selectedNotes добавляем id выбранного наим элемента
        //  а уже на основе этих лайвдат у нас должен перестраиваться UI, за счет поставленных в onCreate observe
        // Можно считать решением?
        tracker?.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    val count = tracker?.selection?.size() ?: 0
                    val isEdit = (count > 0)
                    deleteItem.isVisible = isEdit
                    searchItem.isVisible = ! isEdit
                    supportActionBar !!.setDisplayHomeAsUpEnabled(isEdit)
                    title = if (isEdit) {
                        count.toString()
                    } else {
                        resources.getString(R.string.app_name)
                    }
                    val list = tracker?.selection?.map { it.toInt() }
                    notesVModel.selectedNotes.value = list
                }
            }
        )

        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = getString(R.string.searchQueryHint)

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                notesVModel.searchQuery.value = query
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                notesVModel.searchQuery.value = newText
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            tracker?.clearSelection()
            true
        }
        R.id.action_delete -> {
            notesVModel.deleteSelected()
            tracker?.clearSelection()
            true
        }
        else ->
            super.onOptionsItemSelected(item)
    }
}