package com.example.notesdemo

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import com.example.notesdemo.model.Notes
import com.example.notesdemo.veiwmodel.NotesViewModel
import com.example.notesdemo.veiwmodel.NotesViewModelFactory
import kotlinx.android.synthetic.main.activity_edit_note.*
import java.util.*
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.UriPermission
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import java.io.File
import android.content.ContentUris
import java.net.FileNameMap


/** The request code for requesting [Manifest.permission.READ_EXTERNAL_STORAGE] permission. */
private const val READ_EXTERNAL_STORAGE_REQUEST = 0x1045

/**
 * Code used with [IntentSender] to request user permission to delete an image with scoped storage.
 */
private const val DELETE_PERMISSION_REQUEST = 0x1033


class EditNote : AppCompatActivity() {

    private var currentNote: Notes? = null
    //private var contentObserver: ContentObserver? = null

    private val notesVModel: NotesViewModel by viewModels {
        NotesViewModelFactory((application as NotesApplication).repository)
    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val selectedImageUri: Uri? = data?.data
                if (null != selectedImageUri) {
                    val path = getPathFromURI(selectedImageUri)
                    notes_image.setImageURI(selectedImageUri)
                }
                /*notes_image.setTag(uri.toString())
                notes_image.setImageURI(uri)
                */
            }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        intent.getParcelableExtra<Notes>("note").also { currentNote = it }

        currentNote?.let {
            notes_name.setText(it.noteName)
            notes_text.setText(it.noteText)
            if (it.image != null) {
                openMediaStore()
                //showImages(it.image!!.toUri())
            }
            /*notes_image.setImageURI(null)
            //val file = File(it.image)
            if (it.image !=null){
               val contentResolver = getContentResolver()
                 contentResolver.persistedUriPermissions.add(UriPermission. FLAG_GRANT_READ_URI_PERMISSION)
                val imgSrc = ImageDecoder.createSource(contentResolver, it.image!!.toUri())
                val img = ImageDecoder.decodeBitmap(imgSrc)
                notes_image.setImageBitmap(img)
            }
*/
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
                val newNote = Notes(
                    noteName = notes_name.text.toString(),
                    noteText = notes_text.text.toString(),
                    image = notes_image.getTag().toString(),
                    createDate = Date()
                )
                notesVModel.insert(newNote)
                currentNote = newNote
                Toast.makeText(this, "Insert Note $newNote", Toast.LENGTH_LONG).show()
            } else {
                val newNote = Notes(
                    noteId = currentNote!!.noteId,
                    noteName = notes_name.text.toString(),
                    noteText = notes_text.text.toString(),
                    createDate = currentNote!!.createDate,
                    image = notes_image.getTag().toString(),
                    modifiedDate = Date()
                )
                currentNote = newNote
                notesVModel.update(currentNote!!)
                Toast.makeText(this, "Update note", Toast.LENGTH_SHORT).show()
            }
            true
        }
        R.id.menu_delete -> {
            if (currentNote != null) {
                notesVModel.delete(currentNote!!)
                true
            } else false

        }
        R.id.menu_addimage -> {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            }
            resultLauncher.launch(Intent.createChooser(intent, "Select Picture"))

            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
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
                    showImages(currentNote?.image!!.toUri())
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

    private fun showImages(imageUri: Uri) {
        val path = File(imageUri.toString())
        val fname = path.name

        val uriExternal: Uri = // MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val cur = contentResolver.query(
            uriExternal,
            projection,
            "${MediaStore.Images.Media._ID} = ?",
            arrayOf("31"),
            null
        )
        cur?.use { cursor ->
            if (cursor.moveToNext()) {
                val thumbColumn: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
                val _thumpId: Int = cursor.getInt(thumbColumn)
                val imageUri_t = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    _thumpId.toLong()
                )
                Glide.with(this)
                    .load(imageUri_t)
                    .thumbnail(0.33f)
                    .centerCrop()
                    .into(notes_image)
            }
        }


        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentObserver = getApplication().contentResolver.loadThumbnail(imageUri)
            Glide.with(this)
                .load()
                .thumbnail(0.33f)
                .centerCrop()
                .into(notes_image)
        }*/


    }

    /**
     * Convenience method to check if [Manifest.permission.READ_EXTERNAL_STORAGE] permission
     * has been granted to the app.
     */
    fun haveStoragePermission() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PERMISSION_GRANTED

    /**
     * Convenience method to request [Manifest.permission.READ_EXTERNAL_STORAGE] permission.
     */
    fun requestPermission() {
        if (!haveStoragePermission()) {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(this, permissions, READ_EXTERNAL_STORAGE_REQUEST)
        }
    }

    fun openMediaStore() {
        if (haveStoragePermission()) {
            showImages(currentNote?.image!!.toUri())
        } else {
            requestPermission()
        }
    }

    private fun getPathFromURI(uri: Uri?): String {
        var path = ""
        if (contentResolver != null) {
            val cursor = contentResolver.query(uri!!, null, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }
}