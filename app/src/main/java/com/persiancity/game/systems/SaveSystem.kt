package com.persiancity.game.systems

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.persiancity.game.data.GameData
import com.persiancity.game.entities.Player
import com.persiancity.game.world.DayNightCycle

/**
 * سیستم ذخیره و بارگذاری بازی
 * Save/load using SharedPreferences + Gson serialization
 */
class SaveSystem(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "persian_city_save"
        private const val KEY_PLAYER = "player_json"
        private const val KEY_TIME = "time_hour"
        private const val KEY_DAY = "day_number"
        private const val KEY_EXISTS = "save_exists"
    }

    /** ذخیره بازی */
    fun save(player: Player, dayNight: DayNightCycle): Boolean {
        return try {
            val playerJson = gson.toJson(PlayerSnapshot.fromPlayer(player))
            prefs.edit()
                .putString(KEY_PLAYER, playerJson)
                .putFloat(KEY_TIME, dayNight.hour)
                .putInt(KEY_DAY, dayNight.day)
                .putBoolean(KEY_EXISTS, true)
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    /** بارگذاری بازی */
    fun load(player: Player, dayNight: DayNightCycle): Boolean {
        if (!hasSave()) return false
        return try {
            val json = prefs.getString(KEY_PLAYER, null) ?: return false
            val snap: PlayerSnapshot = gson.fromJson(json, PlayerSnapshot::class.java) ?: return false
            snap.applyTo(player)
            dayNight.hour = prefs.getFloat(KEY_TIME, 8f)
            dayNight.day = prefs.getInt(KEY_DAY, 1)
            true
        } catch (e: Exception) {
            false
        }
    }

    /** آیا ذخیره‌ای وجود دارد؟ */
    fun hasSave(): Boolean = prefs.getBoolean(KEY_EXISTS, false)

    /** حذف ذخیره */
    fun clearSave() {
        prefs.edit().clear().apply()
    }

    /** اسنپ‌شات از بازیکن برای ذخیره JSON */
    data class PlayerSnapshot(
        var x: Float = 200f,
        var y: Float = 1200f,
        var gender: String = "MALE",
        var hairColor: Int = 0,
        var shirtColor: Int = 0,
        var pantsColor: Int = 0,
        var skinColor: Int = 0,
        var hairStyle: Int = 0,
        var money: Long = 5000,
        var bankBalance: Long = 0,
        var loan: Long = 0,
        var hunger: Int = 100,
        var energy: Int = 100,
        var health: Int = 100,
        var jobId: String? = null,
        var ownedVehicles: MutableList<String> = mutableListOf("bicycle"),
        var ownedProperties: MutableList<String> = mutableListOf(),
        var ownedClothing: MutableList<String> = mutableListOf(),
        var vehicleUpgrades: Map<String, List<String>> = emptyMap(),
        var vehicleColors: Map<String, Int> = emptyMap()
    ) {
        companion object {
            fun fromPlayer(p: Player): PlayerSnapshot {
                return PlayerSnapshot(
                    x = p.x, y = p.y,
                    gender = p.gender.name,
                    hairColor = p.hairColor, shirtColor = p.shirtColor,
                    pantsColor = p.pantsColor, skinColor = p.skinColor,
                    hairStyle = p.hairStyle,
                    money = p.money, bankBalance = p.bankBalance, loan = p.loan,
                    hunger = p.hunger, energy = p.energy, health = p.health,
                    jobId = p.jobId,
                    ownedVehicles = p.ownedVehicles,
                    ownedProperties = p.ownedProperties,
                    ownedClothing = p.ownedClothing,
                    vehicleUpgrades = p.vehicleUpgrades.mapValues { it.value.toList() },
                    vehicleColors = p.vehicleColors.toMap()
                )
            }
        }

        fun applyTo(p: Player) {
            p.x = x; p.y = y
            p.gender = Player.Gender.valueOf(gender)
            p.hairColor = hairColor; p.shirtColor = shirtColor
            p.pantsColor = pantsColor; p.skinColor = skinColor
            p.hairStyle = hairStyle
            p.money = money; p.bankBalance = bankBalance; p.loan = loan
            p.hunger = hunger; p.energy = energy; p.health = health
            p.jobId = jobId
            p.ownedVehicles.clear()
            p.ownedVehicles.addAll(ownedVehicles)
            p.ownedProperties.clear()
            p.ownedProperties.addAll(ownedProperties)
            p.ownedClothing.clear()
            p.ownedClothing.addAll(ownedClothing)
            p.vehicleUpgrades.clear()
            vehicleUpgrades.forEach { (k, v) ->
                p.vehicleUpgrades[k] = v.toMutableList()
            }
            p.vehicleColors.clear()
            p.vehicleColors.putAll(vehicleColors)
        }
    }
}
