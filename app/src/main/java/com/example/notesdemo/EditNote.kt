package com.example.notesdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import com.example.notesdemo.DAO.NotesRepository
import com.example.notesdemo.model.Notes
import com.example.notesdemo.veiwmodel.NotesViewModel
import com.example.notesdemo.veiwmodel.NotesViewModelFactory
import kotlinx.android.synthetic.main.activity_edit_note.*
import java.util.*

class EditNote : AppCompatActivity() {

    val notesVModel:NotesViewModel by viewModels {
        NotesViewModelFactory((application as NotesApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        setSupportActionBar(findViewById(R.id.toolbar_edit_note))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val btnSave = findViewById<Button>(R.id.btn_save)

        val name = edit_text_name.text.toString()
        val text = notes_text.text.toString()
        val vdat = Date()

        btnSave.setOnClickListener {

            if (name != null && text != null && vdat != null) {
                val note = Notes(noteName = name,noteText =text,createDate = vdat  )
                notesVModel.insert(note)
            } else {
                Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Adds menu itmes to right upmenu with 3 dots ... not what i want
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit_menu,menu)
        return true//super.onCreateOptionsMenu(menu)
    }
}