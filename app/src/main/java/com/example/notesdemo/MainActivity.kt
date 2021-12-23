package com.example.notesdemo

import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notesdemo.adapters.NotesListAdapter
import com.example.notesdemo.databinding.ActivityMainBinding
import com.example.notesdemo.viewmodel.NotesViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // FIXME это свойство точно не должно быть публичным. никто снаружи этого класса не должен знать
    //  об этой детали реализации. А также ссылка на adapter нужна нам только в течении создания view
    //  в onCreate, и только там нам надо создать адаптер, прицепить его к recyclerView и указать
    //  логику заполнения данных
    private val adapter = NotesListAdapter()

    // FIXME это свойство тоже не должно быть публичным. это детали реализации конкретно этого активити
    private val notesVModel: NotesViewModel by viewModels {
        val repository = application.getNotesRepository()
        return@viewModels ViewModelFactory(repository, this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mainActivityToolbar)

        binding.recyclerviewNotes.layoutManager = LinearLayoutManager(this)
        binding.recyclerviewNotes.adapter = adapter

        notesVModel.allNotes.observe(this) { notes ->
            notes.let { adapter.setNotes(it) }
        }

        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.recyclerviewNotes.addItemDecoration(divider)

        //test
        /*lifecycle.addObserver(LifecycleEventObserver { source: LifecycleOwner, event: Lifecycle.Event ->
            notesVModel.onNotePressed(event.name)
        })*/

        adapter.setOnNoteTapListener {
            val noteId = it.noteId
            if (noteId != null) notesVModel.onNotePressed(this, noteId)
        }

        adapter.onNoteLongClickListener {
            val noteId = it.noteId
            if (noteId != null) notesVModel.onNoteLongClick(this, noteId)
        }


        /*
         adapter.setOnNoteTapListener { note ->

             // FIXME как я уже говорил выше лучше логику выбора вынести в viewmodel.
             //  и тогда тут у тебя будет просто вызов обработчика вьюмодели onNotePressed
             //  а уже viewModel будет определять надо ли добавить новую заметку в выбранные,
             //  или же надо пойти на другой экран. В случае если надо пойти на другой экран просто
             //  пульнет в Flow с событиями соответствующее событие `OpenNote(noteId)`
             //  https://proandroiddev.com/android-singleliveevent-redux-with-kotlin-flow-b755c70bb055
             if (!notesVModel.selectionMode.value?:true) {
                 // FIXME логика формирования опций для открытия экрана должна быть вынесена в
                 //  отдельную функцию, что позволит реюзать эти опции
                 val bundle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                     ActivityOptions.makeSceneTransitionAnimation(
                         this,
                         binding.recyclerviewNotes.findViewById(R.id.iv_has_image),
                         getString(R.string.transition_image_view)
                     ).toBundle()
                 } else {
                     ActivityOptionsCompat.makeCustomAnimation(this, 0, 0).toBundle()
                 }
                 // FIXME вся логика формирования правильного Intent должна быть не в месте открытия
                 //  активити, а в companion object в открываемой активити. пример там оформил
                 //  createIntent(context: Context, note: Notes): Intent
                 //  таким образом мы даем возможность легко создавать интент для открытия экрана
                 //  откуда угодно, не дублируя код и не показывая внутрянку активити другим классам
                 val intent = Intent(this@MainActivity, CreateOrEditNoteActivity::class.java)
                 // FIXME передавать между экранами прям объект заметки - оверхед.
                 //  лучше передавать id заметки, а целевой экран без пробелм считает с базы данных
                 //  эту заметку по ее id
                 intent.putExtra("noteId", note.noteId)
                 // FIXME IDE подсказывает что такой метод недоступен на API 14, которая заявлена как
                 //  поддерживаемая
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                     startActivity(intent, bundle)
                 } else {
                     //TODO обработка на API 14
                 }

             } else {
                 startSelection(note)
             }
         }

         adapter.onNoteLongClickListener { note ->
             // FIXME обработкой лонг клика тоже заниматься вьюмодели стоит. она включит режим выбора,
             //  добавит в выбранные эелменты заметку которую жмем
             startSelection(note)
         }*/


        // FIXME когда подключишь ViewBinding надо заменить на типобезопасное получение вьюхи
        //fixed ?
        val fab = binding.fabMainAddNote
        fab.setOnClickListener {
            startActivity(CreateOrEditNoteActivity.createIntent(this@MainActivity, 0))
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        // FIXME вместо сохранения в свойства класса активити этих элементов - надо просто здась
        //  получить эти элементы и подписаться на изменение лайвдаты в viewModel, в зависимости от
        //  которой скрывать или показывать эти элементы
        val deleteItem = menu.findItem(R.id.action_delete)
        val searchItem = menu.findItem(R.id.action_search)

        notesVModel.selectionMode.observe(this) {
            title = if (it) {
                notesVModel.selectedNotes.value?.size.toString()
            } else {
                resources.getString(R.string.app_name)
            }
            deleteItem.isVisible = it
            searchItem.isVisible = !it
            supportActionBar!!.setDisplayHomeAsUpEnabled(it)
        }


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

    // FIXME эта пачка логики должна уйти в вьюмодель и по сути делать следующее:
    //  лайвдата selectionMode включается в true, в лайвдату selectedNotes добавляем id выбранного наим элемента
    //  а уже на основе этих лайвдат у нас должен перестраиваться UI, за счет поставленных в onCreate observe
/* private fun startSelection(note: Note) {
     note.selected = !note.selected
     val index = adapter.notesList.indexOf(note)
     adapter.notifyItemChanged(index)
     if (adapter.notesList.none { noteSel ->
             noteSel.selected
         }) {
         setDefaultToolbar()
         selectionModeEnabled = false
     } else {
         setDeleteToolBar()
     }
 }*/

    // FIXME это логика вьюмодели уже, она должна менять данные а UI просто обновится после
    //  получения новой инфы
/*private fun deselectAllNotes() {
    adapter.notesList.forEachIndexed { position, note ->
        if (note.selected) {
            val index = adapter.notesList.indexOf(note)
            note.selected = false
            adapter.notifyItemChanged(index)
        }
    }
    setDefaultToolbar()
    selectionModeEnabled = false
}

// FIXME это должно быть логикой вьюмодели. она удаляет из репозитория, она обновляет лайвдату,
//  а UI реагирует только на изменения livedata
private fun deleteSelectedNotes() {
    val selList = adapter.getSelectedList()
    if (selList.isNotEmpty()) {
        val firstSelItem = selList[0]
        val position = adapter.notesList.indexOf(firstSelItem)
        deleteNote(firstSelItem, position)
    }
}

override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
    android.R.id.home -> {
        deselectAllNotes()
        true
    }
    R.id.action_delete -> {
        deleteSelectedNotes()
        true
    }
    else ->
        super.onOptionsItemSelected(item)
}

// FIXME логика удаления вся во вьюмодели должна быть. не должен ui чето там додумывать -
//  он просто обновляется реагируя на изменения livedata
private fun deleteNote(note: Note, position: Int) {
    val isNoteDeleted = notesVModel.delete(note)
    isNoteDeleted.also {
        adapter.notesList.remove(note)
        adapter.notifyItemRemoved(position)
        adapter.notifyItemRangeChanged(position, adapter.notesList.size)
        if (adapter.getSelectedList().isEmpty()) {
            setDefaultToolbar()
            selectionModeEnabled = false
        }
        deleteSelectedNotes()
        //}

    }
}*/
}