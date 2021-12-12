package com.example.notesdemo.adapters

// FIXME нужно убрать использование синтетиков, их развитие остановлено и в любой момент они могут
//  вообще перестать работать.
//  Равноценная замена будет - https://developer.android.com/topic/libraries/view-binding
//import kotlinx.android.synthetic.main.activity_edit_note.*
//import kotlinx.android.synthetic.main.notes_list_item.view.*
import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.notesdemo.databinding.NotesListItemBinding
import com.example.notesdemo.extensions.humanizeDiff
import com.example.notesdemo.model.Notes
import com.example.notesdemo.utils.showImagesThumb
import java.util.*

class NotesListAdapter : RecyclerView.Adapter<NotesViewHolder>() {
    // FIXME тут ужас из мутабельности - и свойство открытое и изменяемое, и список хранится тоже
    //  изменяемый, это создает множество возможных кривых использований приводящих к неожидаемому результату
    //  мы можем в любой момент (даже из вне этого класса) изменить значение notesList, и это
    //  не приведет к notifyDataSetChanged. Можем не меняя значение изменить содержимое списка типа
    //  notesList.add(..) и тоже без нотифая - в итоге это все приведет к поломке приложения и
    //  неконсистентному состоянию.
    //  Надо:
    //  1. закрыть поле приватностью - private
    //  2. сделать поле неизменяемым val - нам не надо менять ссылку на объект, мы будем просто
    //  менять содержимое списка, а не указывать другой список
    val notesList = mutableListOf<Notes>()

    private var itemClickListener: ((Notes) -> Unit)? = null

    private var itemLongClickListener: ((Notes) -> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setNotes(notes: List<Notes>) {
        // FIXME вместо использования списка который нам дали, лучше чистить текущий наш список и
        //  добавлять все элементы из списка переданного в функцию в наш список - тогда не будет
        //  вероятности что передан как аргумент MutableList и после вызова setItems где-то он изменяется
        notesList.clear()
        notesList.addAll(notes)
        // FIXME в пару к notifyDataSetChanged стоит указать в init блоке адаптера setHasStableIds(true)
        //  чтобы были анимации изменений из коробки
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val layouyInflater =
            LayoutInflater.from(parent.context)//.inflate(R.layout.notes_list_item, parent, false)
        val itemBinding = NotesListItemBinding.inflate(layouyInflater, parent, false)
        return NotesViewHolder(itemBinding)

    }


    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        val note = notesList[position]
        holder.itemName.text = note.noteName

            /*if (note.modifiedDate != null)
            // FIXME вместо форскаста лучше заиспользовать конструкцию
            //  (note.modifiedDate ?? note.createDate).humanizeDiff(Date())
            //  это будет более надежный вариант, защищенный от ошибок модификации кода
            //  (если в текущем коде изменить условие но не изменить тело - можем получить креш на форскасте)
            note.modifiedDate!!.humanizeDiff(Date())
        else
            note.createDate.humanizeDiff(Date())*/
        holder.itemDate.text = (note.modifiedDate?:note.createDate).humanizeDiff(Date())

        holder.itemText.text = note.noteText

        // FIXME в биндинге ВСЕГДА надо обрабатывать обе ветки условий.
        //  когда происходит биндинг мы можем привязывать данные как к совершенно новым вьюхам,
        //  так и к уже использовавшимся, где уже есть какая-то картинка, текста и прочее.
        //  и если это не сбрасывать - юзер увидит некорректные данные
        //  (просто надо скроллить длинный список и это будет видно)
        if (note.image != null) {
            // FIXME опять же вместо форскаста следует использовать смарт каст
            //  для этого надо сохранить image в локальную переменную и проверить в
            //  if (noteImage != null) и внутри блока этого условия будет noteImage не нуллабельный
            //  https://kotlinlang.org/docs/typecasts.html#smart-casts
            val imageUri = showImagesThumb(holder.image.context, note.image!!.toUri())

            Glide.with(holder.image.context)
                .load(imageUri)
                .thumbnail(0.33f)
                .centerCrop()
                .into(holder.image)
        }

        holder.itemView.setBackgroundColor(if (note.selected) Color.GREEN else Color.TRANSPARENT)

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
    fun setOnNoteTapListener(listener: ((Notes) -> Unit)) {
        this.itemClickListener = listener
    }

    // FIXME нет смысла добавлять такой сеттер - свойство listener уже публичное и мутабельное, его
    //  напрямую можно из вне менять, а сам сеттер не добавляет вообще никакой логики
    fun onNoteLongClickListener(listener: (Notes) -> Unit){
        this.itemLongClickListener =listener
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    // FIXME эта функция не является зоной ответственности адаптера для ресайкла -
    //  тут ей не место (вообще во вьюмодели должна быть)
    fun getSelectedList() = notesList.filter { note -> note.selected }

}


class NotesViewHolder(binding: NotesListItemBinding) : RecyclerView.ViewHolder(binding.root) {
    // FIXME для всех свойств надо указывать тип чтобы:
    //  1. при чтении было понятно что тут будет храниться
    //  2. IDE не ругалась что у нас тип от Java не понятно нуллабелен или нет
    val itemDate: TextView = binding.listItemDate
    val itemName: TextView = binding.listItemName
    val itemText: TextView = binding.listItemText
    val image: ImageView = binding.ivHasImage
}


