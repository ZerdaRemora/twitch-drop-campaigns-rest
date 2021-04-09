package gy.zr.twitchdropcampaignsrest

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

class NotificationBot : TelegramLongPollingBot() {

    private var chatId: String? = null

    override fun getBotToken() = System.getenv("BOT_TOKEN") ?: ""

    override fun getBotUsername() = "Twitch Drop Bot"

    override fun onUpdateReceived(update: Update?) {
        println("Received: ${update?.message?.text} from ${update?.message?.chat?.userName}")
        if (update != null && update.message != null) {
            val msg = update.message

            // When the provided username sends a message, make a note of the chatId so that we can send them messages.
            if (msg.chat?.userName == System.getenv("CHAT_USERNAME")) {
                chatId = msg.chatId.toString()
            }
        }
    }

    fun sendMessage(messageText: String) {
        if (getChatId() != null) {
            val message = SendMessage()
            message.chatId = getChatId().toString()
            message.text = messageText

            execute(message)
        }
    }

    private fun getChatId(): String? {
        if (chatId == null)
            chatId = System.getenv("CHAT_ID")

        return chatId
    }
}