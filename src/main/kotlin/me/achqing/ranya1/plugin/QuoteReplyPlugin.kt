package me.achqing.ranya1.plugin

import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import me.achqing.ranya1.utils.startsWithAny
import org.springframework.stereotype.Component

@Shiro
@Component
class QuoteReplyPlugin : BotPlugin() {
    override fun onGroupMessage(bot: Bot?, event: GroupMessageEvent?): Int {
        val message = event?.message ?: return MESSAGE_BLOCK
        val groupId = event.groupId ?: return MESSAGE_BLOCK

        when {
            message == "o.O" -> bot?.sendGroupMsg(groupId, "尊嘟", false)
            message == "O.o" -> bot?.sendGroupMsg(groupId, "假嘟", false)
            message.startsWithAny("[", "/") -> return bot?.quoteReply(event)!!
            else -> return MESSAGE_IGNORE
        }

        return MESSAGE_BLOCK
    }

    fun Bot.quoteReply(event: GroupMessageEvent) : Int{
        //sendGroupMsg(event.groupId, "引用回复: ${event.message}", true)

        return MESSAGE_BLOCK
    }

}
