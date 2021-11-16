package com.example.notesdemo

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notesdemo.adapters.NotesListAdapter
import com.example.notesdemo.model.Notes
import com.example.notesdemo.veiwmodel.NotesViewModel
import com.example.notesdemo.veiwmodel.NotesViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val notesVModel: NotesViewModel by viewModels {
        NotesViewModelFactory((application as NotesApplication).repository)
    }

    private var selectionModeEnabled = false
    val adapter = NotesListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(main_activity_toolbar)


        recyclerview_notes.layoutManager = LinearLayoutManager(this)
        recyclerview_notes.adapter = adapter

        adapter.setOnNoteTapListener { note ->
            if (!selectionModeEnabled) {
                val intent = Intent(this@MainActivity, EditNote::class.java)
                intent.putExtra("note", note)
                startActivity(intent)
            } else {
                startSelection(note)
            }
        }

        adapter.onNoteLongClickListener { note ->
            selectionModeEnabled = true
            startSelection(note)
        }


        //allNotes
        notesVModel.allNotes.observe(this) { notes ->
            notes.let { adapter.setNotes(it) }
        }
        //notesVModel.getNotesData().observe(this, {adapter.setNotes(it)})

        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)

        with(recyclerview_notes) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(divider)
        }


        val fab = findViewById<FloatingActionButton>(R.id.fab_main_add_note)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, EditNote::class.java)
            startActivity(intent)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = "Введите наименование для поиска"
        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                notesVModel.handleSearchQuery(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                notesVModel.handleSearchQuery(newText)
                return true
            }


        })
        return true//super.onCreateOptionsMenu(menu)
    }


    private fun startSelection(note: Notes) {
        note.selected = !note.selected
        val index = adapter.notesList.indexOf(note)
        adapter.notifyItemChanged(index);
        if (adapter.notesList.none { note ->
                note.selected
            }) {
            //setDefaultToolbar()
            selectionModeEnabled = false
        } else {
            //setDeleteToolBar()
        }
    }


}