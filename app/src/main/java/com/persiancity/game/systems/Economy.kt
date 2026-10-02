package com.persiancity.game.systems

import com.persiancity.game.data.GameData
import com.persiancity.game.data.PersianTexts
import com.persiancity.game.entities.Player

/**
 * سیستم اقتصاد - پول، بانک، وام، ملک
 * Economy: money, bank deposits, loans, property rent collection
 */
class Economy {

    /** نرخ سود سالانه وام */
    val loanInterestRate = 0.15f
    /** نرخ سود سالانه سپرده */
    val depositInterestRate = 0.08f
    /** حداکثر وام = ۳ برابر موجودی فعلی بانک + ۱۰۰ هزار */
    fun maxLoan(player: Player): Long = 100_000L + (player.bankBalance * 3)

    /** سپرده‌گذاری پول در بانک */
    fun deposit(player: Player, amount: Long): Pair<Boolean, String> {
        if (amount <= 0) return Pair(false, "مبلغ نامعتبر!")
        if (!player.canAfford(amount)) {
            return Pair(false, PersianTexts.Bank.NOT_ENOUGH_BALANCE)
        }
        player.spend(amount)
        player.bankBalance += amount
        return Pair(true, PersianTexts.Bank.DEPOSIT_SUCCESS)
    }

    /** برداشت از بانک */
    fun withdraw(player: Player, amount: Long): Pair<Boolean, String> {
        if (amount <= 0) return Pair(false, "مبلغ نامعتبر!")
        if (player.bankBalance < amount) {
            return Pair(false, PersianTexts.Bank.NOT_ENOUGH_BALANCE)
        }
        player.bankBalance -= amount
        player.addMoney(amount)
        return Pair(true, PersianTexts.Bank.WITHDRAW_SUCCESS)
    }

    /** گرفتن وام */
    fun takeLoan(player: Player, amount: Long): Pair<Boolean, String> {
        if (amount <= 0) return Pair(false, "مبلغ نامعتبر!")
        if (player.loan > 0) {
            return Pair(false, PersianTexts.Bank.ALREADY_HAS_LOAN)
        }
        if (amount > maxLoan(player)) {
            return Pair(false, PersianTexts.Bank.LOAN_TOO_BIG + " (حداکثر: ${maxLoan(player)} تومان)")
        }
        player.loan = amount
        player.addMoney(amount)
        return Pair(true, "وام به مبلغ $amount تومان به شما داده شد!")
    }

    /** بازپرداخت وام */
    fun repayLoan(player: Player, amount: Long): Pair<Boolean, String> {
        if (player.loan <= 0) return Pair(false, "شما وامی ندارید!")
        if (amount > player.money) return Pair(false, PersianTexts.Bank.NOT_ENOUGH_BALANCE)
        val repayAmount = amount.coerceAtMost(player.loan)
        player.spend(repayAmount)
        player.loan -= repayAmount
        return Pair(true, "مبلغ $repayAmount تومان از وام شما کسر شد.")
    }

    /** سود روزانه سپرده + بهره روزانه وام (در شروع هر روز) */
    fun applyDailyInterest(player: Player) {
        // سود روزانه سپرده
        if (player.bankBalance > 0) {
            val interest = (player.bankBalance * depositInterestRate / 365f).toLong()
            player.bankBalance += interest
        }
        // بهره روزانه وام
        if (player.loan > 0) {
            val interest = (player.loan * loanInterestRate / 365f).toLong()
            player.loan += interest
        }
    }

    /** درآمد روزانه املاک */
    fun collectPropertyRent(player: Player): Long {
        var total = 0L
        for (propId in player.ownedProperties) {
            val prop = GameData.PROPERTIES.find { it.id == propId } ?: continue
            total += prop.rentPerDay
        }
        if (total > 0) {
            player.addMoney(total)
        }
        return total
    }

    /** خرید ملک */
    fun buyProperty(player: Player, propId: String): Pair<Boolean, String> {
        val prop = GameData.PROPERTIES.find { it.id == propId }
            ?: return Pair(false, "ملک یافت نشد!")
        if (player.ownedProperties.contains(propId)) {
            return Pair(false, "شما این ملک را دارید!")
        }
        if (!player.canAfford(prop.price.toLong())) {
            return Pair(false, PersianTexts.NOT_ENOUGH_MONEY)
        }
        player.spend(prop.price.toLong())
        player.ownedProperties.add(propId)
        return Pair(true, "${prop.name} با موفقیت خریداری شد!")
    }

    /** خرید وسیله نقلیه */
    fun buyVehicle(player: Player, vehicleId: String): Pair<Boolean, String> {
        val def = GameData.VEHICLES.find { it.id == vehicleId }
            ?: return Pair(false, "وسیله نقلیه یافت نشد!")
        if (player.ownedVehicles.contains(vehicleId)) {
            return Pair(false, "شما این وسیله را دارید!")
        }
        if (!player.canAfford(def.price.toLong())) {
            return Pair(false, PersianTexts.NOT_ENOUGH_MONEY + " (${def.price} تومان لازم است)")
        }
        player.spend(def.price.toLong())
        player.ownedVehicles.add(vehicleId)
        return Pair(true, "${def.name} خریداری شد!")
    }

    /** خرید لباس */
    fun buyClothing(player: Player, clothingId: String): Pair<Boolean, String> {
        val def = GameData.CLOTHING.find { it.id == clothingId }
            ?: return Pair(false, "آیتم یافت نشد!")
        if (player.ownedClothing.contains(clothingId)) {
            return Pair(false, "شما این لباس را دارید!")
        }
        if (!player.canAfford(def.price.toLong())) {
            return Pair(false, PersianTexts.NOT_ENOUGH_MONEY)
        }
        player.spend(def.price.toLong())
        player.ownedClothing.add(clothingId)
        return Pair(true, "${def.name} خریداری شد!")
    }

    /** خرید آپگرید ماشین */
    fun buyUpgrade(player: Player, vehicleId: String, upgradeId: String): Pair<Boolean, String> {
        val def = GameData.VEHICLE_UPGRADES.find { it.id == upgradeId }
            ?: return Pair(false, "ارتقا یافت نشد!")
        if (!player.ownedVehicles.contains(vehicleId)) {
            return Pair(false, "شما این ماشین را ندارید!")
        }
        val list = player.vehicleUpgrades.getOrPut(vehicleId) { mutableListOf() }
        if (list.contains(upgradeId)) {
            return Pair(false, "این ارتقا نصب شده!")
        }
        if (!player.canAfford(def.price.toLong())) {
            return Pair(false, PersianTexts.NOT_ENOUGH_MONEY)
        }
        player.spend(def.price.toLong())
        list.add(upgradeId)
        return Pair(true, "${def.name} نصب شد!")
    }

    /** تنظیم رنگ ماشین (رایگان) */
    fun setVehicleColor(player: Player, vehicleId: String, color: Int) {
        player.vehicleColors[vehicleId] = color
    }
}
