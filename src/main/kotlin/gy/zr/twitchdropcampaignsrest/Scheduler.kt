package gy.zr.twitchdropcampaignsrest

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.util.*
import javax.annotation.PostConstruct

@Component
class Scheduler(val repository: DropCampaignRepository) {

    val bot = NotificationBot()

    @PostConstruct
    fun init() {
        val botApi = TelegramBotsApi(DefaultBotSession::class.java)
        botApi.registerBot(bot)
    }

    @Scheduled(cron = "0 */10 * * * *")
    fun notifyUpcomingDrops() {
        repository.findAllByStatusIs("UPCOMING").forEach {
            val cal = Calendar.getInstance()
            cal.time = it.started
            cal.add(Calendar.MINUTE, -10)
            val tenMins = cal.time
            val now = Date()

            if (now.after(tenMins) && now.before(it.started)) { // Drop starts within next 10 mins so notify.
                println("${it.name} at ${it.started}")
                bot.sendMessage("${it.name} (${it.game.name}) starts at ${it.started}")
            }
        }
    }
}