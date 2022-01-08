package com.example.notesdemo.utils

//copied from
//https://github.com/icerockdev/moko-mvvm/blob/master/mvvm-livedata/src/androidMain/kotlin/dev/icerock/moko/mvvm/utils/LiveDataExt.kt

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.io.Closeable

fun <T> LiveData<T>.bind(lifecycleOwner: LifecycleOwner, observer: (T?) -> Unit): Closeable {
    observer(value)

    val androidObserver = Observer<T> { value ->
        observer(value)
    }
    val androidLiveData = this//.ld()

    androidLiveData.observe(lifecycleOwner, androidObserver)

    return Closeable {
        androidLiveData.removeObserver(androidObserver)
    }
}

fun <T> LiveData<T>.bindNotNull(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit): Closeable {
    return bind(lifecycleOwner) { value ->
        if (value == null) return@bind

        observer(value)
    }
}
