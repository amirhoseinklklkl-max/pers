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
 * بازیکن - شخصیت کاربر
 * Player character with appearance, money, hunger, energy, health
 */
class Player(
    var x: Float = 200f,
    var y: Float = 1200f
) {
    // ==================== ویژگی‌های ظاهری ====================
    var gender: Gender = Gender.MALE
    var hairColor: Int = GameData.HAIR_COLORS[0]
    var shirtColor: Int = GameData.SHIRT_COLORS[0]
    var pantsColor: Int = GameData.PANTS_COLORS[0]
    var skinColor: Int = GameData.SKIN_COLORS[0]
    var hairStyle: Int = 0   // 0=کوتاه، 1=بلند، 2=طاس

    // ==================== آمار ====================
    var money: Long = 5000
    var bankBalance: Long = 0
    var loan: Long = 0
    var hunger: Int = 100    // 0-100، 100 یعنی سیر
    var energy: Int = 100    // 0-100
    var health: Int = 100    // 0-100

    // ==================== شغل و ملک ====================
    var jobId: String? = null
    var ownedVehicles: MutableList<String> = mutableListOf("bicycle") // شروع با دوچرخه
    var ownedProperties: MutableList<String> = mutableListOf()
    var ownedClothing: MutableList<String> = mutableListOf()
    var vehicleUpgrades: MutableMap<String, MutableList<String>> = mutableMapOf()
    var vehicleColors: MutableMap<String, Int> = mutableMapOf()

    // ==================== حرکت ====================
    var vx: Float = 0f
    var vy: Float = 0f
    var facing: Int = 1   // 1=راست، -1=چپ
    var walkPhase: Float = 0f
    var isMoving: Boolean = false
    var isInVehicle: Boolean = false
    var currentVehicleId: String? = null

    val radius: Float = 22f

    enum class Gender { MALE, FEMALE }

    fun update(dt: Float, mx: Float, my: Float) {
        val speed = if (isInVehicle) 0f else 180f
        vx = mx * speed
        vy = my * speed
        x += vx * dt
        y += vy * dt
        isMoving = (abs(mx) + abs(my)) > 0.1f
        if (isMoving) {
            facing = if (mx > 0.05f) 1 else if (mx < -0.05f) -1 else facing
            walkPhase += dt * 8f
        } else {
            walkPhase = 0f
        }
        // محدودیت نقشه
        x = x.coerceIn(radius, GameData.WORLD_WIDTH - radius)
        y = y.coerceIn(radius, GameData.WORLD_HEIGHT - radius)
    }

    /**
     * کشیدن بازیکن با استایل کارتونی
     * Cartoon-style character rendering using primitive shapes
     */
    fun draw(canvas: Canvas, paint: Paint, sx: Float, sy: Float) {
        val px = sx
        val py = sy
        val f = facing

        // سایه زیر پا
        paint.color = Color.argb(80, 0, 0, 0)
        canvas.drawOval(RectF(px - 18f, py + 24f, px + 18f, py + 30f), paint)

        // ===== پاها - با انیمیشن راه رفتن =====
        val legSwing = if (isMoving) sin(walkPhase.toDouble()).toFloat() * 6f else 0f
        paint.color = pantsColor
        // پای چپ
        canvas.save()
        canvas.rotate(legSwing, px - 7f, py + 14f)
        canvas.drawRect(RectF(px - 12f, py + 14f, px - 2f, py + 28f), paint)
        canvas.restore()
        // پای راست
        canvas.save()
        canvas.rotate(-legSwing, px + 7f, py + 14f)
        canvas.drawRect(RectF(px + 2f, py + 14f, px + 12f, py + 28f), paint)
        canvas.restore()

        // کفش‌ها
        paint.color = Color.parseColor("#FF212121")
        canvas.drawOval(RectF(px - 13f, py + 26f, px - 1f, py + 32f), paint)
        canvas.drawOval(RectF(px + 1f, py + 26f, px + 13f, py + 32f), paint)

        // ===== بدنه (لباس) =====
        paint.color = shirtColor
        val body = RectF(px - 16f, py - 6f, px + 16f, py + 16f)
        canvas.drawRoundRect(body, 6f, 6f, paint)

        // بازوها - با نوسان
        val armSwing = if (isMoving) sin(walkPhase.toDouble()).toFloat() * 8f else 0f
        paint.color = shirtColor
        canvas.save()
        canvas.rotate(armSwing, px - 16f, py - 2f)
        canvas.drawRoundRect(RectF(px - 22f, py - 2f, px - 12f, py + 14f), 4f, 4f, paint)
        // دست
        paint.color = skinColor
        canvas.drawCircle(px - 17f, py + 14f, 4f, paint)
        canvas.restore()

        canvas.save()
        canvas.rotate(-armSwing, px + 16f, py - 2f)
        paint.color = shirtColor
        canvas.drawRoundRect(RectF(px + 12f, py - 2f, px + 22f, py + 14f), 4f, 4f, paint)
        paint.color = skinColor
        canvas.drawCircle(px + 17f, py + 14f, 4f, paint)
        canvas.restore()

        // ===== سر =====
        paint.color = skinColor
        canvas.drawCircle(px, py - 18f, 14f, paint)

        // ===== مو =====
        paint.color = hairColor
        when (hairStyle) {
            0 -> {  // مو کوتاه
                canvas.drawArc(RectF(px - 14f, py - 32f, px + 14f, py - 4f), 180f, 180f, true, paint)
            }
            1 -> {  // مو بلند
                canvas.drawArc(RectF(px - 14f, py - 32f, px + 14f, py - 4f), 180f, 180f, true, paint)
                canvas.drawRect(RectF(px - 14f, py - 22f, px - 8f, py + 4f), paint)
                canvas.drawRect(RectF(px + 8f, py - 22f, px + 14f, py + 4f), paint)
            }
            2 -> {  // طاس
                // هیچ
            }
        }

        // ===== صورت =====
        // چشم‌ها
        paint.color = Color.parseColor("#FF212121")
        val eyeY = py - 18f
        if (f == 1) {
            canvas.drawCircle(px + 4f, eyeY, 2f, paint)
            canvas.drawCircle(px + 9f, eyeY, 2f, paint)
        } else {
            canvas.drawCircle(px - 4f, eyeY, 2f, paint)
            canvas.drawCircle(px - 9f, eyeY, 2f, paint)
        }
        // دهان
        paint.color = Color.parseColor("#FFC62828")
        if (f == 1) {
            canvas.drawArc(RectF(px + 3f, py - 14f, px + 9f, py - 10f), 0f, 180f, true, paint)
        } else {
            canvas.drawArc(RectF(px - 9f, py - 14f, px - 3f, py - 10f), 0f, 180f, true, paint)
        }

        // ===== شاخص بالای سر (مخصوص زن: مو بلندتر/رنگ لب) =====
        if (gender == Gender.FEMALE && hairStyle != 2) {
            paint.color = hairColor
            // موهای بلندتر روی شانه
            canvas.drawRoundRect(RectF(px - 16f, py - 20f, px - 8f, py + 4f), 6f, 6f, paint)
            canvas.drawRoundRect(RectF(px + 8f, py - 20f, px + 16f, py + 4f), 6f, 6f, paint)
        }
    }

    /** شعاع تعامل - برای ورود به ساختمان/صحبت با NPC */
    fun interactsWith(otherX: Float, otherY: Float, range: Float = 60f): Boolean {
        val dx = x - otherX
        val dy = y - otherY
        return (dx * dx + dy * dy) < range * range
    }

    /** افزودن پول */
    fun addMoney(amount: Long) {
        money += amount
        if (money < 0) money = 0
    }

    /** خرید - آیا پول کافی است؟ */
    fun canAfford(amount: Long): Boolean = money >= amount

    /** خرج کردن پول - اگر کافی نبود false برمی‌گرداند */
    fun spend(amount: Long): Boolean {
        if (!canAfford(amount)) return false
        money -= amount
        return true
    }

    /** کم شدن گرسنگی، انرژی و سلامتی با گذشت زمان */
    fun decayStats(dt: Float) {
        hunger = (hunger - dt * 1.5f).toInt().coerceIn(0, 100)
        if (hunger < 20) {
            health = (health - dt * 1f).toInt().coerceIn(0, 100)
        }
        if (!isMoving) {
            energy = (energy + dt * 0.5f).toInt().coerceIn(0, 100)
        } else {
            energy = (energy - dt * 0.2f).toInt().coerceIn(0, 100)
        }
    }

    /** خوردن غذا */
    fun eat(hungerRestore: Int, healthBoost: Int) {
        hunger = (hunger + hungerRestore).coerceAtMost(100)
        health = (health + healthBoost).coerceAtMost(100)
    }

    /** استراحت (خواب) - بازیابی انرژی و سلامتی */
    fun sleep() {
        energy = 100
        health = (health + 30).coerceAtMost(100)
        hunger = (hunger - 20).coerceAtLeast(0)
    }
}
