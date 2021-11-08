package com.example.notesdemo

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.example.notesdemo.model.Notes
import com.example.notesdemo.veiwmodel.NotesViewModel
import com.example.notesdemo.veiwmodel.NotesViewModelFactory
import kotlinx.android.synthetic.main.activity_edit_note.*
import java.util.*

class EditNote : AppCompatActivity() {

    private var currentNote: Notes? = null

    private val notesVModel: NotesViewModel by viewModels {
        NotesViewModelFactory((application as NotesApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        intent.getParcelableExtra<Notes>("note").also { currentNote = it }

        currentNote?.let {
            notes_name.setText(it.noteName)
            notes_text.setText(it.noteText)
            notes_image.setImageURI(Uri.EMPTY)
        } ?: kotlin.run {

        }

        setSupportActionBar(findViewById(R.id.toolbar_edit_note))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }

    //Adds menu itmes to right upmenu with 3 dots ... not what i want
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit_menu, menu)
        return true//super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_save -> {
            if (currentNote == null) {
                currentNote = Notes(
                    noteName = notes_name.text.toString(),
                    noteText = notes_text.text.toString(),
                    createDate = Date()
                )
                notesVModel.insert(currentNote!!)
            } else {
                currentNote!!.modifiedDate = Date()
                notesVModel.update(currentNote!!)
            }
            true
        }
        R.id.menu_delete -> {
            if (currentNote != null){
                notesVModel.delete(currentNote!!)
                true
            }
            else false

        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }

    }
}