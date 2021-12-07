package com.example.notesdemo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
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
import com.example.notesdemo.model.Notes
import com.example.notesdemo.utils.showImagesThumb
import com.example.notesdemo.veiwmodel.NotesViewModel
import com.example.notesdemo.veiwmodel.NotesViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_edit_note.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


/** The request code for requesting [Manifest.permission.READ_EXTERNAL_STORAGE] permission. */
private const val READ_EXTERNAL_STORAGE_REQUEST = 0x1045


class EditNote : AppCompatActivity() {

    lateinit var viewFields: Map<String, TextView>

    companion object {
        const val IS_EDITE_MODE = "IS_EDITE_MODE"
    }

    private var currentNote: Notes? = null

    var isEditMode = false


    private val notesVModel: NotesViewModel by viewModels {
        NotesViewModelFactory((application as NotesApplication).repository)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri: Uri? = data?.data
                if (null != selectedImageUri) {
                    notes_image.setTag(selectedImageUri.toString())
                    notes_image.setImageURI(selectedImageUri)
                }
            }
        }

    //    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        intent.getParcelableExtra<Notes>("note").also { currentNote = it }
        isEditMode = savedInstanceState?.getBoolean(IS_EDITE_MODE, false) ?: intent.getBooleanExtra(
            "isEdit",
            false
        )
        initViews()


        currentNote?.let {
            notes_name.setText(it.noteName)
            notes_text.setText(it.noteText)
            if (it.image != null) {
                openMediaStore()
            }
        } ?: kotlin.run {

        }

        setSupportActionBar(findViewById(R.id.toolbar_edit_note))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val fabAddImage = findViewById<FloatingActionButton>(R.id.fab_edit_add_image)
        fabAddImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            resultLauncher.launch(Intent.createChooser(intent, "Select Picture"))
        }

        val fabClearImage = findViewById<FloatingActionButton>(R.id.fab_edit_clear_image)
        fabClearImage.setOnClickListener {
            currentNote?.image = null
            Glide.with(notes_image).clear(notes_image)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit_menu, menu)
        showCurrentMode(isEditMode)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_save -> {
            if (isEditMode) {
                InsertUpdateNote()

                finish()
            }
            isEditMode = !isEditMode
            showCurrentMode(isEditMode)
            true
        }
        R.id.menu_delete -> {
            if (isEditMode) {
                currentNote?.let {
                    notes_name.setText(it.noteName)
                    notes_text.setText(it.noteText)
                    if (it.image != null) {
                        openMediaStore()
                    }
                } ?: kotlin.run {
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
        outState.putBoolean(IS_EDITE_MODE, isEditMode)
    }

    private fun InsertUpdateNote() {
        if (currentNote == null) {
            val newNote = Notes(
                noteName = notes_name.text.toString(),
                noteText = notes_text.text.toString(),
                image = if (notes_image.tag != null)
                    notes_image.getTag().toString()
                else null,
                createDate = Date()
            )
            notesVModel.insert(newNote)
            currentNote = newNote
            Toast.makeText(this, R.string.insert_note, Toast.LENGTH_LONG).show()
        } else {
            if (currentNote != null) {
                //currentNote.also { note ->
                currentNote!!.noteName = notes_name.text.toString()
                currentNote!!.noteText = notes_text.text.toString()
                currentNote!!.createDate = currentNote!!.createDate
                if (notes_image.tag != null)
                    currentNote!!.image = notes_image.getTag().toString()
                currentNote!!.modifiedDate = Date()
                //}
                notesVModel.update(currentNote!!)
                Toast.makeText(this, R.string.update_note, Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                    showImages()
                } else {
                    // If we weren't granted the permission, check to see if we should show
                    // rationale for the permission.
                    val showRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )

                    /**
                     * If we should show the rationale for requesting storage permission, then
                     * we'll show [ActivityMainBinding.permissionRationaleView] which does this.
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
    private fun haveStoragePermission()
            : Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PERMISSION_GRANTED

    /**
     * Convenience method to request [Manifest.permission.READ_EXTERNAL_STORAGE] permission.
     */
    private fun requestPermission() {
        if (!haveStoragePermission()) {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
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

    private fun showImages() {
        if (currentNote?.image != null) {
            val imageUri = showImagesThumb(context = this.baseContext, currentNote?.image!!.toUri())
            notes_image.tag = imageUri.toString()
            Glide.with(this)
                .load(imageUri)
                .thumbnail(0.33f)
                .centerCrop()
                .into(notes_image)
        }
    }

    private fun initViews() {
        viewFields = mapOf(
            "noteName" to notes_name,
            "noteText" to notes_text
        )
        Log.d("M_EditNote", "initViews=$isEditMode")
        showCurrentMode(isEditMode)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun showCurrentMode(isEdit: Boolean) {

        val info =
            viewFields
        for ((_, v) in info) {
            v as EditText
            v.isFocusable = isEdit
            v.isFocusableInTouchMode = isEdit
            v.isEnabled = isEdit
            v.background.alpha = if (isEdit) 255 else 0
        }

        //val tb =   findViewById<Toolbar>(R.id.toolbar_edit_note)
        val fabImageAdd = findViewById<FloatingActionButton>(R.id.fab_edit_add_image)
        fabImageAdd.isVisible = isEdit

        val fabImageClear = findViewById<FloatingActionButton>(R.id.fab_edit_clear_image)
        fabImageClear.isVisible = (isEdit && currentNote?.image != null)


        val btnEdit = toolbar_edit_note.menu?.findItem(R.id.menu_save)
        if (btnEdit != null)
            with(btnEdit) {
                val icon =
                    //TODO("VERSION.SDK_INT < LOLLIPOP")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (isEdit)
                            resources.getDrawable(R.drawable.ic_outline_done_24, theme)
                        else resources.getDrawable(R.drawable.ic_outline_edit_24, theme)

                    } else {
                        if (!isEdit) ContextCompat.getDrawable(
                            this@EditNote,
                            R.drawable.ic_outline_edit_24
                        )
                        else
                            ContextCompat.getDrawable(this@EditNote, R.drawable.ic_outline_done_24)
                    }
                setIcon(icon)
            }

        val btnDel = toolbar_edit_note.menu?.findItem(R.id.menu_delete)
        if (btnDel != null)
            with(btnDel) {
                val icon =
                    //TODO("VERSION.SDK_INT < LOLLIPOP")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (isEdit)
                            resources.getDrawable(R.drawable.ic_outline_cancel_24, theme)
                        else resources.getDrawable(R.drawable.ic_outline_delete_24, theme)

                    } else {
                        if (!isEdit) ContextCompat.getDrawable(
                            this@EditNote,
                            R.drawable.ic_outline_cancel_24
                        )
                        else
                            ContextCompat.getDrawable(
                                this@EditNote,
                                R.drawable.ic_outline_delete_24
                            )
                    }
                setIcon(icon)
            }

    }

// пример как закрыть от внешнего доступа всю логику формирования intent'а по которому можно открыть экран
//    companion object {
//        private const val INTENT_EXTRA_NOTE = "note"
//
//        fun createIntent(context: Context, noteId: Int): Intent {
//            val intent = Intent(context, EditNote::class.java)
//            intent.putExtra(INTENT_EXTRA_NOTE, noteId)
//            return intent
//        }
//    }
}