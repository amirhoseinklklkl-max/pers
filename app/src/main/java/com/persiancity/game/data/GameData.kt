package com.persiancity.game.data

import android.graphics.Color
import com.persiancity.game.data.PersianTexts.Vehicles

/**
 * تمام داده‌های ثابت بازی در این فایل قرار دارد.
 * Static game data: buildings, vehicles, items, properties.
 */
object GameData {

    // ==================== وسایل نقلیه ====================
    data class VehicleDef(
        val id: String,
        val name: String,
        val type: VehicleType,
        val price: Int,
        val speed: Float,           // سرعت نسبی
        val color: Int,             // رنگ پیش‌فرض
        val width: Float,
        val height: Float,
        val seats: Int = 1
    )

    enum class VehicleType { CAR, MOTORCYCLE, BICYCLE, HELICOPTER }

    val VEHICLES = listOf(
        VehicleDef("bicycle", PersianTexts.VEH_BICYCLE, VehicleType.BICYCLE, 500, 1.2f, Color.parseColor("#FF2196F3"), 60f, 35f),
        VehicleDef("motor", PersianTexts.VEH_MOTOR_BIKE, VehicleType.MOTORCYCLE, 8000, 2.5f, Color.parseColor("#FFD32F2F"), 70f, 40f),
        VehicleDef("sedan", PersianTexts.VEH_SEDAN, VehicleType.CAR, 25000, 2.0f, Color.parseColor("#FF1976D2"), 90f, 50f, 4),
        VehicleDef("suv", PersianTexts.VEH_SUV, VehicleType.CAR, 45000, 1.8f, Color.parseColor("#FF388E3C"), 95f, 55f, 4),
        VehicleDef("truck", PersianTexts.VEH_TRUCK, VehicleType.CAR, 35000, 1.5f, Color.parseColor("#FF795548"), 100f, 55f, 2),
        VehicleDef("sport", PersianTexts.VEH_SPORT, VehicleType.CAR, 120000, 3.5f, Color.parseColor("#FFFF5722"), 95f, 45f, 2),
        VehicleDef("heli", PersianTexts.VEH_HELI, VehicleType.HELICOPTER, 500000, 4.5f, Color.parseColor("#FF37474F"), 80f, 60f, 4)
    )

    // ==================== غذاها ====================
    data class FoodDef(val id: String, val name: String, val price: Int, val hungerRestore: Int, val healthBoost: Int)
    val FOODS = listOf(
        FoodDef("sandwich", PersianTexts.Items.SANDWICH, 500, 20, 5),
        FoodDef("kebab", PersianTexts.Items.KEBAB, 1500, 50, 15),
        FoodDef("pizza", PersianTexts.Items.PIZZA, 1200, 40, 10),
        FoodDef("juice", PersianTexts.Items.JUICE, 300, 10, 5),
        FoodDef("tea", PersianTexts.Items.TEA, 100, 5, 2),
        FoodDef("icecream", PersianTexts.Items.ICE_CREAM, 700, 15, 8)
    )

    // ==================== لباس‌ها ====================
    data class ClothingDef(val id: String, val name: String, val price: Int, val slot: ClothingSlot, val color: Int)
    enum class ClothingSlot { SHIRT, PANTS, HAT, SHOES }
    val CLOTHING = listOf(
        ClothingDef("shirt_blue", PersianTexts.Items.SHIRT_BLUE, 800, ClothingSlot.SHIRT, Color.parseColor("#FF1976D2")),
        ClothingDef("shirt_red", PersianTexts.Items.SHIRT_RED, 800, ClothingSlot.SHIRT, Color.parseColor("#FFD32F2F")),
        ClothingDef("shirt_green", PersianTexts.Items.SHIRT_GREEN, 800, ClothingSlot.SHIRT, Color.parseColor("#FF388E3C")),
        ClothingDef("pants_jeans", PersianTexts.Items.PANTS_JEANS, 1200, ClothingSlot.PANTS, Color.parseColor("#FF1565C0")),
        ClothingDef("pants_black", PersianTexts.Items.PANTS_BLACK, 1000, ClothingSlot.PANTS, Color.parseColor("#FF212121")),
        ClothingDef("hat_cap", PersianTexts.Items.HAT_CAP, 500, ClothingSlot.HAT, Color.parseColor("#FFEF6C00")),
        ClothingDef("shoes_sport", PersianTexts.Items.SHOES_SPORT, 1500, ClothingSlot.SHOES, Color.parseColor("#FFFFFFFF"))
    )

    // ==================== خدمات آرایشگاه ====================
    data class BarberService(val id: String, val name: String, val price: Int)
    val BARBER_SERVICES = listOf(
        BarberService("haircut", PersianTexts.Items.HAIRCUT, 500),
        BarberService("beard", PersianTexts.Items.BEARD_TRIM, 300),
        BarberService("color", PersianTexts.Items.HAIR_COLOR, 2000),
        BarberService("style", PersianTexts.Items.HAIRSTYLE, 1000)
    )

    // ==================== ارتقای ماشین ====================
    data class UpgradeDef(val id: String, val name: String, val price: Int, val desc: String)
    val VEHICLE_UPGRADES = listOf(
        UpgradeDef("turbo", Vehicles.TURBO, 5000, "افزایش ۲۰٪ سرعت"),
        UpgradeDef("nitro", Vehicles.NITRO, 8000, "نیروی اضافی موقت"),
        UpgradeDef("wheels", Vehicles.WHEELS, 3000, "کنترل بهتر"),
        UpgradeDef("spoiler", Vehicles.SPOILER, 4000, "ظاهر اسپرت"),
        UpgradeDef("engine", Vehicles.UPGRADE_HP, 10000, "افزایش ۳۰٪ قدرت")
    )

    val VEHICLE_COLORS = listOf(
        Color.parseColor("#FFD32F2F"), // قرمز
        Color.parseColor("#FF1976D2"), // آبی
        Color.parseColor("#FF388E3C"),  // سبز
        Color.parseColor("#FFFF5722"), // نارنجی
        Color.parseColor("#FFFFEB3B"), // زرد
        Color.parseColor("#FF212121"),  // مشکی
        Color.parseColor("#FFFFFFFF"),  // سفید
        Color.parseColor("#FF795548"),  // قهوه‌ای
        Color.parseColor("#FF9C27B0"),  // بنفش
        Color.parseColor("#FF00BCD4")   // فیروزه‌ای
    )

    // ==================== املاک ====================
    data class PropertyDef(
        val id: String,
        val name: String,
        val price: Int,
        val rentPerDay: Int,
        val x: Float,
        val y: Float
    )
    val PROPERTIES = listOf(
        PropertyDef("apt_1", PersianTexts.Property.APARTMENT + "۱", 50000, 200, 800f, 600f),
        PropertyDef("apt_2", PersianTexts.Property.APARTMENT + "۲", 75000, 350, 2200f, 800f),
        PropertyDef("house_1", PersianTexts.Property.HOUSE + "۱", 200000, 1000, 1200f, 1800f),
        PropertyDef("shop_1", PersianTexts.Property.SHOP + "۱", 350000, 1800, 2600f, 1800f),
        PropertyDef("villa_1", PersianTexts.Property.VILLA, 800000, 5000, 3200f, 600f)
    )

    // ==================== تنظیمات نقشه ====================
    const val WORLD_WIDTH = 4000f
    const val WORLD_HEIGHT = 2400f
    const val TILE_SIZE = 100f

    // ==================== رنگ‌ها ====================
    val HAIR_COLORS = listOf(
        Color.parseColor("#FF212121"), // مشکی
        Color.parseColor("#FF6D4C2F"), // قهوه‌ای
        Color.parseColor("#FFE0C16B"), // بلوند
        Color.parseColor("#FFB71C1C")  // قرمز
    )

    val SHIRT_COLORS = listOf(
        Color.parseColor("#FF1976D2"), // آبی
        Color.parseColor("#FFD32F2F"), // قرمز
        Color.parseColor("#FF388E3C"), // سبز
        Color.parseColor("#FFEF6C00"), // نارنجی
        Color.parseColor("#FF7B1FA2"), // بنفش
        Color.parseColor("#FFFFC107")  // زرد
    )

    val PANTS_COLORS = listOf(
        Color.parseColor("#FF1565C0"), // جین
        Color.parseColor("#FF212121"), // مشکی
        Color.parseColor("#FF616161"), // خاکستری
        Color.parseColor("#FF5D4037")  // قهوه‌ای
    )

    val SKIN_COLORS = listOf(
        Color.parseColor("#FFFFD3A0"), // روشن
        Color.parseColor("#FFEDBA8C"), // متوسط
        Color.parseColor("#FFC68642"), // تیره
        Color.parseColor("#FF8D5524")  // تیره‌تر
    )
}
