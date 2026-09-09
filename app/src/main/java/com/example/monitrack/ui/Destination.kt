package com.example.monitrack.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.monitrack.R

/** Top-level navigation destinations shown in the bottom nav. */
enum class Destination(
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
) {
    TRACK(R.string.nav_track, R.drawable.ic_track),
    PROGRESS(R.string.nav_progress, R.drawable.ic_progress),
    STREAKS(R.string.nav_streaks, R.drawable.ic_streaks),
}
