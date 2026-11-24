package com.example

import com.example.core.i18n.AppLanguage
import com.example.core.i18n.JalaliCalendar
import com.example.data.ai.TaskProposalParser
import org.junit.Assert.assertEquals
import org.junit.Test

class PlanifyCoreTest {
    @Test
    fun jalaliRoundTrip_isAccurate() {
        // 2025-03-21 is 1404/01/01 (Nowruz)
        val jalali = JalaliCalendar.toJalali(2025, 3, 21)
        assertEquals(1404, jalali.year)
        assertEquals(1, jalali.month)
        assertEquals(1, jalali.day)

        val (gy, gm, gd) = JalaliCalendar.toGregorian(1404, 1, 1)
        assertEquals(2025, gy)
        assertEquals(3, gm)
        assertEquals(21, gd)
    }

    @Test
    fun persianDigits_localizedProperly() {
        assertEquals("۱۲:۰۵", JalaliCalendar.localizeDigits("12:05", AppLanguage.FA))
        assertEquals("12:05", JalaliCalendar.localizeDigits("12:05", AppLanguage.EN))
    }

    @Test
    fun taskProposalParser_extractsStructuredTasks() {
        val sample = "Here is your plan:\n[[PLANIFY_TASK|Review Physics Ch.2|45|HIGH|Study]]"
        val proposals = TaskProposalParser.extractProposals(sample)
        assertEquals(1, proposals.size)
        assertEquals("Review Physics Ch.2", proposals[0].title)
        assertEquals(45, proposals[0].estimatedMinutes)
        assertEquals("HIGH", proposals[0].priority)
    }
}
