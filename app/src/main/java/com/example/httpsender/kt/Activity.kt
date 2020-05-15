package com.example.httpsender.kt

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

/**
 * User: ljx
 * Date: 2020/5/15
 * Time: 16:33
 */

fun <T : Activity> Activity.startActivity(clazz: KClass<T>, block: (Intent.() -> Unit)? = null) {
    val intent = Intent(this, clazz.java).apply {
        block?.invoke(this)
    }
    startActivity(intent)
}

fun <T : Activity> Fragment.startActivity(clazz: KClass<T>, block: (Intent.() -> Unit)? = null) {
    val intent = Intent(activity, clazz.java).apply {
        block?.invoke(this)
    }
    startActivity(intent)
}