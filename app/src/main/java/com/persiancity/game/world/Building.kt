package com.persiancity.game.world

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.persiancity.game.data.PersianTexts
import com.persiancity.game.entities.NPC

/**
 * ساختمان - تعریف یک ساختمان روی نقشه
 * Building on the city map with type, position, color, entrance position
 */
class Building(
    val id: String,
    val name: String,
    val type: BuildingType,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val wallColor: Int,
    val roofColor: Int,
    val npcId: String? = null
) {
    /** نقطه ورود - جلوی در */
    val entranceX: Float get() = x + width / 2
    val entranceY: Float get() = y + height + 30f

    /** فاصله نقطه ورود از یک مختصات */
    fun distanceToEntrance(px: Float, py: Float): Float {
        val dx = entranceX - px
        val dy = entranceY - py
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    /** آیا بازیکن به اندازه کافی به در نزدیک است؟ */
    fun isAtDoor(px: Float, py: Float, range: Float = 80f): Boolean =
        distanceToEntrance(px, py) < range

    /** کشیدن ساختمان به سبک کارتونی */
    fun draw(canvas: Canvas, paint: Paint, sx: Float, sy: Float) {
        val r = RectF(sx, sy, sx + width, sy + height)

        // سایه زیر ساختمان
        paint.color = Color.argb(60, 0, 0, 0)
        canvas.drawRect(RectF(sx + 4, sy + height, sx + width + 4, sy + height + 8), paint)

        // دیوارها
        paint.color = wallColor
        canvas.drawRect(r, paint)

        // سقف
        paint.color = roofColor
        // سقف شیب‌دار به سبک کارتونی
        canvas.drawRect(RectF(sx - 4, sy - 18, sx + width + 4, sy + 14), paint)
        // مثلث سقف
        val path = android.graphics.Path()
        path.moveTo(sx - 4, sy + 14)
        path.lineTo(sx + width / 2, sy - 30)
        path.lineTo(sx + width + 4, sy + 14)
        path.close()
        paint.color = darken(roofColor, 0.8f)
        canvas.drawPath(path, paint)

        // در - با رنگ متفاوت بسته به نوع
        val doorColor = when (type) {
            BuildingType.HOME -> Color.parseColor("#FF5D4037")
            BuildingType.BANK -> Color.parseColor("#FF1A237E")
            BuildingType.POLICE_STATION -> Color.parseColor("#FF0D47A1")
            BuildingType.HOSPITAL -> Color.parseColor("#FFE53935")
            else -> Color.parseColor("#FF6D4C2F")
        }
        paint.color = doorColor
        val doorW = 30f
        val doorH = 50f
        val doorX = sx + width / 2 - doorW / 2
        val doorY = sy + height - doorH
        canvas.drawRect(RectF(doorX, doorY, doorX + doorW, doorY + doorH), paint)
        // دستگیره در
        paint.color = Color.parseColor("#FFFFD54F")
        canvas.drawCircle(doorX + doorW - 5f, doorY + doorH / 2, 2.5f, paint)

        // پنجره‌ها
        paint.color = Color.parseColor("#FFB3E5FC")
        val winSize = 22f
        canvas.drawRect(RectF(sx + 10, sy + 25, sx + 10 + winSize, sy + 25 + winSize), paint)
        canvas.drawRect(RectF(sx + width - 10 - winSize, sy + 25, sx + width - 10, sy + 25 + winSize), paint)
        // قاب پنجره
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRect(RectF(sx + 10, sy + 25, sx + 10 + winSize, sy + 25 + winSize), paint)
        canvas.drawRect(RectF(sx + width - 10 - winSize, sy + 25, sx + width - 10, sy + 25 + winSize), paint)
        // خطوط پنجره
        canvas.drawLine(sx + 10 + winSize / 2, sy + 25, sx + 10 + winSize / 2, sy + 25 + winSize, paint)
        canvas.drawLine(sx + 10, sy + 25 + winSize / 2, sx + 10 + winSize, sy + 25 + winSize / 2, paint)
        canvas.drawLine(sx + width - 10 - winSize / 2, sy + 25, sx + width - 10 - winSize / 2, sy + 25 + winSize, paint)
        canvas.drawLine(sx + width - 10 - winSize, sy + 25 + winSize / 2, sx + width - 10, sy + 25 + winSize / 2, paint)
        paint.style = Paint.Style.FILL

        // علامت/نشان ساختمان (آیکون بزرگ)
        drawBuildingSign(canvas, paint, sx, sy)

        // نام ساختمان بالای سقف
        paint.color = Color.WHITE
        paint.textSize = 16f
        paint.textAlign = Paint.Align.CENTER
        for (dx in -1..1) for (dy in -1..1) {
            if (dx == 0 && dy == 0) continue
            paint.color = Color.BLACK
            canvas.drawText(name, sx + width / 2 + dx, sy - 35f + dy, paint)
        }
        paint.color = Color.WHITE
        canvas.drawText(name, sx + width / 2, sy - 35f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawBuildingSign(canvas: Canvas, paint: Paint, sx: Float, sy: Float) {
        val cx = sx + width / 2
        val cy = sy + 60
        when (type) {
            BuildingType.HOME -> {
                // قلب
                paint.color = Color.parseColor("#FFE53935")
                canvas.drawCircle(cx - 5, cy - 2, 6f, paint)
                canvas.drawCircle(cx + 5, cy - 2, 6f, paint)
                val p = android.graphics.Path()
                p.moveTo(cx - 10, cy + 1)
                p.lineTo(cx, cy + 12)
                p.lineTo(cx + 10, cy + 1)
                p.close()
                canvas.drawPath(p, paint)
            }
            BuildingType.CLOTHING_SHOP -> {
                // تی‌شرت
                paint.color = Color.parseColor("#FF1976D2")
                val p = android.graphics.Path()
                p.moveTo(cx - 12, cy - 8)
                p.lineTo(cx - 4, cy - 12)
                p.lineTo(cx + 4, cy - 12)
                p.lineTo(cx + 12, cy - 8)
                p.lineTo(cx + 8, cy - 2)
                p.lineTo(cx + 6, cy - 2)
                p.lineTo(cx + 6, cy + 10)
                p.lineTo(cx - 6, cy + 10)
                p.lineTo(cx - 6, cy - 2)
                p.lineTo(cx - 8, cy - 2)
                p.close()
                canvas.drawPath(p, paint)
            }
            BuildingType.RESTAURANT -> {
                // چنگال و قاشق
                paint.color = Color.parseColor("#FFFF9800")
                paint.strokeWidth = 2.5f
                canvas.drawLine(cx - 8, cy - 12, cx - 8, cy + 10, paint)
                canvas.drawLine(cx + 8, cy - 12, cx + 8, cy + 10, paint)
                canvas.drawLine(cx - 12, cy - 12, cx - 4, cy - 12, paint)
                canvas.drawLine(cx + 4, cy - 12, cx + 12, cy - 12, paint)
            }
            BuildingType.BARBERSHOP -> {
                // قیچی
                paint.color = Color.parseColor("#FFD32F2F")
                canvas.drawCircle(cx - 8, cy - 4, 4f, paint)
                canvas.drawCircle(cx + 8, cy - 4, 4f, paint)
                paint.strokeWidth = 2f
                canvas.drawLine(cx - 6, cy - 2, cx + 8, cy + 12, paint)
                canvas.drawLine(cx + 6, cy - 2, cx - 8, cy + 12, paint)
            }
            BuildingType.CAR_DEALER -> {
                // ماشین کوچک
                paint.color = Color.parseColor("#FF388E3C")
                canvas.drawRoundRect(RectF(cx - 14, cy - 4, cx + 14, cy + 8), 4f, 4f, paint)
                canvas.drawRoundRect(RectF(cx - 8, cy - 10, cx + 8, cy + 2), 3f, 3f, paint)
                paint.color = Color.BLACK
                canvas.drawCircle(cx - 9, cy + 10, 3f, paint)
                canvas.drawCircle(cx + 9, cy + 10, 3f, paint)
            }
            BuildingType.GARAGE -> {
                // چرخ‌دنده
                paint.color = Color.parseColor("#FF616161")
                canvas.drawCircle(cx, cy, 10f, paint)
                paint.color = Color.parseColor("#FFBDBDBD")
                canvas.drawCircle(cx, cy, 4f, paint)
            }
            BuildingType.BANK -> {
                // علامت دلار/تومان
                paint.color = Color.parseColor("#FFFFD54F")
                paint.textSize = 28f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("ب", cx, cy + 10, paint)
                paint.textAlign = Paint.Align.LEFT
            }
            BuildingType.POLICE_STATION -> {
                // ستاره پلیس
                paint.color = Color.parseColor("#FFFFD54F")
                drawStar(canvas, paint, cx, cy, 5, 12f, 5f)
            }
            BuildingType.HOSPITAL -> {
                // صلیب
                paint.color = Color.parseColor("#FFE53935")
                canvas.drawRect(RectF(cx - 3, cy - 10, cx + 3, cy + 10), paint)
                canvas.drawRect(RectF(cx - 10, cy - 3, cx + 10, cy + 3), paint)
            }
            BuildingType.PARK -> {
                // درخت
                paint.color = Color.parseColor("#FF6D4C2F")
                canvas.drawRect(RectF(cx - 3, cy, cx + 3, cy + 10), paint)
                paint.color = Color.parseColor("#FF388E3C")
                canvas.drawCircle(cx, cy - 5, 12f, paint)
            }
            BuildingType.GYM -> {
                // دمبل
                paint.color = Color.parseColor("#FF795548")
                canvas.drawRect(RectF(cx - 14, cy - 2, cx + 14, cy + 2), paint)
                canvas.drawRect(RectF(cx - 16, cy - 6, cx - 12, cy + 6), paint)
                canvas.drawRect(RectF(cx + 12, cy - 6, cx + 16, cy + 6), paint)
            }
            BuildingType.SUPERMARKET -> {
                // سبد خرید
                paint.color = Color.parseColor("#FFEF6C00")
                canvas.drawRect(RectF(cx - 10, cy - 8, cx + 10, cy + 8), paint)
                paint.color = Color.WHITE
                canvas.drawRect(RectF(cx - 6, cy - 4, cx - 2, cy), paint)
                canvas.drawRect(RectF(cx + 2, cy - 4, cx + 6, cy), paint)
            }
            BuildingType.CINEMA -> {
                // نوار فیلم
                paint.color = Color.parseColor("#FF000000")
                canvas.drawRect(RectF(cx - 14, cy - 8, cx + 14, cy + 8), paint)
                paint.color = Color.WHITE
                for (i in 0..3) {
                    canvas.drawCircle(cx - 10 + i * 7, cy - 5, 1.5f, paint)
                    canvas.drawCircle(cx - 10 + i * 7, cy + 5, 1.5f, paint)
                }
            }
            BuildingType.GAS_STATION -> {
                // پمپ بنزین
                paint.color = Color.parseColor("#FFD32F2F")
                canvas.drawRect(RectF(cx - 4, cy - 10, cx + 4, cy + 10), paint)
                canvas.drawRect(RectF(cx - 8, cy + 8, cx + 8, cy + 12), paint)
            }
            BuildingType.JOB_CENTER -> {
                // چکش و گاری
                paint.color = Color.parseColor("#FF1976D2")
                paint.textSize = 24f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("ک", cx, cy + 10, paint)
                paint.textAlign = Paint.Align.LEFT
            }
            BuildingType.SCHOOL -> {
                // کتاب
                paint.color = Color.parseColor("#FF7B1FA2")
                canvas.drawRect(RectF(cx - 10, cy - 8, cx + 10, cy + 8), paint)
                paint.color = Color.WHITE
                canvas.drawRect(RectF(cx - 8, cy - 6, cx + 8, cy + 6), paint)
                paint.color = Color.parseColor("#FF7B1FA2")
                canvas.drawLine(cx, cy - 6, cx, cy + 6, paint)
            }
            BuildingType.LIBRARY -> {
                // کتاب‌های روی هم
                paint.color = Color.parseColor("#FF388E3C")
                canvas.drawRect(RectF(cx - 10, cy + 4, cx + 10, cy + 10), paint)
                paint.color = Color.parseColor("#FFD32F2F")
                canvas.drawRect(RectF(cx - 8, cy - 2, cx + 12, cy + 4), paint)
                paint.color = Color.parseColor("#FF1976D2")
                canvas.drawRect(RectF(cx - 12, cy - 8, cx + 8, cy - 2), paint)
            }
            BuildingType.HELIPAD -> {
                // H بزرگ
                paint.color = Color.WHITE
                paint.textSize = 28f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("H", cx, cy + 10, paint)
                paint.textAlign = Paint.Align.LEFT
            }
            BuildingType.BIKE_SHOP -> {
                // دوچرخه
                paint.color = Color.parseColor("#FF00BCD4")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                canvas.drawCircle(cx - 8, cy + 4, 5f, paint)
                canvas.drawCircle(cx + 8, cy + 4, 5f, paint)
                canvas.drawLine(cx - 8, cy + 4, cx, cy - 4, paint)
                canvas.drawLine(cx + 8, cy + 4, cx, cy - 4, paint)
                canvas.drawLine(cx, cy - 4, cx, cy + 4, paint)
                paint.style = Paint.Style.FILL
            }
        }
    }

    private fun drawStar(canvas: Canvas, paint: Paint, cx: Float, cy: Float, points: Int, outer: Float, inner: Float) {
        val path = android.graphics.Path()
        val angle = Math.PI / points
        for (i in 0 until points * 2) {
            val r = if (i % 2 == 0) outer else inner
            val px = (cx + r * Math.cos(i * angle - Math.PI / 2)).toFloat()
            val py = (cy + r * Math.sin(i * angle - Math.PI / 2)).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun darken(c: Int, factor: Float): Int {
        val r = (Color.red(c) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(c) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(c) * factor).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }
}

/** انواع ساختمان‌ها */
enum class BuildingType {
    HOME,              // خانه
    CLOTHING_SHOP,     // مغازه لباس
    RESTAURANT,        // رستوران
    BARBERSHOP,         // آرایشگاه
    CAR_DEALER,        // نمایشگاه ماشین
    GARAGE,            // گاراژ (تنظیم ماشین)
    BANK,              // بانک
    POLICE_STATION,    // کلانتری
    HOSPITAL,          // بیمارستان
    PARK,              // پارک
    GYM,               // باشگاه
    SUPERMARKET,       // سوپرمارکت
    CINEMA,            // سینما
    GAS_STATION,       // پمپ بنزین
    JOB_CENTER,        // مرکز کاریابی
    SCHOOL,            // مدرسه
    LIBRARY,           // کتابخانه
    HELIPAD,           // فرودگاه هلیکوپتر
    BIKE_SHOP          // مغازه دوچرخه
}
