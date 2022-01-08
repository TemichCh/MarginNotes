package com.example.notesdemo.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

//  тут вижу работу с contentResolver - это хорошо. но мы не должны ломать сами uri -
//  нам внешние приложения дают uri ровно по которому можно считать изображение, в тот момент когда
//  uri нам отдали. по этому uri надо считать изображение, положить в наши файлы и показывать картинки
//  из наших локальных файлов
//  надо считать по uri поток и записать в файл - https://developer.android.com/training/data-storage/shared/media#open-file-stream
//  этот поток надо в файл писать так https://developer.android.com/training/data-storage/app-specific#internal-access-store-files
@Deprecated("процедура больше не нужна, оствил для примера на всякий")
fun showImagesThumb(context: Context,imageUri: Uri): Uri? {
    val doc = imageUri.lastPathSegment?.split(":")
    val docId = doc?.get(doc.lastIndex)

    val uriExternal: Uri =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
    val projection = arrayOf(MediaStore.Images.Media._ID)
    val contentResolver: ContentResolver = context.contentResolver
    val cur = contentResolver.query(
        uriExternal,
        projection,
        "${MediaStore.Images.Media._ID} = ?",
        arrayOf(docId),
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
            return imageUri_t
        }
        else return null
    }
    return null
}
