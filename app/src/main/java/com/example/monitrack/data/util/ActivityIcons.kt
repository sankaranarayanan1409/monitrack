package com.example.monitrack.data.util

import androidx.annotation.DrawableRes
import com.example.monitrack.R

/**
 * Stable icon keys, persisted on [com.example.monitrack.data.entity.Activity] instead of a raw
 * drawable resource ID (which aapt2 can renumber across builds, shuffling icons after a
 * reinstall that adds or removes drawables). Once a key ships, never rename or reuse it for a
 * different icon — only add new ones — so existing rows keep resolving correctly.
 */
object ActivityIcons {
    private val byKey: Map<String, Int> = mapOf(
        "sleep" to R.drawable.ic_sleep,
        "work" to R.drawable.ic_work,
        "exercise" to R.drawable.ic_exercise,
    )

    /** Icon keys offered to users when picking an icon for an activity. */
    val keys: List<String> = byKey.keys.toList()

    @DrawableRes
    fun resolve(key: String?): Int? = key?.let { byKey[it] }
}
