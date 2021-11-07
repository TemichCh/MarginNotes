package com.example.notesdemo.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notesdemo.R
import com.example.notesdemo.model.Notes
import kotlinx.android.synthetic.main.notes_list_item.view.*
import kotlinx.coroutines.flow.Flow

class NotesListAdapter : RecyclerView.Adapter<NotesViewHolder>() {
    var notesList = mutableListOf<Notes>()

    @SuppressLint("NotifyDataSetChanged")
    fun setNotes(notes: List<Notes>) {
        this.notesList = notes.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.notes_list_item, parent, false)
        return NotesViewHolder(view)

    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        val note = notesList[position]
        holder.itemName.text = note.noteName
        holder.itemDate.text = note.createDate.toString()

    }

    override fun getItemCount(): Int {
        return notesList.size
    }

}

class NotesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val itemDate = view.list_item_date
    val itemName = view.list_item_name
}
