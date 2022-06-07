package gy.zr.twitchdropcampaignsrest

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
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

            val tenMinsTz = LocalDateTime.ofInstant(cal.time.toInstant(), ZoneId.of("UTC"))
            val nowDefaultTz = LocalDateTime.now().atZone(ZoneId.systemDefault())

            val zonedTenMins = tenMinsTz.atZone(ZoneId.of("UTC"))
            val zonedNow = nowDefaultTz.withZoneSameInstant(ZoneId.of("UTC"))
            val zonedStarted = LocalDateTime.ofInstant(it.started.toInstant(), ZoneId.of("UTC")).atZone(ZoneId.of("UTC"))
            val format2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")

//            println("nowDefaultTz: $nowDefaultTz, tenMins: $tenMinsTz, defaultZone: ${ZoneId.systemDefault().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)}")
//            println("zoned now: $zonedNow, zonedTenMins: $zonedTenMins (zonedStarted: $zonedStarted)")

            if (zonedNow.isAfter(zonedTenMins) && zonedNow.isBefore(zonedStarted)) { // Drop starts within next 10 mins so notify.
//                println("[DEBUG] $zonedNow is after $zonedTenMins")
                println("${it.name} at ${it.started}")
                bot.sendMessage("${it.name} (${it.game.name}) starts at ${format2.format(zonedStarted.withZoneSameInstant(ZoneId.of("Europe/London")))} " +
                        "(${it.started} UTC)")
            }
        }
    }
}
