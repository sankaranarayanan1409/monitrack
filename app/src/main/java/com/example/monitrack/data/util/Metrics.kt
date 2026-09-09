package com.example.monitrack.data.util

import com.example.monitrack.data.entity.ActivityConfig
import com.example.monitrack.data.entity.Session
import com.example.monitrack.data.enums.Sex
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.log10

/** Pure, testable domain calculations. No Android or Room dependencies. */
object Metrics {

    fun localDayOf(session: Session, zone: ZoneId): LocalDate =
        Instant.ofEpochMilli(session.startTime).atZone(zone).toLocalDate()

    /** Total completed duration for [day] (by session start day). */
    fun dailyTotalMs(sessions: List<Session>, day: LocalDate, zone: ZoneId): Long =
        sessions
            .filter { it.durationMs != null && localDayOf(it, zone) == day }
            .sumOf { it.durationMs!! }

    /**
     * Whether [day] meets the activity's target: for aggregating activities the day's
     * summed total must reach the target; otherwise a single session must.
     */
    fun metTargetForDay(
        sessions: List<Session>,
        config: ActivityConfig,
        day: LocalDate,
        zone: ZoneId,
    ): Boolean {
        val durations = sessions
            .filter { it.durationMs != null && localDayOf(it, zone) == day }
            .map { it.durationMs!! }
        if (durations.isEmpty()) return false
        return if (config.aggregatesDaily) {
            durations.sum() >= config.targetMs
        } else {
            durations.any { it >= config.targetMs }
        }
    }

    /** Days (start-day based) whose sessions meet the target. */
    fun metDays(sessions: List<Session>, config: ActivityConfig, zone: ZoneId): Set<LocalDate> {
        val byDay = sessions
            .filter { it.durationMs != null }
            .groupBy { localDayOf(it, zone) }
        return byDay.filterValues { durations ->
            val ms = durations.mapNotNull { it.durationMs }
            if (config.aggregatesDaily) ms.sum() >= config.targetMs else ms.any { it >= config.targetMs }
        }.keys
    }

    /** Consecutive met days ending today or yesterday. */
    fun streak(
        sessions: List<Session>,
        config: ActivityConfig,
        today: LocalDate,
        zone: ZoneId,
    ): Int {
        val met = metDays(sessions, config, zone)
        var day = when {
            today in met -> today
            today.minusDays(1) in met -> today.minusDays(1)
            else -> return 0
        }
        var count = 0
        while (day in met) {
            count++
            day = day.minusDays(1)
        }
        return count
    }

    /** Body mass index (kg/m2). Returns null when weight or height is missing or non-positive. */
    fun bmi(weightKg: Double?, heightCm: Double?): Double? {
        if (weightKg == null || heightCm == null) return null
        if (weightKg <= 0 || heightCm <= 0) return null
        val heightM = heightCm / 100.0
        return weightKg / (heightM * heightM)
    }

    /**
     * US Navy body-fat estimate (percent). Returns null when required inputs are missing
     * or non-positive. Hip is required for females only.
     */
    fun bodyFatNavy(
        sex: Sex,
        heightCm: Double?,
        neckCm: Double?,
        waistCm: Double?,
        hipCm: Double?,
    ): Double? {
        if (heightCm == null || neckCm == null || waistCm == null) return null
        if (heightCm <= 0 || neckCm <= 0 || waistCm <= 0) return null
        return when (sex) {
            Sex.MALE -> {
                val girth = waistCm - neckCm
                if (girth <= 0) return null
                495.0 / (1.0324 - 0.19077 * log10(girth) + 0.15456 * log10(heightCm)) - 450.0
            }
            Sex.FEMALE -> {
                if (hipCm == null || hipCm <= 0) return null
                val girth = waistCm + hipCm - neckCm
                if (girth <= 0) return null
                495.0 / (1.29579 - 0.35004 * log10(girth) + 0.22100 * log10(heightCm)) - 450.0
            }
        }.coerceAtLeast(0.0)
    }
}
