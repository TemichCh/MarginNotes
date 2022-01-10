package com.example.notesdemo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.notesdemo.databinding.ActivityEditNoteBinding
import com.example.notesdemo.extensions.format
import com.example.notesdemo.utils.bindTwoWayToEditTextText
import com.example.notesdemo.viewmodel.CreateOrEditViewModel
import java.io.File
import java.util.*


class CreateOrEditNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditNoteBinding

    private val editNoteViewModel: CreateOrEditViewModel by viewModels {
        val repository = application.getNotesRepository()
        return@viewModels ViewModelFactory(repository, this)
    }

    private val getImageSelectResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resolver = applicationContext.contentResolver
                val uri = result.data?.data
                if (uri != null) {
                    resolver.openInputStream(uri).use { stream ->
                        val bytes = stream?.readBytes()
                        if (bytes != null) {
                            //??? с одной стороны нашему приложению достаточно знать как оно хранит файлы
                            // с другой неплохо было бы чтобы система тоже могла их видеть
                            // изначально у меня и была мысль использовать файлы из общего хранилища
                            // чтобы не дублировать их в системе
                            val now = Calendar.getInstance().time
                            val newFileName = now.format("yyyyMMddhhmm")
                            //??? Вместо filesDir наверное лучше использовать context.getExternalFilesDir(
                            //            Environment.DIRECTORY_PICTURES)
                            //https://developer.android.com/training/data-storage/app-specific#media
                            val file = File(this.filesDir, newFileName)
                            file.writeBytes(bytes)
                            editNoteViewModel.noteImage.value = file.path
                            //editNoteViewModel.imageStream.value = bytes
                        }
                    }
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarEditNote)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //??? вынести в fun bindViewModel
        val noteId = intent.getIntExtra(INTENT_EXTRA_NOTE, 0)
        with(editNoteViewModel) {
            load(noteId)

            isEditMode.observe(this@CreateOrEditNoteActivity) {
                showCurrentMode(it)
            }
            noteName.bindTwoWayToEditTextText(this@CreateOrEditNoteActivity, binding.notesName)
            noteText.bindTwoWayToEditTextText(this@CreateOrEditNoteActivity, binding.notesText)

            //imageStream
            noteImage.observe(this@CreateOrEditNoteActivity) {
                val imageUri = it ?: ""
                Glide.with(this@CreateOrEditNoteActivity)
                    .load(imageUri)
                    .thumbnail(0.33f)
                    .centerCrop()
                    .into(binding.notesImage)
            }
        }


        fabAddImageOnClick()
        fabClearImageOnClick()
    }

    private fun fabClearImageOnClick() {
        val fabClearImage = binding.fabEditClearImage
        fabClearImage.setOnClickListener {
            editNoteViewModel.deleteNoteFile()
        }
    }

    private fun fabAddImageOnClick() {
        val fabAddImage = binding.fabEditAddImage
        fabAddImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            getImageSelectResult.launch(
                Intent.createChooser(
                    intent,
                    getString(R.string.selectPictureTitle)
                )
            )
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_save -> {
            if (editNoteViewModel.saveNote()) {
                finish()
            }
            true
        }
        R.id.menu_delete -> {
            editNoteViewModel.deleteOrCancelEdit()
            finish()
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }

    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun showCurrentMode(isEdit: Boolean) {
        val viewFields = listOf<View>(binding.notesName, binding.notesText)
        viewFields.forEach { view ->
            view.apply {
                isFocusable = isEdit
                isFocusableInTouchMode = isEdit
                isEnabled = isEdit
                background.alpha = if (isEdit) 255 else 0
            }
        }

        with(binding) {
            val isImageSet = editNoteViewModel.noteImage.value.isNullOrBlank()
            fabEditAddImage.isVisible = (isEdit && isImageSet)

            fabEditClearImage.isVisible = (isEdit && ! isImageSet)
            //TODO При добавлении новой записи иконки отрисовываются по умолчанию а не для редактирования
            toolbarEditNote.menu?.findItem(R.id.menu_save)?.apply {
                val icon = if (isEdit) {
                    R.drawable.ic_outline_done_24
                } else {
                    R.drawable.ic_outline_edit_24
                }
                setIcon(ContextCompat.getDrawable(this@CreateOrEditNoteActivity, icon))
            }

            toolbarEditNote.menu?.findItem(R.id.menu_delete)?.apply {
                val icon = if (isEdit) {
                    R.drawable.ic_outline_cancel_24
                } else {
                    R.drawable.ic_outline_delete_24
                }
                setIcon(
                    ContextCompat.getDrawable(this@CreateOrEditNoteActivity, icon)
                )
            }
        }
    }

    // пример как закрыть от внешнего доступа всю логику формирования intent'а по которому можно открыть экран
    companion object {
        private const val INTENT_EXTRA_NOTE = "note"

        fun createIntent(context: Context, noteId: Int?): Intent {
            val intent = Intent(context, CreateOrEditNoteActivity::class.java)
            intent.putExtra(INTENT_EXTRA_NOTE, noteId)
            return intent
        }
    }
}
