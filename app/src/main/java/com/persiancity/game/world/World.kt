package com.persiancity.game.world

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.persiancity.game.data.GameData
import com.persiancity.game.data.PersianTexts
import com.persiancity.game.entities.NPC
import com.persiancity.game.entities.Vehicle
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * نقشه شهر - شامل ساختمان‌ها، خیابان‌ها، درخت‌ها، تزئینات
 * City map: 18+ buildings, roads, trees, decorations
 */
class World {
    val buildings: MutableList<Building> = mutableListOf()
    val npcs: MutableList<NPC> = mutableListOf()
    val parkedVehicles: MutableList<Vehicle> = mutableListOf()

    // درخت‌ها
    data class Tree(val x: Float, val y: Float, val size: Float)
    val trees: MutableList<Tree> = mutableListOf()

    // چراغ‌های خیابون
    data class StreetLamp(val x: Float, val y: Float)
    val lamps: MutableList<StreetLamp> = mutableListOf()

    // خیابان‌ها (در فرمت rect)
    data class Road(val rect: RectF, val horizontal: Boolean)
    val roads: MutableList<Road> = mutableListOf()

    init {
        generateCity()
    }

    /**
     * ساخت شهر با چیدمان منظم:
     * - ۳ خیابان افقی + ۳ خیابان عمودی
     * - ساختمان‌ها بین خیابان‌ها در بلوک‌ها
     */
    private fun generateCity() {
        val W = GameData.WORLD_WIDTH
        val H = GameData.WORLD_HEIGHT
        val roadW = 120f

        // ==== خیابان‌ها ====
        // افقی
        roads.add(Road(RectF(0f, 400f, W, 400f + roadW), true))
        roads.add(Road(RectF(0f, 1100f, W, 1100f + roadW), true))
        roads.add(Road(RectF(0f, 1800f, W, 1800f + roadW), true))
        // عمودی
        roads.add(Road(RectF(700f, 0f, 700f + roadW, H), false))
        roads.add(Road(RectF(1700f, 0f, 1700f + roadW, H), false))
        roads.add(Road(RectF(2700f, 0f, 2700f + roadW, H), false))

        // ==== ساختمان‌ها - ۱۸+ ====
        // ردیف اول (y ~ 100-380): خانه و مغازه‌ها
        buildings.add(Building("home", PersianTexts.HOME, BuildingType.HOME,
            100f, 100f, 200f, 180f, Color.parseColor("#FFE0E0E0"), Color.parseColor("#FFD32F2F")))
        buildings.add(Building("cloth_shop", PersianTexts.CLOTHING_SHOP, BuildingType.CLOTHING_SHOP,
            400f, 100f, 200f, 180f, Color.parseColor("#FFB39DDB"), Color.parseColor("#FF7B1FA2")))
        buildings.add(Building("restaurant", PersianTexts.RESTAURANT, BuildingType.RESTAURANT,
            900f, 100f, 220f, 180f, Color.parseColor("#FFFFCC80"), Color.parseColor("#FFEF6C00")))
        buildings.add(Building("barber", PersianTexts.BARBERSHOP, BuildingType.BARBERSHOP,
            1200f, 100f, 200f, 180f, Color.parseColor("#FFEF9A9A"), Color.parseColor("#FFC62828")))
        buildings.add(Building("supermarket", PersianTexts.SUPERMARKET, BuildingType.SUPERMARKET,
            1900f, 100f, 240f, 180f, Color.parseColor("#FFC5E1A5"), Color.parseColor("#FF558B2F")))
        buildings.add(Building("cinema", PersianTexts.CINEMA, BuildingType.CINEMA,
            2200f, 100f, 220f, 180f, Color.parseColor("#FFB0BEC5"), Color.parseColor("#FF37474F")))
        buildings.add(Building("bike_shop", PersianTexts.BIKE_SHOP, BuildingType.BIKE_SHOP,
            2900f, 100f, 220f, 180f, Color.parseColor("#FF80DEEA"), Color.parseColor("#FF00838F")))
        buildings.add(Building("library", PersianTexts.LIBRARY, BuildingType.LIBRARY,
            3200f, 100f, 240f, 180f, Color.parseColor("#FFA5D6A7"), Color.parseColor("#FF2E7D32")))

        // ردیف دوم (y ~ 600-1080): اداری و خدماتی
        buildings.add(Building("bank", PersianTexts.BANK, BuildingType.BANK,
            100f, 600f, 240f, 200f, Color.parseColor("#FF90CAF9"), Color.parseColor("#FF1565C0")))
        buildings.add(Building("police", PersianTexts.POLICE_STATION, BuildingType.POLICE_STATION,
            400f, 600f, 220f, 200f, Color.parseColor("#FFB0BEC5"), Color.parseColor("#FF0D47A1")))
        buildings.add(Building("hospital", PersianTexts.HOSPITAL, BuildingType.HOSPITAL,
            900f, 600f, 260f, 200f, Color.parseColor("#FFFFFFFF"), Color.parseColor("#FFE53935")))
        buildings.add(Building("gym", PersianTexts.GYM, BuildingType.GYM,
            1250f, 600f, 220f, 200f, Color.parseColor("#FFFFAB91"), Color.parseColor("#FFBF360C")))
        buildings.add(Building("gas", PersianTexts.GAS_STATION, BuildingType.GAS_STATION,
            1900f, 600f, 240f, 200f, Color.parseColor("#FFC5E1A5"), Color.parseColor("#FF827717")))
        buildings.add(Building("school", PersianTexts.SCHOOL, BuildingType.SCHOOL,
            2250f, 600f, 240f, 200f, Color.parseColor("#FFFFE082"), Color.parseColor("#FFF57F17")))
        buildings.add(Building("car_dealer", PersianTexts.CAR_DEALER, BuildingType.CAR_DEALER,
            2900f, 600f, 260f, 200f, Color.parseColor("#FFB0BEC5"), Color.parseColor("#FF1B5E20")))
        buildings.add(Building("garage", PersianTexts.GARAGE, BuildingType.GARAGE,
            3200f, 600f, 240f, 200f, Color.parseColor("#FFB0BEC5"), Color.parseColor("#FF37474F")))

        // ردیف سوم (y ~ 1300-1780): عمومی و خاص
        buildings.add(Building("job_center", PersianTexts.JOB_CENTER, BuildingType.JOB_CENTER,
            200f, 1300f, 260f, 200f, Color.parseColor("#FFB3E5FC"), Color.parseColor("#FF0277BD")))
        buildings.add(Building("park", PersianTexts.PARK, BuildingType.PARK,
            650f, 1300f, 280f, 220f, Color.parseColor("#FFC5E1A5"), Color.parseColor("#FF33691E")))
        buildings.add(Building("helipad", PersianTexts.HELIPAD, BuildingType.HELIPAD,
            1100f, 1300f, 280f, 220f, Color.parseColor("#FFBDBDBD"), Color.parseColor("#FF424242")))

        // ==== درخت‌ها (پراکنده در فضاهای خالی) ====
        val rng = Random(42)
        // اطراف پارک
        repeat(8) {
            trees.add(Tree(680f + rng.nextFloat() * 220f, 1540f + rng.nextFloat() * 40f, 25f + rng.nextFloat() * 15f))
        }
        // کنار خیابان‌ها
        repeat(30) {
            val x = rng.nextFloat() * GameData.WORLD_WIDTH
            val y = if (rng.nextBoolean()) 380f - 15f else 1240f + 5f
            trees.add(Tree(x, y, 22f))
        }
        // گوشه‌های شهر
        repeat(15) {
            val x = rng.nextFloat() * GameData.WORLD_WIDTH
            val y = rng.nextFloat() * 200f + 50f
            // پرهیز از ساختمان‌ها
            val overlap = buildings.any { b -> x > b.x - 30f && x < b.x + b.width + 30f && y > b.y - 30f && y < b.y + b.height + 30f }
            if (!overlap) trees.add(Tree(x, y, 25f))
        }

        // ==== چراغ‌های خیابون ====
        for (x in 200..GameData.WORLD_WIDTH.toInt() step 400) {
            lamps.add(StreetLamp(x.toFloat(), 380f))
            lamps.add(StreetLamp(x.toFloat() + 50f, 580f))
            lamps.add(StreetLamp(x.toFloat(), 1080f))
            lamps.add(StreetLamp(x.toFloat() + 50f, 1280f))
            lamps.add(StreetLamp(x.toFloat(), 1780f))
            lamps.add(StreetLamp(x.toFloat() + 50f, 1980f))
        }

        // ==== NPC ها ====
        spawnNPCs()

        // ==== ماشین‌های پارک شده ====
        spawnParkedVehicles()
    }

    private fun spawnNPCs() {
        // فروشنده لباس
        npcs.add(NPC(500f, 350f, "رضا", NPC.Role.SHOPKEEPER,
            shirtColor = Color.parseColor("#FF7B1FA2")))
        // آرایشگر
        npcs.add(NPC(1300f, 350f, "مریم", NPC.Role.BARBER,
            shirtColor = Color.parseColor("#FFC2185B")))
        // آشپز
        npcs.add(NPC(1010f, 350f, "حسن", NPC.Role.CHEF,
            shirtColor = Color.WHITE))
        // پلیس
        npcs.add(NPC(510f, 850f, "افسر کریمی", NPC.Role.POLICE,
            shirtColor = Color.parseColor("#FF1A237E")))
        // بانکیار
        npcs.add(NPC(220f, 850f, "خانم احمدی", NPC.Role.BANKER,
            shirtColor = Color.parseColor("#FF1565C0")))
        // فروشنده ماشین
        npcs.add(NPC(3030f, 850f, "علی", NPC.Role.DEALER,
            shirtColor = Color.parseColor("#FF1B5E20")))
        // شهروندان عادی
        val names = listOf("محمد", "فاطمه", "زهرا", "حسین", "علی", "سارا", "رضا", "نازنین", "بابک", "مهسارا", "امیر", "دانیال")
        val rng = Random(7)
        repeat(20) { i ->
            val x = rng.nextFloat() * GameData.WORLD_WIDTH
            val y = 600f + rng.nextFloat() * 1500f
            // فقط روی خیابان‌ها یا پیاده‌روها
            val onRoad = roads.any { r -> r.rect.contains(x, y) }
            val onSidewalk = roads.any { r -> r.rect.left - 30 < x && x < r.rect.right + 30 && r.rect.top - 100 < y && y < r.rect.bottom + 100 }
            if (onRoad || onSidewalk) {
                npcs.add(NPC(
                    x.coerceIn(100f, GameData.WORLD_WIDTH - 100f),
                    y.coerceIn(600f, 2100f),
                    names[i % names.size],
                    if (i % 7 == 0) NPC.Role.CHILD else if (i % 11 == 0) NPC.Role.ELDER else NPC.Role.CIVILIAN
                ))
            }
        }
    }

    private fun spawnParkedVehicles() {
        // چند ماشین کنار خیابان‌ها پارک شده
        val rng = Random(11)
        val carDefs = GameData.VEHICLES.filter { it.type == GameData.VehicleType.CAR || it.type == GameData.VehicleType.MOTORCYCLE }
        repeat(8) {
            val def = carDefs[rng.nextInt(carDefs.size)]
            val x = 200f + rng.nextFloat() * (GameData.WORLD_WIDTH - 400f)
            val y = 520f + rng.nextFloat() * 20f  // کنار اولین خیابان
            parkedVehicles.add(Vehicle(def, x, y, def.color))
        }
    }

    /** یافتن ساختمان با ID */
    fun getBuildingById(id: String): Building? = buildings.find { it.id == id }

    /** یافتن ساختمان نزدیک به بازیکن */
    fun getNearbyBuilding(px: Float, py: Float, range: Float = 80f): Building? {
        return buildings.find { it.isAtDoor(px, py, range) }
    }

    /** یافتن NPC نزدیک */
    fun getNearbyNPC(px: Float, py: Float, range: Float = 70f): NPC? {
        return npcs.find { it.isInRange(px, py, range) }
    }

    /** آیا مختصات روی خیابان است؟ */
    fun isOnRoad(x: Float, y: Float): Boolean {
        return roads.any { it.rect.contains(x, y) }
    }

    /** آیا مختصات با ساختمان برخورد دارد؟ */
    fun collidesWithBuilding(x: Float, y: Float, r: Float): Boolean {
        return buildings.any { b ->
            x > b.x - r && x < b.x + b.width + r && y > b.y - r && y < b.y + b.height + r
        }
    }

    /** رسم کل نقشه */
    fun draw(canvas: Canvas, paint: Paint, cameraX: Float, cameraY: Float, viewW: Float, viewH: Float, dayNight: DayNightCycle) {
        // زمین چمن
        paint.color = Color.parseColor("#FF6FBF5C")
        canvas.drawRect(0f, 0f, viewW, viewH, paint)

        // پیاده‌روها (نوارهای روشن‌تر کنار خیابان‌ها)
        paint.color = Color.parseColor("#FFD4D4D4")
        for (road in roads) {
            val sx = road.rect.left - cameraX
            val sy = road.rect.top - cameraY
            // پیاده‌روی چپ و راست
            val sidewalk = 20f
            if (road.horizontal) {
                canvas.drawRect(sx - sidewalk, sy - sidewalk, sx + road.rect.width() + sidewalk, sy, paint)
                canvas.drawRect(sx - sidewalk, sy + road.rect.height(), sx + road.rect.width() + sidewalk, sy + road.rect.height() + sidewalk, paint)
            } else {
                canvas.drawRect(sx - sidewalk, sy - sidewalk, sx, sy + road.rect.height() + sidewalk, paint)
                canvas.drawRect(sx + road.rect.width(), sy - sidewalk, sx + road.rect.width() + sidewalk, sy + road.rect.height() + sidewalk, paint)
            }
        }

        // خیابان‌ها
        paint.color = Color.parseColor("#FF6E6E6E")
        for (road in roads) {
            val sx = road.rect.left - cameraX
            val sy = road.rect.top - cameraY
            canvas.drawRect(sx, sy, sx + road.rect.width(), sy + road.rect.height(), paint)

            // خط‌چین وسط خیابان
            paint.color = Color.parseColor("#FFFFFFF0")
            if (road.horizontal) {
                val cy = sy + road.rect.height() / 2
                var x = sx
                paint.strokeWidth = 4f
                while (x < sx + road.rect.width()) {
                    canvas.drawLine(x, cy, x + 30, cy, paint)
                    x += 60
                }
            } else {
                val cx = sx + road.rect.width() / 2
                var y = sy
                paint.strokeWidth = 4f
                while (y < sy + road.rect.height()) {
                    canvas.drawLine(cx, y, cx, y + 30, paint)
                    y += 60
                }
            }
        }

        // چراغ‌های خیابون (با درخشش در شب)
        for (lamp in lamps) {
            drawLamp(canvas, paint, lamp.x - cameraX, lamp.y - cameraY, dayNight.isNight())
        }

        // درخت‌ها
        for (t in trees) {
            if (isOnScreen(t.x, t.y, cameraX, cameraY, viewW, viewH)) {
                drawTree(canvas, paint, t.x - cameraX, t.y - cameraY, t.size)
            }
        }

        // ساختمان‌ها
        for (b in buildings) {
            if (isOnScreen(b.x, b.y, cameraX, cameraY, viewW, viewH, b.width + 50)) {
                b.draw(canvas, paint, b.x - cameraX, b.y - cameraY)
            }
        }
    }

    private fun isOnScreen(x: Float, y: Float, cx: Float, cy: Float, vw: Float, vh: Float, margin: Float = 100f): Boolean {
        return x - cx > -margin && x - cx < vw + margin && y - cy > -margin && y - cy < vh + margin
    }

    private fun drawTree(canvas: Canvas, paint: Paint, x: Float, y: Float, size: Float) {
        // سایه
        paint.color = Color.argb(70, 0, 0, 0)
        canvas.drawOval(RectF(x - size, y + size, x + size, y + size + 8), paint)
        // تنه
        paint.color = Color.parseColor("#FF6D4C2F")
        canvas.drawRect(RectF(x - 4, y, x + 4, y + size), paint)
        // شاخ و برگ (سه دایره)
        paint.color = Color.parseColor("#FF2E7D32")
        canvas.drawCircle(x, y - size * 0.3f, size * 0.8f, paint)
        paint.color = Color.parseColor("#FF388E3C")
        canvas.drawCircle(x - size * 0.5f, y, size * 0.6f, paint)
        canvas.drawCircle(x + size * 0.5f, y, size * 0.6f, paint)
        // نقطه روشن
        paint.color = Color.parseColor("#FF66BB6A")
        canvas.drawCircle(x - size * 0.2f, y - size * 0.5f, size * 0.2f, paint)
    }

    private fun drawLamp(canvas: Canvas, paint: Paint, x: Float, y: Float, lit: Boolean) {
        // پایه چراغ
        paint.color = Color.parseColor("#FF424242")
        canvas.drawRect(RectF(x - 2, y, x + 2, y + 40), paint)
        // سر چراغ
        paint.color = Color.parseColor("#FF616161")
        canvas.drawCircle(x, y, 5f, paint)
        if (lit) {
            // نور
            paint.color = Color.argb(80, 255, 235, 100)
            canvas.drawCircle(x, y + 5, 40f, paint)
            paint.color = Color.argb(160, 255, 245, 150)
            canvas.drawCircle(x, y + 5, 18f, paint)
            // لامپ
            paint.color = Color.parseColor("#FFFFEB3B")
            canvas.drawCircle(x, y, 3f, paint)
        } else {
            // خاموش
            paint.color = Color.parseColor("#FF757575")
            canvas.drawCircle(x, y, 3f, paint)
        }
    }

    /** رسم لایه تیرگی شب - روی همه چیز */
    fun drawNightOverlay(canvas: Canvas, paint: Paint, cameraX: Float, cameraY: Float, viewW: Float, viewH: Float, dayNight: DayNightCycle) {
        val tint = dayNight.ambientTint()
        if (Color.alpha(tint) > 0) {
            paint.color = tint
            canvas.drawRect(0f, 0f, viewW, viewH, paint)

            // چراغ‌های روشن - هاله نور روی تیرگی
            if (dayNight.isNight()) {
                for (lamp in lamps) {
                    if (isOnScreen(lamp.x, lamp.y, cameraX, cameraY, viewW, viewH)) {
                        val sx = lamp.x - cameraX
                        val sy = lamp.y - cameraY
                        // پاک کردن تیرگی با نور
                        paint.color = Color.argb(100, 255, 235, 100)
                        canvas.drawCircle(sx, sy + 5, 60f, paint)
                        paint.color = Color.argb(50, 255, 245, 150)
                        canvas.drawCircle(sx, sy + 5, 120f, paint)
                    }
                }
                // نور پنجره‌های ساختمان‌ها
                for (b in buildings) {
                    if (isOnScreen(b.x, b.y, cameraX, cameraY, viewW, viewH, b.width + 50)) {
                        val sx = b.x - cameraX
                        val sy = b.y - cameraY
                        paint.color = Color.argb(120, 255, 235, 100)
                        canvas.drawRect(RectF(sx + 12, sy + 27, sx + 30, sy + 45), paint)
                        canvas.drawRect(RectF(sx + b.width - 30, sy + 27, sx + b.width - 12, sy + 45), paint)
                    }
                }
            }
        }
    }

    /** به‌روزرسانی همه NPC ها */
    fun update(dt: Float) {
        for (npc in npcs) {
            npc.update(dt)
        }
    }
}
