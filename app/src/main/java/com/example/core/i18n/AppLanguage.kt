package com.example.core.i18n

import java.util.Calendar
import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String, val isRtl: Boolean) {
    FA("fa-IR", "فارسی", true),
    EN("en-US", "English", false);

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: EN
    }
}

enum class CalendarSystem {
    JALALI,
    GREGORIAN
}

data class JalaliDate(val year: Int, val month: Int, val day: Int)

object JalaliCalendar {
    private val jalaliMonthsFa = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )
    private val jalaliMonthsEn = listOf(
        "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
        "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand"
    )
    private val gregorianMonthsFa = listOf(
        "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
        "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
    )
    private val gregorianMonthsEn = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    private val weekdaysFa = listOf(
        "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه"
    )
    private val weekdaysEn = listOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    fun toJalali(gYear: Int, gMonth: Int, gDay: Int): JalaliDate {
        val gDm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gMonth > 2) gYear + 1 else gYear
        var days = 355666 + (365 * gYear) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) +
            ((gy2 + 399) / 400) + gDay + gDm[gMonth - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        return JalaliDate(jy, jm, jd)
    }

    fun toGregorian(jYear: Int, jMonth: Int, jDay: Int): Triple<Int, Int, Int> {
        val jy = jYear + 1595
        var days = -355668 + (365 * jy) + ((jy / 33) * 8) + (((jy % 33) + 3) / 4) +
            jDay + if (jMonth < 7) (jMonth - 1) * 31 else ((jMonth - 7) * 30) + 186
        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            gy += 100 * (--days / 36524)
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }
        var gd = days + 1
        val isLeap = ((gy % 4 == 0) && (gy % 100 != 0)) || (gy % 400 == 0)
        val salA = intArrayOf(0, 31, if (isLeap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < 13 && gd > salA[gm]) {
            gd -= salA[gm]
            gm++
        }
        return Triple(gy, gm, gd)
    }

    fun isJalaliLeapYear(jYear: Int): Boolean {
        val breaks = intArrayOf(1, 5, 9, 13, 17, 22, 26, 30)
        val mod = ((jYear - 474) % 128 + 128) % 128
        val rem = mod % 33
        return breaks.contains(rem) || (mod == 0)
    }

    fun jalaliMonthLength(jYear: Int, jMonth: Int): Int = when {
        jMonth <= 6 -> 31
        jMonth <= 11 -> 30
        isJalaliLeapYear(jYear) -> 30
        else -> 29
    }

    fun gregorianMonthLength(gYear: Int, gMonth: Int): Int {
        val isLeap = (gYear % 4 == 0 && gYear % 100 != 0) || (gYear % 400 == 0)
        return when (gMonth) {
            2 -> if (isLeap) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
    }

    fun todayEpochDay(): Long {
        val cal = Calendar.getInstance()
        return epochDayFromGregorian(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun epochDayFromGregorian(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis / 86_400_000L
    }

    fun gregorianFromEpochDay(epochDay: Long): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochDay * 86_400_000L + 43_200_000L
        }
        return Triple(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun jalaliFromEpochDay(epochDay: Long): JalaliDate {
        val (gy, gm, gd) = gregorianFromEpochDay(epochDay)
        return toJalali(gy, gm, gd)
    }

    fun epochDayFromJalali(jYear: Int, jMonth: Int, jDay: Int): Long {
        val (gy, gm, gd) = toGregorian(jYear, jMonth, jDay)
        return epochDayFromGregorian(gy, gm, gd)
    }

    fun weekdayName(epochDay: Long, language: AppLanguage): String {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochDay * 86_400_000L + 43_200_000L
        }
        val idx = (cal.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)
        return if (language == AppLanguage.FA) weekdaysFa[idx] else weekdaysEn[idx]
    }

    fun formatDate(epochDay: Long, language: AppLanguage, calendarSystem: CalendarSystem): String {
        val raw = if (calendarSystem == CalendarSystem.JALALI) {
            val j = jalaliFromEpochDay(epochDay)
            val mName = if (language == AppLanguage.FA) jalaliMonthsFa[j.month - 1] else jalaliMonthsEn[j.month - 1]
            "${j.day} $mName ${j.year}"
        } else {
            val (gy, gm, gd) = gregorianFromEpochDay(epochDay)
            val mName = if (language == AppLanguage.FA) gregorianMonthsFa[gm - 1] else gregorianMonthsEn[gm - 1]
            if (language == AppLanguage.FA) "$gd $mName $gy" else "$mName $gd, $gy"
        }
        return localizeDigits(raw, language)
    }

    fun formatShortDate(epochDay: Long, language: AppLanguage, calendarSystem: CalendarSystem): String {
        val raw = if (calendarSystem == CalendarSystem.JALALI) {
            val j = jalaliFromEpochDay(epochDay)
            "${j.year}/${j.month.toString().padStart(2, '0')}/${j.day.toString().padStart(2, '0')}"
        } else {
            val (gy, gm, gd) = gregorianFromEpochDay(epochDay)
            "${gy}-${gm.toString().padStart(2, '0')}-${gd.toString().padStart(2, '0')}"
        }
        return localizeDigits(raw, language)
    }

    fun formatMonthYear(year: Int, month: Int, language: AppLanguage, calendarSystem: CalendarSystem): String {
        val mName = if (calendarSystem == CalendarSystem.JALALI) {
            if (language == AppLanguage.FA) jalaliMonthsFa[(month - 1).coerceIn(0, 11)]
            else jalaliMonthsEn[(month - 1).coerceIn(0, 11)]
        } else {
            if (language == AppLanguage.FA) gregorianMonthsFa[(month - 1).coerceIn(0, 11)]
            else gregorianMonthsEn[(month - 1).coerceIn(0, 11)]
        }
        return localizeDigits("$mName $year", language)
    }

    fun formatTime(totalMinutes: Int, language: AppLanguage): String {
        val h = (totalMinutes / 60).coerceIn(0, 23)
        val m = (totalMinutes % 60).coerceIn(0, 59)
        val raw = String.format(Locale.US, "%02d:%02d", h, m)
        return localizeDigits(raw, language)
    }

    fun formatCountdown(totalSeconds: Int, language: AppLanguage): String {
        val s = totalSeconds.coerceAtLeast(0)
        val hrs = s / 3600
        val mins = (s % 3600) / 60
        val secs = s % 60
        val raw = if (hrs > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format(Locale.US, "%02d:%02d", mins, secs)
        }
        return localizeDigits(raw, language)
    }

    fun localizeDigits(input: String, language: AppLanguage): String {
        if (language != AppLanguage.FA) return input
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(persianDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatNumber(value: Number, language: AppLanguage): String =
        localizeDigits(value.toString(), language)
}
