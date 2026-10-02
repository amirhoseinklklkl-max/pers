package com.persiancity.game.world

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.persiancity.game.data.GameData
import com.persiancity.game.data.PersianTexts
import com.persiancity.game.entities.NPC
import com.persiancity.game.entities.Vehicle
import kotlin.math.sin

/**
 * چرخه شبانه‌روز - مدیریت زمان و نور محیط
 * Day/night cycle: time progression, sky color, ambient light
 */
class DayNightCycle {
    /** زمان فعلی به ساعت (0-24) */
    var hour: Float = 8f
    /** روز فعلی */
    var day: Int = 1
    /** یک روز کامل ۵ دقیقه (انتخاب کاربر: متوسط) */
    private val dayDurationSec: Float = 300f

    /** فاز روز بر اساس ساعت */
    enum class Phase { DAWN, MORNING, NOON, EVENING, DUSK, NIGHT }
    val phase: Phase get() = when {
        hour < 5 -> Phase.NIGHT
        hour < 7 -> Phase.DAWN
        hour < 11 -> Phase.MORNING
        hour < 15 -> Phase.NOON
        hour < 18 -> Phase.EVENING
        hour < 20 -> Phase.DUSK
        hour < 24 -> Phase.NIGHT
        else -> Phase.MORNING
    }

    /** نام فارسی فاز روز */
    val phaseName: String get() = when (phase) {
        Phase.DAWN -> PersianTexts.MORNING
        Phase.MORNING -> PersianTexts.MORNING
        Phase.NOON -> PersianTexts.NOON
        Phase.EVENING -> PersianTexts.EVENING
        Phase.DUSK -> PersianTexts.EVENING
        Phase.NIGHT -> PersianTexts.NIGHT
    }

    /** پیشرفت زمان در هر فریم */
    fun update(dt: Float): Boolean {
        val newDay = false
        val prevHour = hour
        hour += (24f / dayDurationSec) * dt
        if (hour >= 24f) {
            hour -= 24f
            day++
            return true
        }
        return false
    }

    /** رنگ آسمان فعلی */
    fun skyColor(): Int {
        return when (phase) {
            Phase.DAWN -> Color.parseColor("#FFB39DDB")       // بنفش روشن سپیده‌دم
            Phase.MORNING -> Color.parseColor("#FF87CEEB")     // آبی روشن صبح
            Phase.NOON -> Color.parseColor("#FF4FC3F7")        // آبی روشن ظهر
            Phase.EVENING -> Color.parseColor("#FFFFB74D")     // نارنجی عصر
            Phase.DUSK -> Color.parseColor("#FFEF6C00")        // نارنجی تیره غروب
            Phase.NIGHT -> Color.parseColor("#FF1A2A4A")       // آبی تیره شب
        }
    }

    /** رنگ روی زمین (با درخشش نور) */
    fun ambientTint(): Int {
        // آلفا رنگ - مقدار تیرگی
        val alpha = when (phase) {
            Phase.NIGHT -> 130
            Phase.DUSK -> 70
            Phase.DAWN -> 60
            else -> 0
        }
        return Color.argb(alpha, 30, 40, 80)
    }

    /** نمایش ساعت به فارسی: "۸:۳۰ صبح" */
    fun timeString(): String {
        val h = hour.toInt()
        val m = ((hour - h) * 60).toInt()
        val phaseLabel = if (hour < 12) "صبح" else if (hour < 17) "بعدازظهر" else if (hour < 20) "عصر" else "شب"
        val hour12 = if (h == 0) 12 else if (h > 12) h - 12 else h
        return String.format("%d:%02d %s", hour12, m, phaseLabel)
    }

    /** آیا شب است؟ (برای چراغ‌های روشن) */
    fun isNight(): Boolean = phase == Phase.NIGHT || phase == Phase.DUSK

    /** آیا صبح زود است؟ */
    fun isDawn(): Boolean = phase == Phase.DAWN
}
