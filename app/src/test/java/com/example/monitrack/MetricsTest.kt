package com.example.monitrack

import com.example.monitrack.data.util.Metrics
import com.example.monitrack.data.entity.ActivityConfig
import com.example.monitrack.data.entity.Session
import com.example.monitrack.data.enums.Sex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class MetricsTest {

    private val zone: ZoneId = ZoneId.of("UTC")
    private val today: LocalDate = LocalDate.of(2026, 9, 9)

    private fun config(
        type: String,
        targetMinutes: Int,
        aggregatesDaily: Boolean,
    ) = ActivityConfig(
        type = type,
        targetMinutes = targetMinutes,
        aggregatesDaily = aggregatesDaily,
        requiresTargetForCompletion = true,
        dailyNudgeEnabled = false,
        nudgeTime = LocalTime.NOON,
        overrunAlertEnabled = false,
        streakEnabled = true,
        streakColor = 0,
    )

    private fun session(type: String, day: LocalDate, minutes: Long): Session {
        val start = day.atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
        return Session(type = type, startTime = start, endTime = start + minutes * 60_000L)
    }

    // --- daily aggregation vs single-session ---

    @Test
    fun `aggregating activity sums sessions to meet target`() {
        val sleep = config("Sleep", targetMinutes = 480, aggregatesDaily = true)
        val sessions = listOf(
            session("Sleep", today, 300),
            session("Sleep", today, 200),
        )
        assertTrue(Metrics.metTargetForDay(sessions, sleep, today, zone))
    }

    @Test
    fun `non-aggregating activity needs a single session to meet target`() {
        val exercise = config("Exercise", targetMinutes = 60, aggregatesDaily = false)
        val short = listOf(
            session("Exercise", today, 40),
            session("Exercise", today, 40),
        )
        assertFalse(Metrics.metTargetForDay(short, exercise, today, zone))
        val long = listOf(session("Exercise", today, 70))
        assertTrue(Metrics.metTargetForDay(long, exercise, today, zone))
    }

    // --- streak ---

    @Test
    fun `streak counts consecutive met days ending today`() {
        val exercise = config("Exercise", targetMinutes = 60, aggregatesDaily = false)
        val sessions = listOf(
            session("Exercise", today, 70),
            session("Exercise", today.minusDays(1), 90),
            session("Exercise", today.minusDays(2), 60),
        )
        assertEquals(3, Metrics.streak(sessions, exercise, today, zone))
    }

    @Test
    fun `an unmet day breaks the streak`() {
        val exercise = config("Exercise", targetMinutes = 60, aggregatesDaily = false)
        val sessions = listOf(
            session("Exercise", today, 70),
            session("Exercise", today.minusDays(1), 30), // short, not met
            session("Exercise", today.minusDays(2), 60),
        )
        assertEquals(1, Metrics.streak(sessions, exercise, today, zone))
    }

    @Test
    fun `streak is zero when latest met day is older than yesterday`() {
        val exercise = config("Exercise", targetMinutes = 60, aggregatesDaily = false)
        val sessions = listOf(session("Exercise", today.minusDays(3), 70))
        assertEquals(0, Metrics.streak(sessions, exercise, today, zone))
    }

    // --- US Navy body fat ---

    @Test
    fun `body fat for male matches reference`() {
        val bf = Metrics.bodyFatNavy(Sex.MALE, heightCm = 180.0, neckCm = 40.0, waistCm = 90.0, hipCm = null)
        assertNotNull(bf)
        assertEquals(18.4, bf!!, 0.5)
    }

    @Test
    fun `body fat for female matches reference`() {
        val bf = Metrics.bodyFatNavy(Sex.FEMALE, heightCm = 165.0, neckCm = 34.0, waistCm = 78.0, hipCm = 96.0)
        assertNotNull(bf)
        assertEquals(28.4, bf!!, 0.5)
    }

    @Test
    fun `body fat is null when female hip is missing`() {
        assertNull(Metrics.bodyFatNavy(Sex.FEMALE, 165.0, 34.0, 78.0, hipCm = null))
    }

    @Test
    fun `body fat is null when waist not greater than neck`() {
        assertNull(Metrics.bodyFatNavy(Sex.MALE, 180.0, 40.0, 40.0, hipCm = null))
    }
}
