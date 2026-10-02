package com.persiancity.game.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.persiancity.game.data.GameData
import com.persiancity.game.data.PersianTexts
import kotlin.math.sin

/**
 * شخصیت‌های غیربازیکن (NPC)
 * Non-player characters with dialogs and roles
 */
class NPC(
    var x: Float,
    var y: Float,
    val name: String,
    val role: Role,
    val dialogTree: Map<String, List<String>> = emptyMap(),
    val shirtColor: Int = GameData.SHIRT_COLORS.random(),
    val skinColor: Int = GameData.SKIN_COLORS.random(),
    val hairColor: Int = GameData.HAIR_COLORS.random()
) {
    enum class Role {
        CIVILIAN, SHOPKEEPER, BARBER, CHEF, POLICE, BANKER, DEALER, CHILD, ELDER
    }

    var facing: Int = if (Math.random() < 0.5) 1 else -1
    var idlePhase: Float = Math.random().toFloat() * 6.28f
    var walkPhase: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var walkTarget: Float? = null
    var nextWalkTime: Float = 2f + Math.random().toFloat() * 5f
    var timeAccum: Float = 0f

    fun update(dt: Float) {
        idlePhase += dt
        timeAccum += dt
        // حرکت تصادفی خفیف
        if (walkTarget == null && timeAccum > nextWalkTime) {
            timeAccum = 0f
            nextWalkTime = 3f + Math.random().toFloat() * 6f
            if (Math.random() < 0.4f) {
                walkTarget = x + (Math.random().toFloat() - 0.5f) * 200f
                facing = if ((walkTarget ?: x) > x) 1 else -1
            }
        }
        if (walkTarget != null) {
            val target = walkTarget!!
            val dx = target - x
            if (Math.abs(dx) < 5f) {
                walkTarget = null
                vx = 0f
            } else {
                vx = Math.signum(dx) * 30f
                x += vx * dt
                walkPhase += dt * 6f
            }
        }
    }

    fun draw(canvas: Canvas, paint: Paint, sx: Float, sy: Float, showName: Boolean = true) {
        val px = sx
        val py = sy
        // سایه
        paint.color = Color.argb(70, 0, 0, 0)
        canvas.drawOval(RectF(px - 14f, py + 22f, px + 14f, py + 28f), paint)

        val isMoving = Math.abs(vx) > 1f
        val legSwing = if (isMoving) sin(walkPhase.toDouble()).toFloat() * 5f else 0f

        // پاها
        paint.color = Color.parseColor("#FF37474F")
        canvas.save()
        canvas.rotate(legSwing, px - 5f, py + 12f)
        canvas.drawRect(RectF(px - 9f, py + 12f, px - 1f, py + 24f), paint)
        canvas.restore()
        canvas.save()
        canvas.rotate(-legSwing, px + 5f, py + 12f)
        canvas.drawRect(RectF(px + 1f, py + 12f, px + 9f, py + 24f), paint)
        canvas.restore()

        // بدنه
        paint.color = shirtColor
        canvas.drawRoundRect(RectF(px - 12f, py - 4f, px + 12f, py + 14f), 5f, 5f, paint)

        // بازو
        val armSwing = if (isMoving) sin(walkPhase.toDouble()).toFloat() * 6f else 0f
        canvas.save()
        canvas.rotate(armSwing, px - 12f, py - 2f)
        paint.color = shirtColor
        canvas.drawRoundRect(RectF(px - 17f, py - 2f, px - 9f, py + 12f), 3f, 3f, paint)
        paint.color = skinColor
        canvas.drawCircle(px - 13f, py + 12f, 3f, paint)
        canvas.restore()
        canvas.save()
        canvas.rotate(-armSwing, px + 12f, py - 2f)
        paint.color = shirtColor
        canvas.drawRoundRect(RectF(px + 9f, py - 2f, px + 17f, py + 12f), 3f, 3f, paint)
        paint.color = skinColor
        canvas.drawCircle(px + 13f, py + 12f, 3f, paint)
        canvas.restore()

        // سر
        paint.color = skinColor
        canvas.drawCircle(px, py - 16f, 11f, paint)

        // مو
        paint.color = hairColor
        canvas.drawArc(RectF(px - 11f, py - 28f, px + 11f, py - 5f), 180f, 180f, true, paint)

        // چشم‌ها
        paint.color = Color.parseColor("#FF212121")
        val eyeY = py - 16f
        if (facing == 1) {
            canvas.drawCircle(px + 3f, eyeY, 1.5f, paint)
            canvas.drawCircle(px + 7f, eyeY, 1.5f, paint)
        } else {
            canvas.drawCircle(px - 3f, eyeY, 1.5f, paint)
            canvas.drawCircle(px - 7f, eyeY, 1.5f, paint)
        }

        // نشان شغل
        when (role) {
            Role.POLICE -> {
                // کلاه پلیس
                paint.color = Color.parseColor("#FF1A237E")
                canvas.drawRect(RectF(px - 11f, py - 30f, px + 11f, py - 24f), paint)
                // نشان
                paint.color = Color.parseColor("#FFFFD54F")
                canvas.drawCircle(px, py - 4f, 2f, paint)
            }
            Role.CHEF -> {
                // کلاه آشپز
                paint.color = Color.WHITE
                canvas.drawRect(RectF(px - 9f, py - 32f, px + 9f, py - 26f), paint)
                canvas.drawOval(RectF(px - 11f, py - 36f, px + 11f, py - 26f), paint)
            }
            Role.BARBER -> {
                // ساق سر قرمز
                paint.color = Color.parseColor("#FFD32F2F")
                canvas.drawRect(RectF(px - 11f, py - 25f, px + 11f, py - 22f), paint)
            }
            else -> { /* بدون نشان */ }
        }

        // نام (روی سر)
        if (showName) {
            paint.color = Color.WHITE
            paint.textSize = 18f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(name, px, py - 38f, paint)
            paint.color = Color.BLACK
            paint.textSize = 18f
            paint.textAlign = Paint.Align.CENTER
            // outline
            for (dx in -1..1) for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                canvas.drawText(name, px + dx, py - 38f + dy, paint)
            }
            paint.color = Color.WHITE
            canvas.drawText(name, px, py - 38f, paint)
            paint.textAlign = Paint.Align.LEFT
        }
    }

    /** آیا بازیکن به اندازه کافی نزدیک است برای صحبت؟ */
    fun isInRange(px: Float, py: Float, range: Float = 70f): Boolean {
        val dx = x - px
        val dy = y - py
        return (dx * dx + dy * dy) < range * range
    }

    /** دریافت دیالوگ مناسب برای نقش */
    fun getGreeting(): String = when (role) {
        Role.SHOPKEEPER -> PersianTexts.Dialogs.GREETING_SHOPKEEPER
        Role.BARBER -> PersianTexts.Dialogs.GREETING_BARBER
        Role.CHEF -> PersianTexts.Dialogs.GREETING_CHEF
        Role.POLICE -> PersianTexts.Dialogs.GREETING_POLICE
        Role.BANKER -> PersianTexts.Dialogs.GREETING_BANKER
        else -> listOf(
            PersianTexts.Dialogs.GREETING_RANDOM_1,
            PersianTexts.Dialogs.GREETING_RANDOM_2,
            PersianTexts.Dialogs.GREETING_RANDOM_3,
            PersianTexts.Dialogs.GREETING_RANDOM_4,
            PersianTexts.Dialogs.GREETING_RANDOM_5
        ).random()
    }
}
