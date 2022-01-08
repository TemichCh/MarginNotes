package com.example.notesdemo.utils

//copied from
//https://github.com/icerockdev/moko-mvvm/blob/master/mvvm-livedata/src/androidMain/kotlin/dev/icerock/moko/mvvm/livedata/EditTextBindings.kt

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import java.io.Closeable

fun MutableLiveData<String>.bindTwoWayToEditTextText(
    lifecycleOwner: LifecycleOwner,
    editText: EditText
): Closeable {
    val readCloseable = bindNotNull(lifecycleOwner) {
        if (editText.text.toString() == it) return@bindNotNull

        editText.setText(it)
    }

    val liveData = this
    val watcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val str = s.toString()
            if (str == liveData.value) return

            liveData.value = str
        }

        override fun afterTextChanged(s: Editable?) = Unit
    }
    editText.addTextChangedListener(watcher)

    val writeCloseable = Closeable {
        editText.removeTextChangedListener(watcher)
    }

    return Closeable {
        readCloseable.close()
        writeCloseable.close()
    }
}
