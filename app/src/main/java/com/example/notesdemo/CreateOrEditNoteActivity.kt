package com.example.notesdemo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.notesdemo.databinding.ActivityEditNoteBinding
import com.example.notesdemo.model.Note
import com.example.notesdemo.utils.showImagesThumb
import com.example.notesdemo.viewmodel.NotesViewModel
import com.example.notesdemo.viewmodel.NotesViewModelFactory
import java.util.*


// FIXME имя константы не отражает суть. Константа сама имеет смысл и влияние только внутри класса
//  активити, и должна быть занесена внутрь компаньен объекта данной активити
/** The request code for requesting [Manifest.permission.READ_EXTERNAL_STORAGE] permission. */
private const val READ_EXTERNAL_STORAGE_REQUEST = 0x1045

// FIXME нейминг надо поправить. тут как я вижу правдивое имя будет CreateOrEditNoteActivity
class CreateOrEditNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditNoteBinding

    // FIXME не по кодстайлу - компаньены в самом конце идут
   /* companion object {
        // FIXME ошибка в названии, а также эта константа должна быть приватная и не доступна для
        //  использования из вне данной активности
        const val IS_EDITE_MODE = "IS_EDITE_MODE"
    }*/

    // FIXME для активности все равно должно быть на объект заметки - активность должна просто выводить
    //  все что скажет вьюмодель и ничего не додумывать, и не хранить в себе
    private var currentNote: Note? = null

    // FIXME это свойство не должно быть публичным. а также его можно вообще сделать не изменяемым,
    //  вычисляемым через lazy, считывая значение из intent.
    //  https://kotlinlang.org/docs/delegated-properties.html#lazy-properties
    var isEditMode = false


    // FIXME тут нам нужно не вьюмодель списка а свою вьюмодель создания и редактирования, в которму
    //  мы сразу в конструктор передаем id заметки из intent (случай редактирования) - эта вьюмодель
    //  должна управлять логикой экрана
    private val notesVModel: NotesViewModel by viewModels {
        NotesViewModelFactory((application as NotesApplication).repository)
    }

    // FIXME это точно не var - это val. и имя надо исправить у переменной - не информативное
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // FIXME обработчик получения данных нужно в отдельную функцию выделить и
                //  там реализовать всю логику по чтению изображения по полученному uri и сохранению
                //  в данные приложения
                val data: Intent? = result.data
                val selectedImageUri: Uri? = data?.data
                if (null != selectedImageUri) {
                    binding.notesImage.setTag(selectedImageUri.toString())
                    binding.notesImage.setImageURI(selectedImageUri)
                }
            }
        }

    //    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // FIXME выше уже сказал что вместо этого надо получать id заметки и отдавать в вьюмодель
        //intent.getParcelableExtra<Int?>("note").also { currentNote = it }
        // FIXME нет смысла сохранять в инстанс отдельный флаг - интент он всегда доступен, даже
        //  после пересоздания экрана - с него можно читать
       /* isEditMode = savedInstanceState?.getBoolean(IS_EDITE_MODE, false) ?: intent.getBooleanExtra(
            "isEdit",
            false
        )*/

        // FIXME вместо выставления так данных - должна быть привязка к лайвдатам из вьюмодели,
        //  вьюхи на активити должны быть тупыми и просто получают значения от вьюмодели. а та в
        //  свою очередь получит все данные с бд и выставит в лайвдаты
        currentNote?.let {
            binding.notesName.setText(it.noteName)
            binding.notesText.setText(it.noteText)
            if (it.image != null) {
                openMediaStore()
            }
        }

        setSupportActionBar(findViewById(R.id.toolbar_edit_note))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fabAddImageOnClick()
        fabClearImageOnClick()
    }

    private fun fabClearImageOnClick() {
        val fabClearImage =
            binding.fabEditClearImage //findViewById<FloatingActionButton>(R.id.fab_edit_clear_image)
        // FIXME обработчик нажатия надо в отдельную функцию выносить чтобы onCreate не раздувать и
        //  его можно было прочитать нормально не отвлекаясь на другие контексты деталей
        fabClearImage.setOnClickListener {
            // FIXME тут мы должны вызвать обработчик во вьюмодели, там очистится лайвдата и в свою
            //  очередь сработает подписка ui на эту лайвдату и изображение уберется
            currentNote?.image = null
            Glide.with(binding.notesImage).clear(binding.notesImage)
        }
    }

    private fun fabAddImageOnClick() {
        val fabAddImage =
            binding.fabEditAddImage //findViewById<FloatingActionButton>(R.id.fab_edit_add_image)
        // FIXME обработчик нажатия надо в отдельную функцию выносить чтобы onCreate не раздувать и
        //  его можно было прочитать нормально не отвлекаясь на другие контексты деталей
        fabAddImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            // FIXME заголовок не хардкодить надо а с ресурсов читать
            resultLauncher.launch(
                Intent.createChooser(
                    intent,
                    getString(R.string.selectPictureTitle)
                )
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit_menu, menu)
        showCurrentMode(isEditMode)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_save -> {
            // FIXME должны просто оповещать вьюмодель, а она уже действовать
            if (isEditMode) {
                InsertUpdateNote()

                finish()
            }
            // FIXME вроде тут нету логики переключения режимов же, или есть возможность включать
            //  выключать редактирование?
            isEditMode = !isEditMode
            showCurrentMode(isEditMode)
            true
        }
        R.id.menu_delete -> {
            // FIXME должны просто оповещать вьюмодель, а она уже действовать
            if (isEditMode) {
                currentNote?.let {
                    binding.notesName.setText(it.noteName)
                    binding.notesText.setText(it.noteText)
                    if (it.image != null) {
                        openMediaStore()
                    }
                } ?: kotlin.run {
                    // FIXME зачем тут run?
                }
            } else
                if (currentNote != null) {
                    notesVModel.delete(currentNote!!)
                    finish()
                }
            isEditMode = !isEditMode
            showCurrentMode(isEditMode)
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
       // outState.putBoolean(IS_EDITE_MODE, isEditMode)
    }

    // FIXME нейминг не по кодстайлу. и логика работы с бд это ответственность вьюмодели, не ui
    //  должен все эти данные готовить
    private fun InsertUpdateNote() {
        if (currentNote == null) {
            val newNote = Note(
                noteName = binding.notesName.text.toString(),
                noteText = binding.notesText.text.toString(),
                image = if (binding.notesImage.tag != null) {
                    binding.notesImage.getTag().toString()
                } else null,
                createDate = Date()
            )
            notesVModel.insert(newNote)
            currentNote = newNote
            Toast.makeText(this, R.string.insert_note, Toast.LENGTH_LONG).show()
        } else {
            if (currentNote != null) {
                //currentNote.also { note ->
                currentNote!!.noteName = binding.notesName.text.toString()
                currentNote!!.noteText = binding.notesText.text.toString()
                currentNote!!.createDate = currentNote!!.createDate
                if (binding.notesImage.tag != null)
                    currentNote!!.image = binding.notesImage.getTag().toString()
                currentNote!!.modifiedDate = Date()
                //}
                notesVModel.update(currentNote!!)
                Toast.makeText(this, R.string.update_note, Toast.LENGTH_SHORT).show()
            }
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
        if (currentNote?.image != null) {
            val imageUri = showImagesThumb(context = this.baseContext, currentNote?.image!!.toUri())
            binding.notesImage.tag = imageUri.toString()
            Glide.with(this)
                .load(imageUri)
                .thumbnail(0.33f)
                .centerCrop()
                .into(binding.notesImage)
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

        binding.fabEditAddImage.isVisible = isEdit
        binding.fabEditClearImage.isVisible = (isEdit && currentNote?.image != null)

        binding.toolbarEditNote.menu?.findItem(R.id.menu_save)?.apply {
            //TODO заменить на отдельный пункт меню?
            val icon = if (isEdit) {
                R.drawable.ic_outline_done_24
            } else {
                R.drawable.ic_outline_edit_24
            }
            setIcon(
                ContextCompat.getDrawable(this@CreateOrEditNoteActivity, icon)
            )
        }

        binding.toolbarEditNote.menu?.findItem(R.id.menu_delete)?.apply {
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

// пример как закрыть от внешнего доступа всю логику формирования intent'а по которому можно открыть экран
    companion object {
        private const val INTENT_EXTRA_NOTE = "note"

        fun createIntent(context: Context, noteId: Int): Intent {
            val intent = Intent(context, CreateOrEditNoteActivity::class.java)
            intent.putExtra(INTENT_EXTRA_NOTE, noteId)
            return intent
        }
    }
}