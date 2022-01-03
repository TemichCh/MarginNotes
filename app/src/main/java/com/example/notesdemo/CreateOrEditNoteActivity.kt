package com.example.notesdemo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.notesdemo.databinding.ActivityEditNoteBinding
import com.example.notesdemo.utils.bindTwoWayToEditTextText
import com.example.notesdemo.viewmodel.CreateOrEditViewModel
import java.io.File
import java.util.*


// FIXME имя константы не отражает суть. Константа сама имеет смысл и влияние только внутри класса
//  активити, и должна быть занесена внутрь компаньен объекта данной активити
/** The request code for requesting [Manifest.permission.READ_EXTERNAL_STORAGE] permission. */
private const val READ_EXTERNAL_STORAGE_REQUEST = 0x1045

class CreateOrEditNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditNoteBinding

    // FIXME это свойство не должно быть публичным. а также его можно вообще сделать не изменяемым,
    //  вычисляемым через lazy, считывая значение из intent.
    //  https://kotlinlang.org/docs/delegated-properties.html#lazy-properties
//    var isEditMode = false

    private val editNoteViewModel: CreateOrEditViewModel by viewModels {
        val repository = application.getNotesRepository()
        return@viewModels ViewModelFactory(repository, this)
    }

    private val getImageSelectResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                //TODO generate file name
                //https://stackoverflow.com/questions/3401579/get-filename-and-path-from-uri-from-mediastore
                val resolver = applicationContext.contentResolver
                val uri = result.data?.data
                if (uri != null) {
                    val file = File(applicationContext.filesDir, "test")
                    resolver.openInputStream(uri).use { stream ->
                        val bytes = stream?.readBytes()
                        applicationContext.openFileOutput(file.name, Context.MODE_PRIVATE).use {
                            it.write(bytes)
                        }
                    }
                    editNoteViewModel.noteImage.value = file.path
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val noteId = intent.getIntExtra(INTENT_EXTRA_NOTE, 0)
        with(editNoteViewModel) {
            load(noteId)

            isEditMode.observe(this@CreateOrEditNoteActivity) {
                showCurrentMode(it)
            }
            noteName.bindTwoWayToEditTextText(this@CreateOrEditNoteActivity, binding.notesName)
            noteText.bindTwoWayToEditTextText(this@CreateOrEditNoteActivity, binding.notesText)

            noteImage.observe(this@CreateOrEditNoteActivity) {
                Glide.with(this@CreateOrEditNoteActivity)
                    .load(it)
                    .thumbnail(0.33f)
                    .centerCrop()
                    .into(binding.notesImage)
            }
        }


        setSupportActionBar(binding.toolbarEditNote)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fabAddImageOnClick()
        fabClearImageOnClick()
    }

    private fun fabClearImageOnClick() {
        val fabClearImage = binding.fabEditClearImage
        fabClearImage.setOnClickListener {
            // FIXME тут мы должны вызвать обработчик во вьюмодели, там очистится лайвдата и в свою
            //  очередь сработает подписка ui на эту лайвдату и изображение уберется
            editNoteViewModel.noteImage.value = ""
        }
    }

    private fun fabAddImageOnClick() {
        val fabAddImage = binding.fabEditAddImage
        // FIXME обработчик нажатия надо в отдельную функцию выносить чтобы onCreate не раздувать и
        //  его можно было прочитать нормально не отвлекаясь на другие контексты деталей
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
            if (editNoteViewModel.saveNote()){
                finish()
            }
            true
        }
        /*R.id.menu_delete -> {
            // FIXME должны просто оповещать вьюмодель, а она уже действовать
            if (isEditMode) {
                currentNote?.let {
                    binding.notesName.setText(it.noteName)
                    binding.notesText.setText(it.noteText)
                    if (it.image != null) {
                        openMediaStore()
                    }
                }
            } else
                if (currentNote != null) {
                    notesVModel.delete(currentNote!!)
                    finish()
                }
            isEditMode = !isEditMode
            showCurrentMode(isEditMode)
            true
        }*/
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }

    }

    // FIXME вместо замут с запросом разрешения и добавления своего обработчика стоит использовать
    //  современное API из Activity Result API
    //  https://medium.com/@ajinkya.kolkhede1/requesting-runtime-permissions-using-new-activityresult-api-cb6116551f00
    // TODO вообще эти разрешения даже не нужны если сделать сохранение изображений себе в файлы
    //  так как приложения выдают uri с разрешением на чтение
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    // FIXME правильнее вызывать onReadStoragePermissionGranted - свой обработчик где
                    //  уже делать какую-то логику, так будет понятнее когда и почему вызовется этот
                    //  обработчик. а showImages будто где угодно можно вызывать и покажутся какието
                    //  картинки
                    showImages()
                } else {
                    // FIXME обработку тут стоит доделать :)
                    // If we weren't granted the permission, check to see if we should show
                    // rationale for the permission.
                    /*
                    val showRationale =

                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                        */
                    /**
                     * If we should show the rationale for requesting storage permission, then
                     * we'll show ActivityMainBinding.permissionRationaleView which does this.
                     *
                     * If `showRationale` is false, this means the user has not only denied
                     * the permission, but they've clicked "Don't ask again". In this case
                     * we send the user to the settings page for the app so they can grant
                     * the permission (Yay!) or uninstall the app.
                     */
                    /*if (showRationale) {
                        showNoAccess()
                    } else {
                        goToSettings()
                    }*/
                }
                return
            }
        }
    }


    /**
     * Convenience method to check if [Manifest.permission.READ_EXTERNAL_STORAGE] permission
     * has been granted to the app.
     */
    // FIXME страшно поехал весь код
    private fun haveStoragePermission() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PERMISSION_GRANTED
        } else {
            TODO("VERSION.SDK_INT < JELLY_BEAN")
        }

    /**
     * Convenience method to request [Manifest.permission.READ_EXTERNAL_STORAGE] permission.
     */
    private fun requestPermission() {
        // FIXME лучше использовать ранний возврат - будет код чище
        //  https://habr.com/ru/post/348074/
        if (!haveStoragePermission()) {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            // FIXME юзать в таких случаях Activity Result API
            //  https://medium.com/@ajinkya.kolkhede1/requesting-runtime-permissions-using-new-activityresult-api-cb6116551f00
            ActivityCompat.requestPermissions(this, permissions, READ_EXTERNAL_STORAGE_REQUEST)
        }
    }

    private fun openMediaStore() {
        if (haveStoragePermission()) {
            showImages()
        } else {
            requestPermission()
        }
    }

    // FIXME я уже упоминал в других местах - надо читать чисто из своих файлов, те копии
    //  изображений которые уже в локальное хранилище отправлены, тогда никаких проблем с доступами
    //  и потерями добавленных изображений не будет
    private fun showImages() {
        /*if (currentNote?.image != null) {
            val imageUri = showImagesThumb(context = this.baseContext, currentNote?.image!!.toUri())
            binding.notesImage.tag = imageUri.toString()
            Glide.with(this)
                .load(imageUri)
                .thumbnail(0.33f)
                .centerCrop()
                .into(binding.notesImage)
        }*/
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

            toolbarEditNote.menu?.findItem(R.id.menu_save)?.apply {
                //TODO заменить на отдельный пункт меню?
                val icon = if (isEdit) {
                    R.drawable.ic_outline_done_24
                } else {
                    R.drawable.ic_outline_edit_24
                }
                setIcon(ContextCompat.getDrawable(this@CreateOrEditNoteActivity, icon))
            }

            toolbarEditNote.menu?.findItem(R.id.menu_delete)?.apply {
                //TODO заменить на отдельный пункт меню?
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