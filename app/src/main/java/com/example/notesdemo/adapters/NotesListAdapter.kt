package com.example.notesdemo.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.notesdemo.databinding.NotesListItemBinding
import com.example.notesdemo.extensions.humanizeDiff
import com.example.notesdemo.model.Note
import java.util.*

class NotesListAdapter : RecyclerView.Adapter<NotesViewHolder>() {
    //  тут ужас из мутабельности - и свойство открытое и изменяемое, и список хранится тоже
    //  изменяемый, это создает множество возможных кривых использований приводящих к неожидаемому результату
    //  мы можем в любой момент (даже из вне этого класса) изменить значение notesList, и это
    //  не приведет к notifyDataSetChanged. Можем не меняя значение изменить содержимое списка типа
    //  notesList.add(..) и тоже без нотифая - в итоге это все приведет к поломке приложения и
    //  неконсистентному состоянию.
    //  Надо:
    //  1. закрыть поле приватностью - private
    //  2. сделать поле неизменяемым val - нам не надо менять ссылку на объект, мы будем просто
    //  менять содержимое списка, а не указывать другой список
    private val notesList = mutableListOf<Note>()
    var tracker: SelectionTracker<Long>? = null

    private var itemClickListener: ((Note) -> Unit)? = null

    private var itemLongClickListener: ((Note) -> Unit)? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        val itemId = notesList[position].noteId?.toLong() ?: 0
        return itemId
    }

    //@SuppressLint("NotifyDataSetChanged")
    fun setNotes(notes: List<Note>) {
        //  вместо использования списка который нам дали, лучше чистить текущий наш список и
        //  добавлять все элементы из списка переданного в функцию в наш список - тогда не будет
        //  вероятности что передан как аргумент MutableList и после вызова setItems где-то он изменяется
        notesList.clear()
        notesList.addAll(notes)
        //  в пару к notifyDataSetChanged стоит указать в init блоке адаптера setHasStableIds(true)
        //  чтобы были анимации изменений из коробки

        //???
        //IDE подсказывает использовать этот вызов в крайнем случае, но без него не обновляются данные
        //скорее всего необходимо вынести notesList в объявление класса и переделать его на List?
        //Не срабатывает обновление т.к. mutableList по факту как объект не изменился наверно
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val layouyInflater = LayoutInflater.from(parent.context)
        val itemBinding = NotesListItemBinding.inflate(layouyInflater, parent, false)
        return NotesViewHolder(itemBinding)

    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        val note = notesList[position]
        tracker?.let {
            holder.selected(it.isSelected(note.noteId?.toLong()))
        }

        holder.itemName.text = note.noteName

        holder.itemDate.text = (note.modifiedDate ?: note.createDate).humanizeDiff(Date())
        holder.itemText.text = note.noteText
        val imageUri = note.image ?: ""
        Glide.with(holder.image.context)
            .load(imageUri)
            .thumbnail(0.33f)
            .centerCrop()
            .into(holder.image)

        holder.itemView.setOnClickListener {
            itemClickListener?.invoke(notesList[position])
        }

        holder.itemView.setOnLongClickListener {
            itemLongClickListener?.invoke(notesList[position])
            return@setOnLongClickListener true
        }

    }

    // FIXME нет смысла добавлять такой сеттер - свойство listener уже публичное и мутабельное, его
    //  напрямую можно из вне менять, а сам сеттер не добавляет вообще никакой логики
    //???
    //itemClickListener сделал ptivate var но думаю речь была не про это
    fun setOnNoteTapListener(listener: ((Note) -> Unit)) {
        this.itemClickListener = listener
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

}


class NotesViewHolder(binding: NotesListItemBinding) : RecyclerView.ViewHolder(binding.root) {
    // для всех свойств надо указывать тип чтобы:
    //  1. при чтении было понятно что тут будет храниться
    //  2. IDE не ругалась что у нас тип от Java не понятно нуллабелен или нет
    val itemDate: TextView = binding.listItemDate
    val itemName: TextView = binding.listItemName
    val itemText: TextView = binding.listItemText
    val image: ImageView = binding.ivHasImage

    fun selected(isSelected: Boolean = false) {
        //TODO решение с цветом "временное" надо подобрать вариант получше
        itemView.setBackgroundColor(if (isSelected) Color.GREEN else Color.TRANSPARENT)
    }

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
        object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int = adapterPosition
            override fun getSelectionKey(): Long = itemId
        }
}


