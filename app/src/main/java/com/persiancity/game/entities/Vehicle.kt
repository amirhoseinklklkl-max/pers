package com.persiancity.game.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.persiancity.game.data.GameData
import com.persiancity.game.data.PersianTexts
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * وسیله نقلیه - ماشین، موتور، دوچرخه، هلیکوپتر
 * Vehicle entity with physics, color, upgrades
 */
class Vehicle(
    val def: GameData.VehicleDef,
    var x: Float,
    var y: Float,
    var color: Int = def.color
) {
    var vx: Float = 0f
    var vy: Float = 0f
    var rotation: Float = 0f   // درجه
    var speed: Float = 0f
    var ownerId: String? = null  // اگر متعلق به بازیکن است، "player"
    var upgrades: MutableList<String> = mutableListOf()
    var isPlayerDriving: Boolean = false
    var idlePhase: Float = 0f

    val width: Float get() = def.width
    val height: Float get() = def.height

    fun update(dt: Float, accel: Float, steerX: Float, steerY: Float) {
        if (!isPlayerDriving) {
            idlePhase += dt
            return
        }
        val baseSpeed = def.speed * 80f
        val speedMult = computeSpeedMultiplier()
        val maxSpeed = baseSpeed * speedMult
        speed += accel * maxSpeed * dt * 2f
        speed = speed.coerceIn(-maxSpeed * 0.4f, maxSpeed)

        if (abs(steerX) > 0.05f || abs(steerY) > 0.05f) {
            val targetRot = Math.toDegrees(Math.atan2(steerX.toDouble(), -steerY.toDouble())).toFloat()
            // smooth rotation
            var diff = targetRot - rotation
            while (diff > 180) diff -= 360
            while (diff < -180) diff += 360
            rotation += diff * dt * 4f
        }

        // حرکت بر اساس زاویه
        val rad = Math.toRadians(rotation.toDouble())
        val dx = sin(rad).toFloat() * speed * dt
        val dy = -cos(rad).toFloat() * speed * dt
        x += dx
        y += dy

        // محدودیت نقشه
        x = x.coerceIn(width / 2, GameData.WORLD_WIDTH - width / 2)
        y = y.coerceIn(height / 2, GameData.WORLD_HEIGHT - height / 2)

        // اصطکاک
        speed *= (1f - dt * 0.6f)
        if (abs(speed) < 1f) speed = 0f
    }

    private fun computeSpeedMultiplier(): Float {
        var mult = 1f
        if (upgrades.contains("turbo")) mult += 0.2f
        if (upgrades.contains("engine")) mult += 0.3f
        return mult
    }

    /** کشیدن وسیله نقلیه */
    fun draw(canvas: Canvas, paint: Paint, sx: Float, sy: Float) {
        canvas.save()
        canvas.rotate(rotation, sx, sy)

        // سایه
        paint.color = Color.argb(70, 0, 0, 0)
        canvas.drawOval(RectF(sx - width / 2 + 4, sy + height / 2 - 6, sx + width / 2 + 4, sy + height / 2 + 6), paint)

        when (def.type) {
            GameData.VehicleType.CAR -> drawCar(canvas, paint, sx, sy)
            GameData.VehicleType.MOTORCYCLE -> drawMotorcycle(canvas, paint, sx, sy)
            GameData.VehicleType.BICYCLE -> drawBicycle(canvas, paint, sx, sy)
            GameData.VehicleType.HELICOPTER -> drawHelicopter(canvas, paint, sx, sy)
        }

        canvas.restore()

        // چرخش پره‌های هلیکوپتر (خارج از rotate اصلی تا روی خودش بچرخه)
        if (def.type == GameData.VehicleType.HELICOPTER) {
            val bladeAngle = (idlePhase * 720f) % 360f
            paint.color = Color.argb(120, 100, 100, 100)
            canvas.save()
            canvas.rotate(bladeAngle, sx, sy - height / 2 + 5)
            canvas.drawRect(RectF(sx - width, sy - height / 2 + 2, sx + width, sy - height / 2 + 8), paint)
            canvas.restore()
        }
    }

    private fun drawCar(canvas: Canvas, paint: Paint, sx: Float, sy: Float) {
        val w = width
        val h = height
        // بدنه پایین
        paint.color = color
        canvas.drawRoundRect(RectF(sx - w / 2, sy - h / 2 + 6, sx + w / 2, sy + h / 2 - 4), 8f, 8f, paint)
        // سقف کابین
        paint.color = darken(color, 0.85f)
        canvas.drawRoundRect(RectF(sx - w / 3, sy - h / 3, sx + w / 3, sy + h / 4), 6f, 6f, paint)
        // شیشه‌ها
        paint.color = Color.parseColor("#FFB3E5FC")
        canvas.drawRect(RectF(sx - w / 4, sy - h / 4, sx + w / 4, sy), paint)
        // چراغ‌های جلو (پشت در rotation)
        paint.color = Color.parseColor("#FFFFFFF0")
        canvas.drawRect(RectF(sx - w / 2 + 4, sy + h / 2 - 8, sx - w / 2 + 12, sy + h / 2 - 2), paint)
        canvas.drawRect(RectF(sx + w / 2 - 12, sy + h / 2 - 8, sx + w / 2 - 4, sy + h / 2 - 2), paint)
        // چراغ عقب
        paint.color = Color.parseColor("#FFFF5252")
        canvas.drawRect(RectF(sx - w / 2 + 4, sy - h / 2 + 2, sx - w / 2 + 12, sy - h / 2 + 8), paint)
        canvas.drawRect(RectF(sx + w / 2 - 12, sy - h / 2 + 2, sx + w / 2 - 4, sy - h / 2 + 8), paint)
        // چرخ‌ها
        paint.color = Color.parseColor("#FF212121")
        canvas.drawCircle(sx - w / 2.6f, sy - h / 3, 6f, paint)
        canvas.drawCircle(sx + w / 2.6f, sy - h / 3, 6f, paint)
        canvas.drawCircle(sx - w / 2.6f, sy + h / 3, 6f, paint)
        canvas.drawCircle(sx + w / 2.6f, sy + h / 3, 6f, paint)
        // اسپویلر اگر نصب شده
        if (upgrades.contains("spoiler")) {
            paint.color = Color.parseColor("#FFD32F2F")
            canvas.drawRect(RectF(sx - w / 3, sy - h / 2 + 2, sx + w / 3, sy - h / 2 + 6), paint)
        }
    }

    private fun drawMotorcycle(canvas: Canvas, paint: Paint, sx: Float, sy: Float) {
        val w = width
        val h = height
        // چرخ‌ها
        paint.color = Color.parseColor("#FF212121")
        canvas.drawCircle(sx, sy - h / 2 + 8, 12f, paint)
        canvas.drawCircle(sx, sy + h / 2 - 8, 12f, paint)
        // بدنه
        paint.color = color
        canvas.drawRoundRect(RectF(sx - 8f, sy - h / 3, sx + 8f, sy + h / 3), 6f, 6f, paint)
        // زین
        paint.color = Color.parseColor("#FF424242")
        canvas.drawRoundRect(RectF(sx - 10f, sy - 4f, sx + 10f, sy + 8f), 4f, 4f, paint)
        // دسته
        paint.color = darken(color, 0.7f)
        canvas.drawRect(RectF(sx - 3f, sy - h / 2 + 4, sx + 3f, sy - h / 4), paint)
    }

    private fun drawBicycle(canvas: Canvas, paint: Paint, sx: Float, sy: Float) {
        val w = width
        val h = height
        // چرخ‌ها
        paint.color = Color.parseColor("#FF212121")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawCircle(sx, sy - h / 2 + 6, 11f, paint)
        canvas.drawCircle(sx, sy + h / 2 - 6, 11f, paint)
        // بدنه
        paint.color = color
        paint.style = Paint.Style.FILL
        canvas.drawLine(sx, sy - h / 2 + 6, sx, sy + h / 2 - 6, paint)
        canvas.drawLine(sx, sy, sx - 12f, sy - 8f, paint)
        canvas.drawLine(sx, sy, sx + 12f, sy - 8f, paint)
        // زین
        paint.color = Color.parseColor("#FF424242")
        canvas.drawOval(RectF(sx - 6f, sy - 4f, sx + 6f, sy + 2f), paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawHelicopter(canvas: Canvas, paint: Paint, sx: Float, sy: Float) {
        val w = width
        val h = height
        // بدنه
        paint.color = color
        canvas.drawOval(RectF(sx - w / 2, sy - h / 4, sx + w / 2, sy + h / 4), paint)
        // شیشه
        paint.color = Color.parseColor("#FFB3E5FC")
        canvas.drawOval(RectF(sx + w / 4, sy - h / 6, sx + w / 2, sy + h / 8), paint)
        // دم
        paint.color = darken(color, 0.8f)
        canvas.drawRect(RectF(sx - w / 2 - 20f, sy - 3f, sx - w / 2, sy + 3f), paint)
        // پایه‌ها
        paint.color = Color.parseColor("#FF424242")
        canvas.drawRect(RectF(sx - w / 3, sy + h / 4, sx - w / 3 + 5, sy + h / 4 + 10), paint)
        canvas.drawRect(RectF(sx + w / 3 - 5, sy + h / 4, sx + w / 3, sy + h / 4 + 10), paint)
        // پل هلیکوپتر جداگانه در draw اصلی
    }

    private fun darken(c: Int, factor: Float): Int {
        val r = (Color.red(c) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(c) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(c) * factor).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }
}
