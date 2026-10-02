package com.persiancity.game.systems

import com.persiancity.game.data.PersianTexts
import com.persiancity.game.entities.Player
import com.persiancity.game.entities.NPC
import com.persiancity.game.world.World

/**
 * سیستم شغل - شروع، پیشرفت و اتمام شغل
 * Job system: 6 jobs with rewards - courier, chef, car dealer, clothing seller, barber, police
 */
class JobSystem {

    enum class JobId { NONE, COURIER, CHEF, CAR_DEALER, CLOTHING_SELLER, BARBER, POLICE }

    /** اطلاعات شغل */
    data class JobInfo(
        val id: JobId,
        val name: String,
        val description: String,
        val reward: String
    )

    val allJobs = listOf(
        JobInfo(JobId.COURIER, PersianTexts.JOB_COURIER, PersianTexts.Jobs.COURIER_DESC, PersianTexts.Jobs.COURIER_REWARD),
        JobInfo(JobId.CHEF, PersianTexts.JOB_CHEF, PersianTexts.Jobs.CHEF_DESC, PersianTexts.Jobs.CHEF_REWARD),
        JobInfo(JobId.CAR_DEALER, PersianTexts.JOB_CAR_DEALER, PersianTexts.Jobs.CAR_DEALER_DESC, PersianTexts.Jobs.CAR_DEALER_REWARD),
        JobInfo(JobId.CLOTHING_SELLER, PersianTexts.JOB_CLOTHING_SELLER, PersianTexts.Jobs.CLOTHING_SELLER_DESC, PersianTexts.Jobs.CLOTHING_SELLER_REWARD),
        JobInfo(JobId.BARBER, PersianTexts.JOB_BARBER, PersianTexts.Jobs.BARBER_DESC, PersianTexts.Jobs.BARBER_REWARD),
        JobInfo(JobId.POLICE, PersianTexts.JOB_POLICE, PersianTexts.Jobs.POLICE_DESC, PersianTexts.Jobs.POLICE_REWARD)
    )

    /** گرفتن شغل فعلی بازیکن */
    fun currentJob(player: Player): JobInfo? {
        val id = player.jobId ?: return null
        return allJobs.find { it.id.name == id }
    }

    /** شروع شغل */
    fun startJob(player: Player, jobId: JobId): String {
        player.jobId = jobId.name
        val info = allJobs.find { it.id == jobId } ?: return PersianTexts.Jobs.NO_JOB_AVAILABLE
        return "${PersianTexts.JOB_STARTED}\nشغل شما: ${info.name}\n${info.description}\nپاداش: ${info.reward}"
    }

    /** پایان شغل (وقتی بازیکن می‌خواد عوض کنه) */
    fun quitJob(player: Player): String {
        player.jobId = null
        return "شما از شغل فعلی استعفا دادید."
    }

    /**
     * کار کردن - اجرای یک دوره کار
     * برمی‌گرداند: (موفق، پیام، پول دریافتی)
     */
    fun performWork(player: Player, world: World): Triple<Boolean, String, Long> {
        val job = currentJob(player) ?: return Triple(false, "شما شغلی ندارید! اول به مرکز کاریابی بروید.", 0L)

        if (player.energy < 15) {
            return Triple(false, "انرژی کافی نداری! برو بخواب یا غذا بخور.", 0L)
        }

        player.energy = (player.energy - 15).coerceAtLeast(0)
        player.hunger = (player.hunger - 8).coerceAtLeast(0)

        val reward = when (job.id) {
            JobId.COURIER -> {
                // پیدا کردن یک NPC تصادفی به عنوان مقصد
                val target = world.npcs.randomOrNull() ?: return Triple(false, "مقصدی برای تحویل پیدا نشد!", 0L)
                player.addMoney(1500)
                Triple(true, "${PersianTexts.Jobs.DELIVER_TO} ${target.name}!\n${PersianTexts.JOB_FINISHED}", 1500L)
            }
            JobId.CHEF -> {
                player.addMoney(800)
                Triple(true, "${PersianTexts.Jobs.COOK_DISH}!\n${PersianTexts.JOB_FINISHED}", 800L)
            }
            JobId.CAR_DEALER -> {
                val profit = (500..2500).random().toLong()
                player.addMoney(profit)
                Triple(true, "یک ماشین فروختی با سود $profit تومان!", profit)
            }
            JobId.CLOTHING_SELLER -> {
                val profit = (400..1500).random().toLong()
                player.addMoney(profit)
                Triple(true, "چند لباس فروختی با سود $profit تومان!", profit)
            }
            JobId.BARBER -> {
                player.addMoney(500)
                Triple(true, "به یک مشتری خدمات آرایشی دادی!\n${PersianTexts.JOB_FINISHED}", 500L)
            }
            JobId.POLICE -> {
                player.addMoney(2000)
                Triple(true, "یک ساعت گشت زدی!\n${PersianTexts.JOB_FINISHED}", 2000L)
            }
            JobId.NONE -> Triple(false, "شغلی ندارید.", 0L)
        }
        return reward
    }

    /** گرفتن شرح شغل به فارسی */
    fun jobDescription(player: Player): String {
        val job = currentJob(player) ?: return PersianTexts.JOB_UNEMPLOYED
        return "${job.name} - ${job.reward}"
    }
}
