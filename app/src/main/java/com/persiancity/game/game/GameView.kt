package com.persiancity.game.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.persiancity.game.data.GameData
import com.persiancity.game.data.PersianTexts
import com.persiancity.game.entities.NPC
import com.persiancity.game.entities.Player
import com.persiancity.game.entities.Vehicle
import com.persiancity.game.systems.Economy
import com.persiancity.game.systems.JobSystem
import com.persiancity.game.systems.SaveSystem
import com.persiancity.game.world.Building
import com.persiancity.game.world.BuildingType
import com.persiancity.game.world.DayNightCycle
import com.persiancity.game.world.World
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sin

/**
 * نمای اصلی بازی - رندرینگ، ورودی، گیم‌لوپ
 * Main game view: SurfaceView-based renderer, input handler, game loop owner
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    // ==== موجودیت‌ها ====
    val player = Player()
    val world = World()
    val dayNight = DayNightCycle()
    val economy = Economy()
    val jobs = JobSystem()
    val saveSystem = SaveSystem(context)

    // ==== وسیله نقلیه فعلی بازیکن (اگر سوار است) ====
    var activeVehicle: Vehicle? = null

    // ==== دوربین ====
    var cameraX: Float = 0f
    var cameraY: Float = 0f
    var viewWidth: Float = 0f
    var viewHeight: Float = 0f

    // ==== ورودی ====
    private var joystickX: Float = 0f
    private var joystickY: Float = 0f
    private var joystickActive: Boolean = false
    private var joystickCenterX: Float = 0f
    private var joystickCenterY: Float = 0f
    private val joystickRadius: Float = 100f

    // ==== گیم‌لوپ ====
    private var loopThread: Thread? = null
    @Volatile private var running: Boolean = false
    private var lastTime: Long = 0L

    // ==== رابط کاربری فعلی ====
    enum class UIScreen {
        NONE, MENU, CHARACTER_CREATOR, DIALOG, SHOP_FOOD, SHOP_CLOTHES, SHOP_BARBER,
        SHOP_VEHICLE, GARAGE, BANK, JOB_CENTER, PROPERTY, PAUSE, TUTORIAL
    }
    var currentScreen: UIScreen = UIScreen.MENU
    var dialogText: String = ""
    var dialogSpeaker: String = ""
    var notification: String = ""
    var notificationTimer: Float = 0f
    var onExitToMenu: (() -> Unit)? = null

    // ==== نقاشی ====
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        holder.addCallback(this)
        isFocusable = true
        isFocusableInTouchMode = true
    }

    // ==== Callback های Surface ====
    override fun surfaceCreated(holder: SurfaceHolder) {
        viewWidth = width.toFloat()
        viewHeight = height.toFloat()
        if (currentScreen != UIScreen.MENU) {
            startGameLoop()
        } else {
            // رسم یک فریم منو به محض آماده شدن
            try {
                val canvas = holder.lockCanvas()
                if (canvas != null) {
                    drawMenu(canvas)
                    holder.unlockCanvasAndPost(canvas)
                }
            } catch (e: Exception) { /* ignore */ }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        viewWidth = width.toFloat()
        viewHeight = height.toFloat()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopGameLoop()
    }

    /** شروع گیم‌لوپ */
    fun startGameLoop() {
        if (running) return
        running = true
        lastTime = System.currentTimeMillis()
        loopThread = Thread {
            while (running) {
                val start = System.currentTimeMillis()
                val now = start
                val dt = ((now - lastTime) / 1000f).coerceAtMost(0.05f)
                lastTime = now

                update(dt)
                draw()

                val elapsed = System.currentTimeMillis() - start
                val targetMs = 16L
                if (elapsed < targetMs) {
                    try { Thread.sleep(targetMs - elapsed) } catch (e: Exception) {}
                }
            }
        }.also { it.isDaemon = true }
        loopThread?.start()
    }

    fun stopGameLoop() {
        running = false
        try { loopThread?.join(100) } catch (e: Exception) {}
        loopThread = null
    }

    /** به‌روزرسانی وضعیت بازی */
    private fun update(dt: Float) {
        if (currentScreen != UIScreen.NONE) return  // بازی متوقف در حالت منو

        // محاسبه نرمال‌شده جهت حرکت
        var mx = 0f
        var my = 0f
        if (joystickActive) {
            val dx = joystickX - joystickCenterX
            val dy = joystickY - joystickCenterY
            val len = hypot(dx, dy)
            if (len > 10f) {
                mx = (dx / len).coerceIn(-1f, 1f)
                my = (dy / len).coerceIn(-1f, 1f)
            }
        }

        if (player.isInVehicle && activeVehicle != null) {
            // رانندگی
            val v = activeVehicle!!
            v.update(dt, accel = 1f, steerX = mx, steerY = my)
            // بازیکن با ماشین حرکت می‌کند
            player.x = v.x
            player.y = v.y
        } else {
            player.update(dt, mx, my)
        }

        // به‌روزرسانی NPC ها
        world.update(dt)

        // به‌روزرسانی ماشین پارک شده‌ها
        for (v in world.parkedVehicles) v.update(dt, 0f, 0f, 0f)
        if (activeVehicle != null && !player.isInVehicle) {
            activeVehicle?.update(dt, 0f, 0f, 0f)
        }

        // چرخه شبانه‌روز
        val newDay = dayNight.update(dt)
        if (newDay) {
            economy.applyDailyInterest(player)
            val rent = economy.collectPropertyRent(player)
            if (rent > 0) {
                showNotification("${PersianTexts.NEW_DAY} - درآمد اجاره: $rent تومان")
            } else {
                showNotification(PersianTexts.NEW_DAY)
            }
            player.sleep()
        }

        // کاهش گرسنگی/انرژی
        player.decayStats(dt)
        if (player.health <= 0) {
            // بازیکن می‌میرد - احیا در بیمارستان
            player.health = 100
            player.money = (player.money * 0.9f).toLong()
            val hosp = world.getBuildingById("hospital")
            if (hosp != null) {
                player.x = hosp.entranceX
                player.y = hosp.entranceY
            }
            showNotification("بیهوش شدی! در بیمارستان احیا شدی.")
        }

        // به‌روزرسانی نوتیفیکیشن
        if (notificationTimer > 0) {
            notificationTimer -= dt
            if (notificationTimer <= 0) notification = ""
        }

        // به‌روزرسانی دوربین (با حرکت نرم)
        val targetCX = player.x - viewWidth / 2
        val targetCY = player.y - viewHeight / 2
        cameraX += (targetCX - cameraX) * 5f * dt
        cameraY += (targetCY - cameraY) * 5f * dt
        cameraX = cameraX.coerceIn(0f, (GameData.WORLD_WIDTH - viewWidth).coerceAtLeast(0f))
        cameraY = cameraY.coerceIn(0f, (GameData.WORLD_HEIGHT - viewHeight).coerceAtLeast(0f))
    }

    /** رسم کل صحنه */
    private fun draw() {
        val canvas = holder.lockCanvas() ?: return
        try {
            // آسمان
            paint.color = dayNight.skyColor()
            canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)

            if (currentScreen == UIScreen.MENU) {
                drawMenu(canvas)
                holder.unlockCanvasAndPost(canvas)
                return
            }

            // نقشه
            world.draw(canvas, paint, cameraX, cameraY, viewWidth, viewHeight, dayNight)

            // وسیله نقلیه بازیکن
            activeVehicle?.let { v ->
                if (isOnScreen(v.x, v.y)) {
                    v.draw(canvas, paint, v.x - cameraX, v.y - cameraY)
                }
            }

            // ماشین‌های پارک شده
            for (v in world.parkedVehicles) {
                if (isOnScreen(v.x, v.y)) {
                    v.draw(canvas, paint, v.x - cameraX, v.y - cameraY)
                }
            }

            // NPC ها
            for (npc in world.npcs) {
                if (isOnScreen(npc.x, npc.y)) {
                    npc.draw(canvas, paint, npc.x - cameraX, npc.y - cameraY)
                }
            }

            // بازیکن (اگر سوار ماشین نیست، یا اگر سوار است و می‌خواهیم نشونش بدیم)
            if (!player.isInVehicle) {
                player.draw(canvas, paint, player.x - cameraX, player.y - cameraY)
            }

            // لایه تیرگی شب
            world.drawNightOverlay(canvas, paint, cameraX, cameraY, viewWidth, viewHeight, dayNight)

            // جوی‌استیک (اگر بازی فعال است)
            drawJoystick(canvas)

            // دکمه اکشن
            drawActionButtons(canvas)

            // HUD
            drawHUD(canvas)

            // دیالوگ/منو
            when (currentScreen) {
                UIScreen.DIALOG -> drawDialog(canvas)
                UIScreen.SHOP_FOOD -> drawFoodShop(canvas)
                UIScreen.SHOP_CLOTHES -> drawClothingShop(canvas)
                UIScreen.SHOP_BARBER -> drawBarberShop(canvas)
                UIScreen.SHOP_VEHICLE -> drawVehicleShop(canvas)
                UIScreen.GARAGE -> drawGarage(canvas)
                UIScreen.BANK -> drawBank(canvas)
                UIScreen.JOB_CENTER -> drawJobCenter(canvas)
                UIScreen.PROPERTY -> drawPropertyMenu(canvas)
                UIScreen.CHARACTER_CREATOR -> drawCharacterCreator(canvas)
                UIScreen.PAUSE -> drawPause(canvas)
                UIScreen.TUTORIAL -> drawTutorial(canvas)
                UIScreen.NONE -> { /* nothing */ }
                UIScreen.MENU -> drawMenu(canvas)
            }

            // نوتیفیکیشن
            if (notification.isNotEmpty()) {
                drawNotification(canvas)
            }

        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun isOnScreen(x: Float, y: Float): Boolean {
        val sx = x - cameraX
        val sy = y - cameraY
        return sx > -100 && sx < viewWidth + 100 && sy > -100 && sy < viewHeight + 100
    }

    // ==================== جوی‌استیک مجازی ====================
    private fun drawJoystick(canvas: Canvas) {
        if (currentScreen != UIScreen.NONE) return

        val baseX = 160f
        val baseY = viewHeight - 160f

        // دایره پایه
        paint.color = Color.argb(80, 100, 100, 100)
        canvas.drawCircle(baseX, baseY, joystickRadius, paint)
        paint.color = Color.argb(140, 200, 200, 200)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawCircle(baseX, baseY, joystickRadius, paint)
        paint.style = Paint.Style.FILL

        // دسته جوی‌استیک
        val stickX = if (joystickActive) joystickX else baseX
        val stickY = if (joystickActive) joystickY else baseY
        paint.color = Color.parseColor("#FFFFFFFF")
        canvas.drawCircle(stickX, stickY, 35f, paint)
        paint.color = Color.parseColor("#FF4CAF50")
        canvas.drawCircle(stickX, stickY, 30f, paint)
    }

    private fun drawActionButtons(canvas: Canvas) {
        if (currentScreen != UIScreen.NONE) return

        val cx = viewWidth - 100f
        val cy = viewHeight - 100f

        // دکمه تعامل (E)
        val nearBuilding = world.getNearbyBuilding(player.x, player.y, 90f)
        val nearNpc = world.getNearbyNPC(player.x, player.y, 80f)
        val nearVehicle = findNearestEnterableVehicle()

        val actionLabel = when {
            nearBuilding != null -> PersianTexts.ENTER_BUILDING
            nearNpc != null -> PersianTexts.TALK
            nearVehicle != null -> PersianTexts.ENTER_VEHICLE
            else -> null
        }

        // دکمه اکشن اصلی (دایره بزرگ سبز)
        if (actionLabel != null) {
            paint.color = Color.parseColor("#FF4CAF50")
            canvas.drawCircle(cx, cy, 60f, paint)
            paint.color = Color.argb(100, 0, 0, 0)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            canvas.drawCircle(cx, cy, 60f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.WHITE
            paint.textSize = 22f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(actionLabel, cx, cy + 8f, paint)
            paint.textAlign = Paint.Align.LEFT
        }

        // دکمه کار (اگر شغل دارد)
        if (player.jobId != null) {
            val wcx = viewWidth - 230f
            val wcy = viewHeight - 80f
            paint.color = Color.parseColor("#FFFF9800")
            canvas.drawCircle(wcx, wcy, 45f, paint)
            paint.color = Color.WHITE
            paint.textSize = 18f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(PersianTexts.WORK, wcx, wcy + 6f, paint)
            paint.textAlign = Paint.Align.LEFT
        }

        // دکمه خروج از ماشین (اگر سوار است)
        if (player.isInVehicle) {
            paint.color = Color.parseColor("#FFEF6C00")
            canvas.drawCircle(viewWidth - 230f, viewHeight - 80f, 45f, paint)
            paint.color = Color.WHITE
            paint.textSize = 16f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(PersianTexts.EXIT_VEHICLE, viewWidth - 230f, viewHeight - 74f, paint)
            paint.textAlign = Paint.Align.LEFT
        }

        // دکمه منو (ذخیره)
        val mcx = viewWidth - 50f
        val mcy = 50f
        paint.color = Color.argb(160, 0, 0, 0)
        canvas.drawRect(RectF(mcx - 30f, mcy - 25f, mcx + 30f, mcy + 25f), paint)
        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("≡", mcx, mcy + 8f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    // ==================== HUD ====================
    private fun drawHUD(canvas: Canvas) {
        // نوار بالایی - پول، روز، زمان
        paint.color = Color.argb(180, 0, 0, 0)
        canvas.drawRect(0f, 0f, viewWidth, 50f, paint)

        // پول
        paint.color = Color.parseColor("#FFFFD54F")
        paint.textSize = 20f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("${PersianTexts.MONEY}: ${player.money} ت", 15f, 33f, paint)

        // زمان
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("${dayNight.timeString()} - ${dayNight.phaseName}", viewWidth / 2f, 33f, paint)

        // روز
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${PersianTexts.DAY} ${dayNight.day}", viewWidth - 80f, 33f, paint)
        paint.textAlign = Paint.Align.LEFT

        // نوار آمار - پایین چپ
        val barX = 15f
        val barY = 60f
        paint.color = Color.argb(140, 0, 0, 0)
        canvas.drawRect(RectF(barX, barY, barX + 200f, barY + 110f), paint)

        // سلامتی (قرمز)
        drawStatBar(canvas, barX + 10f, barY + 12f, 180f, PersianTexts.HEALTH, player.health, Color.parseColor("#FFE53935"))
        // گرسنگی (نارنجی)
        drawStatBar(canvas, barX + 10f, barY + 45f, 180f, PersianTexts.HUNGER, player.hunger, Color.parseColor("#FFFF9800"))
        // انرژی (آبی)
        drawStatBar(canvas, barX + 10f, barY + 78f, 180f, PersianTexts.ENERGY, player.energy, Color.parseColor("#FF2196F3"))

        // شغل
        val job = jobs.currentJob(player)
        if (job != null) {
            paint.color = Color.argb(140, 0, 0, 0)
            canvas.drawRect(RectF(barX, barY + 120f, barX + 200f, barY + 152f), paint)
            paint.color = Color.WHITE
            paint.textSize = 16f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("${PersianTexts.JOB}: ${job.name}", barX + 10f, barY + 142f, paint)
        }
    }

    private fun drawStatBar(canvas: Canvas, x: Float, y: Float, w: Float, label: String, value: Int, color: Int) {
        // پس‌زمینه
        paint.color = Color.argb(80, 0, 0, 0)
        canvas.drawRect(RectF(x, y, x + w, y + 20f), paint)
        // مقدار
        paint.color = color
        canvas.drawRect(RectF(x, y, x + w * (value / 100f), y + 20f), paint)
        // برچسب
        paint.color = Color.WHITE
        paint.textSize = 13f
        canvas.drawText("$label: $value", x + 5f, y + 15f, paint)
    }

    private fun drawNotification(canvas: Canvas) {
        if (notification.isEmpty()) return
        val py = 70f
        val padding = 20f
        paint.textSize = 18f
        paint.textAlign = Paint.Align.CENTER
        val tw = paint.measureText(notification)
        val bx = viewWidth / 2f
        paint.color = Color.argb(180, 200, 50, 50)
        canvas.drawRect(RectF(bx - tw/2 - padding, py - 22, bx + tw/2 + padding, py + 8), paint)
        paint.color = Color.WHITE
        canvas.drawText(notification, bx, py, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    // ==================== دیالوگ ====================
    private fun drawDialog(canvas: Canvas) {
        val bx = viewWidth * 0.1f
        val by = viewHeight - 220f
        val bw = viewWidth * 0.8f
        val bh = 180f

        paint.color = Color.argb(220, 30, 30, 40)
        canvas.drawRect(RectF(bx, by, bx + bw, by + bh), paint)
        paint.color = Color.parseColor("#FF4CAF50")
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        canvas.drawRect(RectF(bx, by, bx + bw, by + bh), paint)
        paint.style = Paint.Style.FILL

        // نام گوینده
        paint.color = Color.parseColor("#FF4CAF50")
        paint.textSize = 22f
        canvas.drawText(dialogSpeaker, bx + 20f, by + 30f, paint)

        // متن
        paint.color = Color.WHITE
        paint.textSize = 20f
        // شکستن خطوط طولانی
        val words = dialogText.split(" ")
        val line = StringBuilder()
        var lineY = by + 70f
        val maxWidth = bw - 40f
        for (w in words) {
            if (paint.measureText("$line $w".trim()) > maxWidth) {
                canvas.drawText(line.toString().trim(), bx + 20f, lineY, paint)
                line.clear()
                line.append(w)
                lineY += 30f
            } else {
                if (line.isNotEmpty()) line.append(" ")
                line.append(w)
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line.toString().trim(), bx + 20f, lineY, paint)
        }

        // دکمه باشه
        val okX = bx + bw - 100f
        val okY = by + bh - 30f
        paint.color = Color.parseColor("#FF4CAF50")
        canvas.drawRect(RectF(okX - 10f, okY - 25f, okX + 90f, okY + 5f), paint)
        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(PersianTexts.OK, okX + 40f, okY - 5f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    // ==================== منوی اصلی ====================
    private fun drawMenu(canvas: Canvas) {
        // پس‌زمینه نیمه‌شفاف
        paint.color = Color.argb(230, 30, 30, 50)
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)

        // عنوان
        paint.color = Color.parseColor("#FF4CAF50")
        paint.textSize = 64f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(PersianTexts.APP_TITLE, viewWidth / 2f, viewHeight / 2f - 100f, paint)

        // دکمه‌ها
        val buttons = if (saveSystem.hasSave()) {
            listOf(PersianTexts.CONTINUE_GAME, PersianTexts.NEW_GAME, PersianTexts.ABOUT, PersianTexts.EXIT)
        } else {
            listOf(PersianTexts.NEW_GAME, PersianTexts.ABOUT, PersianTexts.EXIT)
        }

        paint.textSize = 26f
        var by = viewHeight / 2f - 20f
        for (b in buttons) {
            val bw = 280f
            val bh = 50f
            val bx = viewWidth / 2f - bw / 2f
            paint.color = Color.parseColor("#FF2196F3")
            canvas.drawRect(RectF(bx, by, bx + bw, by + bh), paint)
            paint.color = Color.WHITE
            canvas.drawText(b, viewWidth / 2f, by + 33f, paint)
            by += 70f
        }
        paint.textAlign = Paint.Align.LEFT
    }

    // ==================== مغازه غذا ====================
    private fun drawFoodShop(canvas: Canvas) {
        drawShopBg(canvas, PersianTexts.RESTAURANT)
        val foods = GameData.FOODS
        var y = 180f
        for (f in foods) {
            drawShopItemRow(canvas, 100f, y, viewWidth - 200f, f.name, "${f.price} ت", "مقدار: +${f.hungerRestore} گرسنگی")
            y += 65f
        }
        drawCloseButton(canvas)
    }

    private fun drawClothingShop(canvas: Canvas) {
        drawShopBg(canvas, PersianTexts.CLOTHING_SHOP)
        var y = 180f
        for (c in GameData.CLOTHING) {
            // کادر رنگ
            paint.color = c.color
            canvas.drawRect(RectF(100f, y + 10f, 130f, y + 40f), paint)
            drawShopItemRow(canvas, 100f, y, viewWidth - 200f, c.name, "${c.price} ت", "هنگام خرید روی شما می‌شه", showColorBox = false)
            y += 65f
        }
        drawCloseButton(canvas)
    }

    private fun drawBarberShop(canvas: Canvas) {
        drawShopBg(canvas, PersianTexts.BARBERSHOP)
        var y = 180f
        for (b in GameData.BARBER_SERVICES) {
            drawShopItemRow(canvas, 100f, y, viewWidth - 200f, b.name, "${b.price} ت", "خدمات آرایشی")
            y += 65f
        }
        // انتخاب مدل مو و رنگ مو
        paint.color = Color.WHITE
        paint.textSize = 20f
        canvas.drawText("رنگ مو:", 100f, y + 30f, paint)
        var cx = 200f
        for (c in GameData.HAIR_COLORS) {
            paint.color = c
            canvas.drawRect(RectF(cx, y + 10f, cx + 40f, y + 40f), paint)
            if (player.hairColor == c) {
                paint.color = Color.WHITE
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                canvas.drawRect(RectF(cx, y + 10f, cx + 40f, y + 40f), paint)
                paint.style = Paint.Style.FILL
            }
            cx += 50f
        }
        y += 70f
        paint.color = Color.WHITE
        canvas.drawText("مدل مو: ${player.hairStyle + 1}/3", 100f, y + 30f, paint)
        // دکمه تغییر مدل مو
        paint.color = Color.parseColor("#FF2196F3")
        canvas.drawRect(RectF(280f, y + 10f, 380f, y + 40f), paint)
        paint.color = Color.WHITE
        canvas.drawText("تغییر", 295f, y + 32f, paint)
        drawCloseButton(canvas)
    }

    private fun drawVehicleShop(canvas: Canvas) {
        drawShopBg(canvas, PersianTexts.CAR_DEALER)
        var y = 180f
        for (v in GameData.VEHICLES) {
            val owned = player.ownedVehicles.contains(v.id)
            val status = if (owned) "دارد" else "${v.price} ت"
            val desc = v.name + " - سرعت: ${v.speed}"
            drawShopItemRow(canvas, 100f, y, viewWidth - 200f, desc, status, if (owned) "خریداری شده" else "برای خرید لمس کن", owned = owned)
            y += 65f
        }
        drawCloseButton(canvas)
    }

    private fun drawGarage(canvas: Canvas) {
        drawShopBg(canvas, PersianTexts.CUSTOMIZE)
        // انتخاب ماشین
        paint.color = Color.WHITE
        paint.textSize = 20f
        canvas.drawText("ماشین‌های شما:", 100f, 170f, paint)
        var cx = 100f
        val cy = 180f
        for (vid in player.ownedVehicles) {
            val def = GameData.VEHICLES.find { it.id == vid } ?: continue
            paint.color = player.vehicleColors[vid] ?: def.color
            canvas.drawRect(RectF(cx, cy, cx + 90f, cy + 40f), paint)
            paint.color = Color.WHITE
            paint.textSize = 14f
            canvas.drawText(def.name, cx + 5f, cy + 60f, paint)
            cx += 110f
        }

        // رنگ‌ها
        paint.color = Color.WHITE
        paint.textSize = 20f
        canvas.drawText("رنگ ماشین:", 100f, 280f, paint)
        cx = 100f
        for (c in GameData.VEHICLE_COLORS) {
            paint.color = c
            canvas.drawRect(RectF(cx, 300f, cx + 50f, 340f), paint)
            cx += 60f
        }

        // ارتقاها
        paint.color = Color.WHITE
        paint.textSize = 20f
        canvas.drawText("ارتقاها:", 100f, 400f, paint)
        var y = 420f
        for (u in GameData.VEHICLE_UPGRADES) {
            drawShopItemRow(canvas, 100f, y, viewWidth - 200f, u.name, "${u.price} ت", u.desc)
            y += 65f
        }
        drawCloseButton(canvas)
    }

    private fun drawBank(canvas: Canvas) {
        drawShopBg(canvas, PersianTexts.BANK)
        paint.color = Color.WHITE
        paint.textSize = 20f
        canvas.drawText("${PersianTexts.Bank.BALANCE}: ${player.bankBalance} ت", 100f, 170f, paint)
        canvas.drawText("${PersianTexts.Bank.LOAN}: ${player.loan} ت", 100f, 200f, paint)
        canvas.drawText("${PersianTexts.MONEY}: ${player.money} ت", 100f, 230f, paint)
        canvas.drawText("${PersianTexts.Bank.LOAN_LIMIT}: ${economy.maxLoan(player)} ت", 100f, 260f, paint)

        // دکمه‌های عملیات
        val potentialRent = player.ownedProperties.sumOf { propId ->
            GameData.PROPERTIES.find { it.id == propId }?.rentPerDay?.toLong() ?: 0L
        }
        val buttons = listOf(
            Triple(PersianTexts.Bank.DEPOSIT, "deposit", Color.parseColor("#FF4CAF50")),
            Triple(PersianTexts.Bank.WITHDRAW, "withdraw", Color.parseColor("#FF2196F3")),
            Triple(PersianTexts.Bank.TAKE_LOAN, "loan", Color.parseColor("#FFFF9800")),
            Triple(PersianTexts.Bank.REPAY_LOAN, "repay", Color.parseColor("#FFEF6C00")),
            Triple(PersianTexts.Property.COLLECT_RENT + " ($potentialRent ت)", "rent", Color.parseColor("#FF9C27B0"))
        )
        var y = 320f
        for ((label, _, color) in buttons) {
            paint.color = color
            canvas.drawRect(RectF(100f, y, viewWidth - 100f, y + 50f), paint)
            paint.color = Color.WHITE
            paint.textSize = 20f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(label, viewWidth / 2f, y + 32f, paint)
            paint.textAlign = Paint.Align.LEFT
            y += 60f
        }
        drawCloseButton(canvas)
    }

    private fun drawJobCenter(canvas: Canvas) {
        drawShopBg(canvas, PersianTexts.JOB_CENTER)
        paint.color = Color.WHITE
        paint.textSize = 20f
        canvas.drawText("شغل فعلی: ${jobs.currentJob(player)?.name ?: PersianTexts.JOB_UNEMPLOYED}", 100f, 170f, paint)

        var y = 220f
        for (job in jobs.allJobs) {
            val isCurrent = jobs.currentJob(player)?.id == job.id
            val bg = if (isCurrent) Color.parseColor("#FF4CAF50") else Color.parseColor("#FF2196F3")
            paint.color = bg
            canvas.drawRect(RectF(100f, y, viewWidth - 100f, y + 60f), paint)
            paint.color = Color.WHITE
            paint.textSize = 20f
            canvas.drawText(job.name, 120f, y + 25f, paint)
            paint.textSize = 16f
            canvas.drawText(job.description, 120f, y + 50f, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(job.reward, viewWidth - 120f, y + 35f, paint)
            paint.textAlign = Paint.Align.LEFT
            y += 70f
        }
        drawCloseButton(canvas)
    }

    private fun drawPropertyMenu(canvas: Canvas) {
        drawShopBg(canvas, PersianTexts.Property.BUY_PROPERTY)
        var y = 180f
        for (p in GameData.PROPERTIES) {
            val owned = player.ownedProperties.contains(p.id)
            val status = if (owned) "متعلق به شما" else "${p.price} ت"
            drawShopItemRow(canvas, 100f, y, viewWidth - 200f, p.name, status, "اجاره روزانه: ${p.rentPerDay} ت", owned = owned)
            y += 65f
        }
        drawCloseButton(canvas)
    }

    private fun drawCharacterCreator(canvas: Canvas) {
        drawShopBg(canvas, "ایجاد شخصیت")
        paint.color = Color.WHITE
        paint.textSize = 20f

        // پیش‌نمایش شخصیت در وسط
        canvas.save()
        canvas.translate(viewWidth / 2f - player.x, 300f - player.y)
        // یک نسخه موقت از بازیکن برای پیش‌نمایش
        canvas.restore()
        // رسم دستی شخصیت
        canvas.save()
        canvas.translate(viewWidth / 2f - 200f, 0f)
        // (ساده‌سازی: فقط نشان می‌دهیم)
        canvas.restore()

        var y = 180f
        canvas.drawText(PersianTexts.Customize.CHOOSE_GENDER + ": ${if (player.gender == Player.Gender.MALE) PersianTexts.Customize.MALE else PersianTexts.Customize.FEMALE}", 100f, y, paint)
        paint.color = Color.parseColor("#FF2196F3")
        canvas.drawRect(RectF(viewWidth - 250f, y - 25f, viewWidth - 100f, y + 5f), paint)
        paint.color = Color.WHITE
        canvas.drawText("تغییر", viewWidth - 230f, y - 5f, paint)
        y += 60f

        // رنگ مو
        paint.color = Color.WHITE
        canvas.drawText(PersianTexts.Customize.CHOOSE_HAIR + ":", 100f, y, paint)
        var cx = 250f
        for (c in GameData.HAIR_COLORS) {
            paint.color = c
            canvas.drawRect(RectF(cx, y - 25f, cx + 40f, y + 5f), paint)
            if (player.hairColor == c) {
                paint.color = Color.WHITE
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                canvas.drawRect(RectF(cx, y - 25f, cx + 40f, y + 5f), paint)
                paint.style = Paint.Style.FILL
            }
            cx += 50f
        }
        y += 60f

        // رنگ لباس
        paint.color = Color.WHITE
        canvas.drawText(PersianTexts.Customize.CHOOSE_SHIRT + ":", 100f, y, paint)
        cx = 250f
        for (c in GameData.SHIRT_COLORS) {
            paint.color = c
            canvas.drawRect(RectF(cx, y - 25f, cx + 40f, y + 5f), paint)
            if (player.shirtColor == c) {
                paint.color = Color.WHITE
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                canvas.drawRect(RectF(cx, y - 25f, cx + 40f, y + 5f), paint)
                paint.style = Paint.Style.FILL
            }
            cx += 50f
        }
        y += 60f

        // رنگ شلوار
        paint.color = Color.WHITE
        canvas.drawText(PersianTexts.Customize.CHOOSE_PANTS + ":", 100f, y, paint)
        cx = 250f
        for (c in GameData.PANTS_COLORS) {
            paint.color = c
            canvas.drawRect(RectF(cx, y - 25f, cx + 40f, y + 5f), paint)
            if (player.pantsColor == c) {
                paint.color = Color.WHITE
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                canvas.drawRect(RectF(cx, y - 25f, cx + 40f, y + 5f), paint)
                paint.style = Paint.Style.FILL
            }
            cx += 50f
        }
        y += 60f

        // رنگ پوست
        paint.color = Color.WHITE
        canvas.drawText("رنگ پوست:", 100f, y, paint)
        cx = 250f
        for (c in GameData.SKIN_COLORS) {
            paint.color = c
            canvas.drawRect(RectF(cx, y - 25f, cx + 40f, y + 5f), paint)
            if (player.skinColor == c) {
                paint.color = Color.WHITE
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                canvas.drawRect(RectF(cx, y - 25f, cx + 40f, y + 5f), paint)
                paint.style = Paint.Style.FILL
            }
            cx += 50f
        }
        y += 80f

        // دکمه شروع
        paint.color = Color.parseColor("#FF4CAF50")
        canvas.drawRect(RectF(viewWidth / 2f - 100f, y, viewWidth / 2f + 100f, y + 60f), paint)
        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("شروع بازی", viewWidth / 2f, y + 38f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawPause(canvas: Canvas) {
        paint.color = Color.argb(200, 0, 0, 0)
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)
        paint.color = Color.WHITE
        paint.textSize = 48f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("توقف", viewWidth / 2f, viewHeight / 2f - 80f, paint)
        paint.textSize = 24f
        val items = listOf(PersianTexts.SAVE, PersianTexts.CONTINUE_GAME, PersianTexts.SETTINGS, PersianTexts.EXIT_GAME)
        var y = viewHeight / 2f - 20f
        for (it in items) {
            paint.color = Color.parseColor("#FF2196F3")
            canvas.drawRect(RectF(viewWidth / 2f - 140f, y, viewWidth / 2f + 140f, y + 50f), paint)
            paint.color = Color.WHITE
            canvas.drawText(it, viewWidth / 2f, y + 33f, paint)
            y += 70f
        }
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawTutorial(canvas: Canvas) {
        paint.color = Color.argb(220, 30, 30, 50)
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)
        paint.color = Color.parseColor("#FF4CAF50")
        paint.textSize = 32f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(PersianTexts.Tutorial.WELCOME_TITLE, viewWidth / 2f, 120f, paint)
        paint.color = Color.WHITE
        paint.textSize = 20f
        val lines = PersianTexts.Tutorial.WELCOME_BODY.split(".")
        var y = 200f
        for (line in lines) {
            if (line.isBlank()) continue
            canvas.drawText(line.trim() + ".", viewWidth / 2f, y, paint)
            y += 35f
        }
        y += 30f
        canvas.drawText(PersianTexts.Tutorial.FIND_JOB, viewWidth / 2f, y, paint)
        y += 30f
        canvas.drawText(PersianTexts.Tutorial.BUY_CAR, viewWidth / 2f, y, paint)
        y += 30f
        canvas.drawText(PersianTexts.Tutorial.SAVE_REGULARLY, viewWidth / 2f, y, paint)

        // دکمه شروع
        y += 60f
        paint.color = Color.parseColor("#FF4CAF50")
        canvas.drawRect(RectF(viewWidth / 2f - 100f, y, viewWidth / 2f + 100f, y + 60f), paint)
        paint.color = Color.WHITE
        paint.textSize = 24f
        canvas.drawText("بزن بریم!", viewWidth / 2f, y + 38f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    // ==================== ابزارهای کمکی رابط کاربری ====================
    private fun drawShopBg(canvas: Canvas, title: String) {
        paint.color = Color.argb(220, 30, 30, 50)
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)
        // عنوان
        paint.color = Color.parseColor("#FF4CAF50")
        paint.textSize = 32f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(title, viewWidth / 2f, 100f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawShopItemRow(canvas: Canvas, x: Float, y: Float, w: Float, name: String, price: String, desc: String, showColorBox: Boolean = true, owned: Boolean = false) {
        paint.color = Color.argb(160, 60, 60, 80)
        canvas.drawRect(RectF(x, y, x + w, y + 55f), paint)
        paint.color = Color.WHITE
        paint.textSize = 20f
        canvas.drawText(name, x + 20f, y + 22f, paint)
        paint.textSize = 14f
        canvas.drawText(desc, x + 20f, y + 42f, paint)
        // قیمت/وضعیت در سمت راست
        paint.color = if (owned) Color.parseColor("#FF4CAF50") else Color.parseColor("#FFFFD54F")
        paint.textSize = 18f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(price, x + w - 20f, y + 22f, paint)
        paint.textAlign = Paint.Align.LEFT
        // آیکن خرید
        if (!owned) {
            paint.color = Color.parseColor("#FF4CAF50")
            canvas.drawRect(RectF(x + w - 80f, y + 30f, x + w - 20f, y + 50f), paint)
            paint.color = Color.WHITE
            paint.textSize = 14f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(PersianTexts.BUY, x + w - 50f, y + 44f, paint)
            paint.textAlign = Paint.Align.LEFT
        }
    }

    private fun drawCloseButton(canvas: Canvas) {
        val bx = viewWidth - 130f
        val by = viewHeight - 80f
        paint.color = Color.parseColor("#FFF44336")
        canvas.drawRect(RectF(bx, by, bx + 100f, by + 50f), paint)
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(PersianTexts.CLOSE, bx + 50f, by + 32f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    // ==================== نمایش نوتیفیکیشن ====================
    fun showNotification(text: String) {
        notification = text
        notificationTimer = 3f
    }

    // ==================== جستجوی ماشین قابل سوار شدن ====================
    fun findNearestEnterableVehicle(): Vehicle? {
        var best: Vehicle? = null
        var bestDist = 100f * 100f
        // ماشین‌های پارک شده که متعلق به بازیکن است
        for (v in world.parkedVehicles) {
            val def = v.def
            if (!player.ownedVehicles.contains(def.id)) continue
            val dx = v.x - player.x
            val dy = v.y - player.y
            val d = dx * dx + dy * dy
            if (d < bestDist) {
                bestDist = d
                best = v
            }
        }
        // وسیله نقلیه فعلی بازیکن (اگر پیاده شده)
        if (activeVehicle != null && !player.isInVehicle) {
            val v = activeVehicle!!
            val dx = v.x - player.x
            val dy = v.y - player.y
            val d = dx * dx + dy * dy
            if (d < bestDist) {
                bestDist = d
                best = v
            }
        }
        return best
    }

    // ==================== مدیریت ورودی لمسی ====================
    fun handleTouch(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val x = event.x
                val y = event.y
                return handleTap(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                if (joystickActive) {
                    joystickX = event.x
                    joystickY = event.y
                    val dx = joystickX - joystickCenterX
                    val dy = joystickY - joystickCenterY
                    val len = hypot(dx, dy)
                    if (len > joystickRadius) {
                        joystickX = joystickCenterX + dx / len * joystickRadius
                        joystickY = joystickCenterY + dy / len * joystickRadius
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                joystickActive = false
            }
        }
        return true
    }

    private fun handleTap(x: Float, y: Float): Boolean {
        // اگر در منوی اصلی
        if (currentScreen == UIScreen.MENU) {
            return handleMenuTap(x, y)
        }
        // اگر در توقف
        if (currentScreen == UIScreen.PAUSE) {
            return handlePauseTap(x, y)
        }
        // اگر در آموزش
        if (currentScreen == UIScreen.TUTORIAL) {
            currentScreen = UIScreen.NONE
            return true
        }
        // اگر در شخصیت‌ساز
        if (currentScreen == UIScreen.CHARACTER_CREATOR) {
            return handleCharacterCreatorTap(x, y)
        }
        // اگر در یک مغازه/منو
        if (currentScreen != UIScreen.NONE) {
            return handleShopTap(x, y)
        }

        // دکمه منو ( گوشه راست بالا)
        if (x > viewWidth - 80f && y < 100f) {
            currentScreen = UIScreen.PAUSE
            return true
        }

        // دکمه کار (اگر شغل دارد)
        if (player.jobId != null) {
            val wcx = viewWidth - 230f
            val wcy = viewHeight - 80f
            if (hypot(x - wcx, y - wcy) < 50f) {
                val (success, msg, _) = jobs.performWork(player, world)
                showNotification(msg)
                return true
            }
        }
        // دکمه خروج از ماشین
        if (player.isInVehicle) {
            val ecx = viewWidth - 230f
            val ecy = viewHeight - 80f
            if (hypot(x - ecx, y - ecy) < 50f) {
                exitVehicle()
                return true
            }
        }

        // دکمه اکشن
        val cx = viewWidth - 100f
        val cy = viewHeight - 100f
        if (hypot(x - cx, y - cy) < 70f) {
            performAction()
            return true
        }

        // لمس جوی‌استیک (هرکجای پایین چپ)
        if (x < viewWidth * 0.4f && y > viewHeight * 0.5f) {
            joystickActive = true
            joystickCenterX = 160f
            joystickCenterY = viewHeight - 160f
            joystickX = x
            joystickY = y
            // محدود کردن دسته
            val dx = joystickX - joystickCenterX
            val dy = joystickY - joystickCenterY
            val len = hypot(dx, dy)
            if (len > joystickRadius) {
                joystickX = joystickCenterX + dx / len * joystickRadius
                joystickY = joystickCenterY + dy / len * joystickRadius
            }
            return true
        }
        return false
    }

    private fun handleMenuTap(x: Float, y: Float): Boolean {
        val buttons = if (saveSystem.hasSave()) {
            listOf(PersianTexts.CONTINUE_GAME, PersianTexts.NEW_GAME, PersianTexts.ABOUT, PersianTexts.EXIT)
        } else {
            listOf(PersianTexts.NEW_GAME, PersianTexts.ABOUT, PersianTexts.EXIT)
        }
        var by = viewHeight / 2f - 20f
        for (b in buttons) {
            val bw = 280f
            val bh = 50f
            val bx = viewWidth / 2f - bw / 2f
            if (x > bx && x < bx + bw && y > by && y < by + bh) {
                when (b) {
                    PersianTexts.CONTINUE_GAME -> {
                        saveSystem.load(player, dayNight)
                        currentScreen = UIScreen.NONE
                        startGameLoop()
                    }
                    PersianTexts.NEW_GAME -> {
                        currentScreen = UIScreen.CHARACTER_CREATOR
                    }
                    PersianTexts.ABOUT -> {
                        showNotification("شهر فارسی - بازی جهان باز دو بعدی فارسی")
                    }
                    PersianTexts.EXIT -> {
                        onExitToMenu?.invoke()
                    }
                }
                return true
            }
            by += 70f
        }
        return false
    }

    private fun handlePauseTap(x: Float, y: Float): Boolean {
        val items = listOf(PersianTexts.SAVE, PersianTexts.CONTINUE_GAME, PersianTexts.SETTINGS, PersianTexts.EXIT_GAME)
        var y2 = viewHeight / 2f - 20f
        for (it in items) {
            if (x > viewWidth / 2f - 140f && x < viewWidth / 2f + 140f && y > y2 && y < y2 + 50f) {
                when (it) {
                    PersianTexts.SAVE -> {
                        saveSystem.save(player, dayNight)
                        showNotification(PersianTexts.GAME_SAVED)
                        currentScreen = UIScreen.NONE
                    }
                    PersianTexts.CONTINUE_GAME -> {
                        currentScreen = UIScreen.NONE
                    }
                    PersianTexts.SETTINGS -> {
                        showNotification("تنظیمات به زودی!")
                    }
                    PersianTexts.EXIT_GAME -> {
                        saveSystem.save(player, dayNight)
                        stopGameLoop()
                        currentScreen = UIScreen.MENU
                    }
                }
                return true
            }
            y2 += 70f
        }
        return false
    }

    private fun handleCharacterCreatorTap(x: Float, y: Float): Boolean {
        var yy = 180f
        // دکمه تغییر جنسیت
        if (x > viewWidth - 250f && x < viewWidth - 100f && y > yy - 25f && y < yy + 5f) {
            player.gender = if (player.gender == Player.Gender.MALE) Player.Gender.FEMALE else Player.Gender.MALE
            return true
        }
        yy += 60f
        // رنگ مو
        if (y > yy - 25f && y < yy + 5f) {
            var cx = 250f
            for (c in GameData.HAIR_COLORS) {
                if (x > cx && x < cx + 40f) {
                    player.hairColor = c
                    return true
                }
                cx += 50f
            }
        }
        yy += 60f
        // رنگ لباس
        if (y > yy - 25f && y < yy + 5f) {
            var cx = 250f
            for (c in GameData.SHIRT_COLORS) {
                if (x > cx && x < cx + 40f) {
                    player.shirtColor = c
                    return true
                }
                cx += 50f
            }
        }
        yy += 60f
        // رنگ شلوار
        if (y > yy - 25f && y < yy + 5f) {
            var cx = 250f
            for (c in GameData.PANTS_COLORS) {
                if (x > cx && x < cx + 40f) {
                    player.pantsColor = c
                    return true
                }
                cx += 50f
            }
        }
        yy += 60f
        // رنگ پوست
        if (y > yy - 25f && y < yy + 5f) {
            var cx = 250f
            for (c in GameData.SKIN_COLORS) {
                if (x > cx && x < cx + 40f) {
                    player.skinColor = c
                    return true
                }
                cx += 50f
            }
        }
        yy += 80f
        // دکمه شروع
        if (x > viewWidth / 2f - 100f && x < viewWidth / 2f + 100f && y > yy && y < yy + 60f) {
            currentScreen = UIScreen.TUTORIAL
            startGameLoop()
            return true
        }
        return false
    }

    private fun handleShopTap(x: Float, y: Float): Boolean {
        // دکمه بستن
        val cbx = viewWidth - 130f
        val cby = viewHeight - 80f
        if (x > cbx && x < cbx + 100f && y > cby && y < cby + 50f) {
            currentScreen = UIScreen.NONE
            return true
        }

        when (currentScreen) {
            UIScreen.DIALOG -> {
                // بستن دیالوگ
                if (x > viewWidth * 0.1f + viewWidth * 0.8f - 110f && x < viewWidth * 0.1f + viewWidth * 0.8f + 10f
                    && y > viewHeight - 250f + 125f && y < viewHeight - 250f + 185f
                ) {
                    currentScreen = UIScreen.NONE
                    return true
                }
                // هرکجای دیالوگ
                currentScreen = UIScreen.NONE
                return true
            }
            UIScreen.SHOP_FOOD -> {
                // خرید غذا
                var yy = 180f
                for (f in GameData.FOODS) {
                    if (x > 100f && x < viewWidth - 100f && y > yy && y < yy + 55f) {
                        // خرید
                        if (player.canAfford(f.price.toLong())) {
                            player.spend(f.price.toLong())
                            player.eat(f.hungerRestore, f.healthBoost)
                            showNotification("${f.name} خوردی! ${PersianTexts.BOUGHT_ITEM}")
                        } else {
                            showNotification(PersianTexts.NOT_ENOUGH_MONEY)
                        }
                        return true
                    }
                    yy += 65f
                }
            }
            UIScreen.SHOP_CLOTHES -> {
                var yy = 180f
                for (c in GameData.CLOTHING) {
                    if (x > 100f && x < viewWidth - 100f && y > yy && y < yy + 55f) {
                        if (player.ownedClothing.contains(c.id)) {
                            // پوشیدن لباس
                            if (c.slot == GameData.ClothingSlot.SHIRT) player.shirtColor = c.color
                            if (c.slot == GameData.ClothingSlot.PANTS) player.pantsColor = c.color
                            showNotification("${c.name} پوشیدی!")
                        } else if (player.canAfford(c.price.toLong())) {
                            player.spend(c.price.toLong())
                            player.ownedClothing.add(c.id)
                            if (c.slot == GameData.ClothingSlot.SHIRT) player.shirtColor = c.color
                            if (c.slot == GameData.ClothingSlot.PANTS) player.pantsColor = c.color
                            showNotification("${c.name} خریداری شد!")
                        } else {
                            showNotification(PersianTexts.NOT_ENOUGH_MONEY)
                        }
                        return true
                    }
                    yy += 65f
                }
            }
            UIScreen.SHOP_BARBER -> {
                var yy = 180f
                for (b in GameData.BARBER_SERVICES) {
                    if (x > 100f && x < viewWidth - 100f && y > yy && y < yy + 55f) {
                        if (player.canAfford(b.price.toLong())) {
                            player.spend(b.price.toLong())
                            if (b.id == "color") {
                                player.hairColor = GameData.HAIR_COLORS.random()
                            }
                            if (b.id == "style") {
                                player.hairStyle = (player.hairStyle + 1) % 3
                            }
                            showNotification("${b.name} انجام شد!")
                        } else {
                            showNotification(PersianTexts.NOT_ENOUGH_MONEY)
                        }
                        return true
                    }
                    yy += 65f
                }
                // انتخاب رنگ مو
                yy = 180f + GameData.BARBER_SERVICES.size * 65f + 35f
                if (y > yy - 25f && y < yy + 5f) {
                    var cx = 200f
                    for (c in GameData.HAIR_COLORS) {
                        if (x > cx && x < cx + 40f) {
                            player.hairColor = c
                            showNotification("رنگ مو تغییر کرد!")
                            return true
                        }
                        cx += 50f
                    }
                }
                yy += 70f
                // دکمه تغییر مدل مو
                if (x > 280f && x < 380f && y > yy + 10f && y < yy + 40f) {
                    player.hairStyle = (player.hairStyle + 1) % 3
                    showNotification("مدل مو تغییر کرد!")
                    return true
                }
            }
            UIScreen.SHOP_VEHICLE -> {
                var yy = 180f
                for (v in GameData.VEHICLES) {
                    if (x > 100f && x < viewWidth - 100f && y > yy && y < yy + 55f) {
                        if (!player.ownedVehicles.contains(v.id)) {
                            val (ok, msg) = economy.buyVehicle(player, v.id)
                            showNotification(msg)
                        } else {
                            showNotification("شما این ماشین را دارید!")
                        }
                        return true
                    }
                    yy += 65f
                }
            }
            UIScreen.GARAGE -> {
                // انتخاب ماشین (کلیک روی مربع رنگ ماشین)
                var cx = 100f
                val cy = 180f
                for (vid in player.ownedVehicles) {
                    val def = GameData.VEHICLES.find { it.id == vid } ?: continue
                    if (x > cx && x < cx + 90f && y > cy && y < cy + 70f) {
                        // این ماشین را فعال کن
                        showNotification("ماشین ${def.name} انتخاب شد")
                        return true
                    }
                    cx += 110f
                }
                // رنگ ماشین
                if (y > 300f && y < 340f) {
                    cx = 100f
                    var colorIdx = 0
                    for (c in GameData.VEHICLE_COLORS) {
                        if (x > cx && x < cx + 50f) {
                            // ذخیره رنگ برای اولین ماشین بازیکن
                            val firstVehicle = player.ownedVehicles.firstOrNull()
                            if (firstVehicle != null) {
                                player.vehicleColors[firstVehicle] = c
                                // آپدیت ماشین پارک شده اگر هست
                                val veh = world.parkedVehicles.find { it.def.id == firstVehicle } ?: activeVehicle
                                veh?.color = c
                                showNotification("رنگ ماشین تغییر کرد!")
                            }
                            return true
                        }
                        cx += 60f
                        colorIdx++
                    }
                }
                // ارتقاها
                var yy = 420f
                for (u in GameData.VEHICLE_UPGRADES) {
                    if (x > 100f && x < viewWidth - 100f && y > yy && y < yy + 55f) {
                        val firstVehicle = player.ownedVehicles.firstOrNull()
                        if (firstVehicle != null) {
                            val (ok, msg) = economy.buyUpgrade(player, firstVehicle, u.id)
                            // اعمال روی ماشین پارک شده
                            if (ok) {
                                val veh = world.parkedVehicles.find { it.def.id == firstVehicle } ?: activeVehicle
                                veh?.upgrades?.add(u.id)
                            }
                            showNotification(msg)
                        }
                        return true
                    }
                    yy += 65f
                }
            }
            UIScreen.BANK -> {
                var yy = 320f
                // Deposit
                if (x > 100f && x < viewWidth - 100f && y > yy && y < yy + 50f) {
                    val amount = 1000L
                    val (ok, msg) = economy.deposit(player, amount)
                    showNotification(msg)
                    return true
                }
                yy += 60f
                // Withdraw
                if (x > 100f && x < viewWidth - 100f && y > yy && y < yy + 50f) {
                    val amount = 1000L
                    val (ok, msg) = economy.withdraw(player, amount)
                    showNotification(msg)
                    return true
                }
                yy += 60f
                // Loan
                if (x > 100f && x < viewWidth - 100f && y > yy && y < yy + 50f) {
                    val amount = 50000L
                    val (ok, msg) = economy.takeLoan(player, amount)
                    showNotification(msg)
                    return true
                }
                yy += 60f
                // Repay
                if (x > 100f && x < viewWidth - 100f && y > yy && y < yy + 50f) {
                    val amount = 10000L
                    val (ok, msg) = economy.repayLoan(player, amount)
                    showNotification(msg)
                    return true
                }
                yy += 60f
                // Rent
                if (x > 100f && x < viewWidth - 100f && y > yy && y < yy + 50f) {
                    val rent = economy.collectPropertyRent(player)
                    showNotification(if (rent > 0) "اجاره به مبلغ $rent تومان دریافت شد!" else "ملکی برای دریافت اجاره ندارید!")
                    return true
                }
            }
            UIScreen.JOB_CENTER -> {
                var yy = 220f
                for (job in jobs.allJobs) {
                    if (x > 100f && x < viewWidth - 100f && y > yy && y < yy + 60f) {
                        val msg = jobs.startJob(player, job.id)
                        showNotification(msg)
                        return true
                    }
                    yy += 70f
                }
            }
            UIScreen.PROPERTY -> {
                var yy = 180f
                for (p in GameData.PROPERTIES) {
                    if (x > 100f && x < viewWidth - 100f && y > yy && y < yy + 55f) {
                        if (!player.ownedProperties.contains(p.id)) {
                            val (ok, msg) = economy.buyProperty(player, p.id)
                            showNotification(msg)
                        } else {
                            showNotification("شما این ملک را دارید!")
                        }
                        return true
                    }
                    yy += 65f
                }
            }
            else -> { /* nothing */ }
        }
        return false
    }

    /** انجام اکشن متناسب با موقعیت بازیکن */
    private fun performAction() {
        val building = world.getNearbyBuilding(player.x, player.y, 90f)
        if (building != null) {
            handleBuildingEntry(building)
            return
        }
        val npc = world.getNearbyNPC(player.x, player.y, 80f)
        if (npc != null) {
            dialogSpeaker = npc.name
            dialogText = npc.getGreeting()
            currentScreen = UIScreen.DIALOG
            return
        }
        val vehicle = findNearestEnterableVehicle()
        if (vehicle != null) {
            enterVehicle(vehicle)
            return
        }
    }

    /** ورود به ساختمان */
    private fun handleBuildingEntry(building: Building) {
        when (building.type) {
            BuildingType.HOME -> {
                // استراحت
                player.sleep()
                // پرش به روز بعد
                dayNight.hour = 8f
                dayNight.day++
                economy.applyDailyInterest(player)
                val rent = economy.collectPropertyRent(player)
                showNotification("خوابیدی! انرژی‌ت پر شد. درآمد اجاره: $rent ت")
            }
            BuildingType.RESTAURANT -> currentScreen = UIScreen.SHOP_FOOD
            BuildingType.CLOTHING_SHOP -> currentScreen = UIScreen.SHOP_CLOTHES
            BuildingType.BARBERSHOP -> currentScreen = UIScreen.SHOP_BARBER
            BuildingType.CAR_DEALER -> currentScreen = UIScreen.SHOP_VEHICLE
            BuildingType.GARAGE -> currentScreen = UIScreen.GARAGE
            BuildingType.BANK -> currentScreen = UIScreen.BANK
            BuildingType.JOB_CENTER -> currentScreen = UIScreen.JOB_CENTER
            BuildingType.HOSPITAL -> {
                player.health = 100
                showNotification("درمان شدی! سلامتی‌ت کامل شد.")
            }
            BuildingType.PARK -> {
                player.energy = (player.energy + 20).coerceAtMost(100)
                showNotification("در پارک استراحتی کردی!")
            }
            BuildingType.GYM -> {
                if (player.money >= 200) {
                    player.spend(200)
                    player.health = (player.health + 15).coerceAtMost(100)
                    player.energy = (player.energy + 10).coerceAtMost(100)
                    showNotification("در باشگاه تمرین کردی!")
                } else {
                    showNotification(PersianTexts.NOT_ENOUGH_MONEY + " (۲۰۰ تومان لازم است)")
                }
            }
            BuildingType.SUPERMARKET -> {
                player.hunger = (player.hunger - 5).coerceAtLeast(0)
                showNotification("از سوپرمارکت دیدن کردی!")
            }
            BuildingType.CINEMA -> {
                if (player.money >= 500) {
                    player.spend(500)
                    player.energy = (player.energy + 30).coerceAtMost(100)
                    showNotification("فیلم دیدی! انرژی‌ت جبران شد.")
                } else {
                    showNotification(PersianTexts.NOT_ENOUGH_MONEY)
                }
            }
            BuildingType.POLICE_STATION -> {
                showNotification("به کلانتری خوش آمدید!")
            }
            BuildingType.SCHOOL -> {
                player.energy = (player.energy - 10).coerceAtLeast(0)
                showNotification("مدرسه را دیدی!")
            }
            BuildingType.LIBRARY -> {
                player.energy = (player.energy + 10).coerceAtMost(100)
                showNotification("در کتابخانه مطالعه کردی!")
            }
            BuildingType.GAS_STATION -> {
                showNotification("پمپ بنزین - ماشین‌ها نیازی به سوخت ندارند!")
            }
            BuildingType.HELIPAD -> {
                if (player.ownedVehicles.contains("heli")) {
                    val heliDef = GameData.VEHICLES.find { it.id == "heli" }
                    if (heliDef != null) {
                        val v = Vehicle(heliDef, player.x, player.y + 50f, player.vehicleColors["heli"] ?: heliDef.color)
                        v.ownerId = "player"
                        activeVehicle = v
                        player.isInVehicle = true
                        player.currentVehicleId = "heli"
                        showNotification("به هلیکوپتر سوار شدی!")
                    }
                } else {
                    showNotification("هلیکوپتر نداری! اول بخرش.")
                }
            }
            BuildingType.BIKE_SHOP -> currentScreen = UIScreen.SHOP_VEHICLE
        }
    }

    /** سوار شدن به ماشین */
    private fun enterVehicle(vehicle: Vehicle) {
        vehicle.isPlayerDriving = true
        activeVehicle = vehicle
        player.isInVehicle = true
        player.currentVehicleId = vehicle.def.id
        // حذف از پارک شده‌ها اگر اونجا بود
        world.parkedVehicles.remove(vehicle)
        // اعمال آپگریدها و رنگ ذخیره‌شده روی نمونه فعلی
        val savedUpgrades = player.vehicleUpgrades[vehicle.def.id]
        if (savedUpgrades != null) {
            vehicle.upgrades.clear()
            vehicle.upgrades.addAll(savedUpgrades)
        }
        player.vehicleColors[vehicle.def.id]?.let { vehicle.color = it }
        showNotification("سوار شدی! جهت با جوی‌استیک.")
    }

    /** پیاده شدن از ماشین */
    private fun exitVehicle() {
        val v = activeVehicle ?: return
        v.isPlayerDriving = false
        player.isInVehicle = false
        player.currentVehicleId = null
        player.x = v.x + 40f
        player.y = v.y + 20f
        // اضافه کردن به ماشین‌های پارک شده
        if (player.ownedVehicles.contains(v.def.id)) {
            world.parkedVehicles.add(v)
        }
        activeVehicle = null
        showNotification("پیاده شدی!")
    }

    /** شروع بازی از صفر (ریست) */
    fun newGame() {
        // ریست بازیکن
        val p = player
        p.x = 200f
        p.y = 1200f
        p.gender = Player.Gender.MALE
        p.hairColor = GameData.HAIR_COLORS[0]
        p.shirtColor = GameData.SHIRT_COLORS[0]
        p.pantsColor = GameData.PANTS_COLORS[0]
        p.skinColor = GameData.SKIN_COLORS[0]
        p.hairStyle = 0
        p.money = 5000
        p.bankBalance = 0
        p.loan = 0
        p.hunger = 100
        p.energy = 100
        p.health = 100
        p.jobId = null
        p.ownedVehicles.clear()
        p.ownedVehicles.add("bicycle")
        p.ownedProperties.clear()
        p.ownedClothing.clear()
        p.vehicleUpgrades.clear()
        p.vehicleColors.clear()
        p.isInVehicle = false
        p.currentVehicleId = null

        dayNight.hour = 8f
        dayNight.day = 1
        activeVehicle = null
        cameraX = 0f
        cameraY = 0f

        currentScreen = UIScreen.CHARACTER_CREATOR
    }
}
