package com.example.monitrack.data.enums

import androidx.annotation.DrawableRes
import com.example.monitrack.R

enum class ActivityType(
    val label: String,
    @DrawableRes val iconRes: Int,
) {
    SLEEP("Sleep", R.drawable.ic_sleep),
    WORK("Work", R.drawable.ic_work),
    EXERCISE("Exercise", R.drawable.ic_exercise),
}