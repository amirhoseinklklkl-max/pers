package com.persiancity.game.data

/**
 * تمام متن‌های فارسی بازی در این فایل متمرکز شده‌اند.
 * Central place for all Persian in-game text.
 */
object PersianTexts {

    // ===== منوی اصلی - Main menu =====
    const val APP_TITLE = "شهر فارسی"
    const val NEW_GAME = "بازی جدید"
    const val CONTINUE_GAME = "ادامه بازی"
    const val SETTINGS = "تنظیمات"
    const val EXIT = "خروج"
    const val ABOUT = "درباره بازی"

    // ===== پیام‌های عمومی - General =====
    const val OK = "باشه"
    const val CANCEL = "لغو"
    const val BUY = "خرید"
    const val SELL = "فروش"
    const val CLOSE = "بستن"
    const val YES = "بله"
    const val NO = "خیر"
    const val BACK = "بازگشت"
    const val SAVE = "ذخیره"
    const val EXIT_GAME = "خروج از بازی"

    // ===== HUD =====
    const val MONEY = "پول"
    const val DAY = "روز"
    const val TIME = "زمان"
    const val JOB = "شغل"
    const val HEALTH = "سلامتی"
    const val HUNGER = "گرسنگی"
    const val ENERGY = "انرژی"
    const val NO_JOB = "بدون شغل"

    // ===== زمان روز - Day phases =====
    const val MORNING = "صبح"
    const val NOON = "ظهر"
    const val EVENING = "عصر"
    const val NIGHT = "شب"

    // ===== کنترل‌ها - Controls =====
    const val ENTER_VEHICLE = "سوار شو"
    const val EXIT_VEHICLE = "پیاده شو"
    const val TALK = "صحبت کن"
    const val ENTER_BUILDING = "ورود"
    const val WORK = "کار کن"
    const val CUSTOMIZE = "تنظیم ماشین"
    const val BUY_VEHICLE = "خرید ماشین"
    const val INTERACT = "تعامل"

    // ===== نام ساختمان‌ها - Building names =====
    const val HOME = "خانه"
    const val CLOTHING_SHOP = "مغازه لباس"
    const val RESTAURANT = "رستوران"
    const val BARBERSHOP = "آرایشگاه"
    const val CAR_DEALER = "نمایشگاه ماشین"
    const val GARAGE = "گاراژ ماشین"
    const val BANK = "بانک"
    const val POLICE_STATION = "کلانتری"
    const val HOSPITAL = "بیمارستان"
    const val PARK = "پارک"
    const val GYM = "باشگاه ورزشی"
    const val SUPERMARKET = "سوپرمارکت"
    const val CINEMA = "سینما"
    const val GAS_STATION = "پمپ بنزین"
    const val JOB_CENTER = "مرکز کاریابی"
    const val SCHOOL = "مدرسه"
    const val LIBRARY = "کتابخانه"
    const val HELIPAD = "فرودگاه هلیکوپتر"
    const val BIKE_SHOP = "مغازه دوچرخه"

    // ===== نام شغل‌ها - Job names =====
    const val JOB_COURIER = "پیک"
    const val JOB_CHEF = "آشپز"
    const val JOB_CAR_DEALER = "فروشنده ماشین"
    const val JOB_CLOTHING_SELLER = "فروشنده لباس"
    const val JOB_BARBER = "آرایشگر"
    const val JOB_POLICE = "پلیس"
    const val JOB_UNEMPLOYED = "بی‌کار"

    // ===== نام وسایل نقلیه - Vehicle names =====
    const val VEH_SEDAN = "سدان خانوادگی"
    const val VEH_SPORT = "ماشین اسپرت"
    const val VEH_SUV = "ماشین شاسی‌بلند"
    const val VEH_TRUCK = "وانت"
    const val VEH_MOTOR_BIKE = "موتور سیکلت"
    const val VEH_BICYCLE = "دوچرخه"
    const val VEH_HELI = "هلیکوپتر"

    // ===== دیالوگ‌های NPC - NPC dialogs =====
    object Dialogs {
        const val GREETING_SHOPKEEPER = "سلام! به مغازه من خوش اومدی. چیکار می‌تونم برات بکنم؟"
        const val GREETING_BARBER = "سلام دوست عزیز! موهاتو می‌خوای قیافه‌اشو عوض کنی؟"
        const val GREETING_CHEF = "سلام! امروز چی میل داری؟ غذاهای خوشمزه‌ای دارم."
        const val GREETING_POLICE = "سلام شهروند عزیز! شهر امن ما به شما خوش آمد."
        const val GREETING_BANKER = "سلام! برای خدمات بانکی به من مراجعه کردی؟"
        const val GREETING_RANDOM_1 = "سلام! امروز چطوره؟ من دارم می‌رم سر کار."
        const val GREETING_RANDOM_2 = "هی! شهر ما خیلی قشنگه، نه؟"
        const val GREETING_RANDOM_3 = "وقت بخیر! تو تازه اومدی اینجا؟"
        const val GREETING_RANDOM_4 = "سلام! اگه کار پیدا کنی، پول خوبی می‌تونی دربیاری."
        const val GREETING_RANDOM_5 = "خیلی خوش اومدی! حواست به ماشین‌ها باشه وقتی از خیابون رد میشی."
        const val FAREWELL = "خداحافظ! موفق باشی."
        const val JOB_HINT = "اگه به دنبال کاری، برو به مرکز کاریابی."
    }

    // ===== پیام‌های سیستم - System messages =====
    const val NOT_ENOUGH_MONEY = "پول کافی نداری!"
    const val BOUGHT_ITEM = "خریداری شد!"
    const val SOLD_ITEM = "فروخته شد!"
    const val JOB_STARTED = "شغلت شروع شد!"
    const val JOB_FINISHED = "کار تموم شد! پولت رو گرفتی."
    const val ENTERING_VEHICLE = "سوار شدی!"
    const val EXITING_VEHICLE = "پیاده شدی!"
    const val GAME_SAVED = "بازی ذخیره شد!"
    const val GAME_LOADED = "بازی بارگذاری شد!"
    const val NEW_DAY = "روز جدید!"
    const val SLEEPING = "در حال استراحت..."
    const val ENERGY_RESTORED = "انرژی‌ت جبران شد!"

    // ===== آیتم‌ها - Items =====
    object Items {
        // غذاها
        const val KEBAB = "کباب"
        const val PIZZA = "پیتزا"
        const val SANDWICH = "ساندویچ"
        const val JUICE = "آبمیوه"
        const val TEA = "چای"
        const val ICE_CREAM = "بستنی"

        // لباس‌ها
        const val SHIRT_BLUE = "پیراهن آبی"
        const val SHIRT_RED = "پیراهن قرمز"
        const val SHIRT_GREEN = "پیراهن سبز"
        const val PANTS_JEANS = "شلوار جین"
        const val PANTS_BLACK = "شلوار مشکی"
        const val HAT_CAP = "کلاه"
        const val SHOES_SPORT = "کفش ورزشی"

        // خدمات آرایشگاه
        const val HAIRCUT = "اصلاح مو"
        const val BEARD_TRIM = "اصلاح ریش"
        const val HAIR_COLOR = "رنگ مو"
        const val HAIRSTYLE = "مدل مو"
    }

    // ===== بخش بانک - Bank =====
    object Bank {
        const val BALANCE = "موجودی حساب"
        const val LOAN = "وام"
        const val DEPOSIT = "سپرده"
        const val WITHDRAW = "برداشت"
        const val TAKE_LOAN = "گرفتن وام"
        const val REPAY_LOAN = "بازپرداخت وام"
        const val LOAN_LIMIT = "حداکثر وام قابل دریافت"
        const val INTEREST_RATE = "نرخ سود سالانه"
        const val LOAN_TOO_BIG = "این مبلغ وام بیش از حد مجاز است!"
        const val ALREADY_HAS_LOAN = "شما وام فعلی دارید، اول بازپرداخت کنید!"
        const val NOT_ENOUGH_BALANCE = "موجودی حساب کافی نیست!"
        const val DEPOSIT_SUCCESS = "سپرده با موفقیت ثبت شد!"
        const val WITHDRAW_SUCCESS = "برداشت با موفقیت انجام شد!"
    }

    // ===== بخش شغل‌ها - Jobs =====
    object Jobs {
        const val COURIER_DESC = "بسته‌ها رو به مقاصد مختلف برسان و پول بگیر"
        const val CHEF_DESC = "غذا درست کن و به مشتری‌ها بفروش"
        const val CAR_DEALER_DESC = "ماشین‌ها رو خرید و فروش کن با سود"
        const val CLOTHING_SELLER_DESC = "لباس‌ها رو با سود بفروش"
        const val BARBER_DESC = "به مشتری‌ها خدمات آرایشی بده"
        const val POLICE_DESC = "گشت بزن و شهر رو امن نگه دار"

        const val COURIER_REWARD = "۱۵۰۰ تومان برای هر بسته"
        const val CHEF_REWARD = "۸۰۰ تومان برای هر غذا"
        const val CAR_DEALER_REWARD = "سود متغیر"
        const val CLOTHING_SELLER_REWARD = "سود متغیر"
        const val BARBER_REWARD = "۵۰۰ تومان برای هر مشتری"
        const val POLICE_REWARD = "۲۰۰۰ تومان برای هر ساعت گشت"

        const val WANT_TO_WORK = "می‌خوای شروع کنی به کار؟"
        const val DELIVER_TO = "بسته رو تحویل بده به:"
        const val COOK_DISH = "غذا رو آماده کن"
        const val NO_JOB_AVAILABLE = "هیچ کاری الان موجود نیست!"
    }

    // ===== پیام‌های شخصیت‌سازی - Customization =====
    object Customize {
        const val CHOOSE_GENDER = "جنسیت رو انتخاب کن"
        const val MALE = "پسر"
        const val FEMALE = "دختر"
        const val CHOOSE_HAIR = "رنگ مو"
        const val CHOOSE_SHIRT = "رنگ لباس"
        const val CHOOSE_PANTS = "رنگ شلوار"
        const val HAIR_BLACK = "مشکی"
        const val HAIR_BROWN = "قهوه‌ای"
        const val HAIR_BLONDE = "بلوند"
        const val HAIR_RED = "قرمز"
        const val SKIN_LIGHT = "روشن"
        const val SKIN_MEDIUM = "متوسط"
        const val SKIN_DARK = "تیره"
    }

    // ===== بخش خودرو - Vehicle shop =====
    object Vehicles {
        const val BUY_TITLE = "خرید وسیله نقلیه"
        const val SELL_TITLE = "فروش وسیله نقلیه"
        const val YOUR_VEHICLES = "ماشین‌های شما"
        const val NO_VEHICLES = "شما هیچ وسیله نقلیه‌ای نداری"
        const val GARAGE_FULL = "گاراژ پر است!"
        const val CUSTOMIZE_TITLE = "تنظیمات ماشین"
        const val COLOR = "رنگ"
        const val TURBO = "توربو"
        const val NITRO = "نیترو"
        const val WHEELS = "چرخ‌ها"
        const val SPOILER = "اسپویلر"
        const val INSTALL = "نصب کن"
        const val UPGRADE_HP = "افزایش قدرت موتور"
        const val TOTAL_UPGRADES = "ارتقای کامل"
    }

    // ===== بخش ملک - Property =====
    object Property {
        const val BUY_PROPERTY = "خرید ملک"
        const val YOUR_PROPERTIES = "املاک شما"
        const val RENT_INCOME = "درآمد اجاره"
        const val APARTMENT = "آپارتمان"
        const val HOUSE = "خانه ویلایی"
        const val SHOP = "مغازه"
        const val VILLA = "ویلا"
        const val COLLECT_RENT = "دریافت اجاره"
        const val RENT_AVAILABLE = "اجاره قابل دریافت"
    }

    // ===== راهنما - Tutorial =====
    object Tutorial {
        const val WELCOME_TITLE = "خوش اومدی به شهر فارسی!"
        const val WELCOME_BODY = "در این شهر می‌تونی کار کنی، ماشین بخری، آشنا پیدا کنی و زندگی کنی! جوی‌استیک سمت چپ برای حرکت، دکمه‌های سمت راست برای تعامل."
        const val FIND_JOB = "برای پیدا کردن کار به مرکز کاریابی برو!"
        const val BUY_CAR = "وقتی پول کافی داری، به نمایشگاه ماشین برو و یه ماشین بخر!"
        const val SAVE_REGULARLY = "فراموش نکن بازی رو ذخیره کنی!"
    }

    // ===== تنظیمات - Settings =====
    object Settings {
        const val SOUND = "صدا"
        const val MUSIC = "موسیقی"
        const val ON = "روشن"
        const val OFF = "خاموش"
        const val LANGUAGE = "زبان"
        const val PERSIAN = "فارسی"
        const val RESET_GAME = "بازنشانی بازی"
        const val CONFIRM_RESET = "آیا مطمئنی می‌خوای بازی رو بازنشانی کنی؟ همه پیشرفت‌ها از بین می‌رن!"
    }
}
