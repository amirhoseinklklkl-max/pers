package com.persiancity.game

import android.os.Bundle
import android.view.MotionEvent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.persiancity.game.game.GameView

/**
 * اکتیویتی اصلی بازی
 * Main activity hosting GameView, handles lifecycle and touch dispatch
 */
class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // تمام‌صفحه - حالت افقی از Manifest اعمال می‌شود
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // مخفی کردن نوار وضعیت و ناوبری برای تجربه تمام‌صفحه
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )

        gameView = GameView(this)
        gameView.onExitToMenu = {
            finishAffinity()
        }
        setContentView(gameView)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gameView.handleTouch(event)
        return super.onTouchEvent(event)
    }

    override fun onPause() {
        super.onPause()
        // ذخیره خودکار هنگام خروج
        if (gameView.currentScreen != GameView.UIScreen.MENU) {
            try {
                gameView.saveSystem.save(gameView.player, gameView.dayNight)
            } catch (e: Exception) { /* ignore */ }
        }
        gameView.stopGameLoop()
    }

    override fun onResume() {
        super.onResume()
        // رزومه از حالت منو، چون گیم‌لوپ خودش از surfaceCreated شروع می‌شود
    }

    override fun onBackPressed() {
        // باز کردن منوی توقف به جای خروج
        when (gameView.currentScreen) {
            GameView.UIScreen.NONE -> gameView.currentScreen = GameView.UIScreen.PAUSE
            GameView.UIScreen.PAUSE -> {
                gameView.saveSystem.save(gameView.player, gameView.dayNight)
                gameView.currentScreen = GameView.UIScreen.MENU
            }
            GameView.UIScreen.MENU -> finishAffinity()
            else -> gameView.currentScreen = GameView.UIScreen.NONE
        }
    }
}
