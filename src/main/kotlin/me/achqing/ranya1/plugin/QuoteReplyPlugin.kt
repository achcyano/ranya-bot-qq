package me.achqing.ranya1.plugin

import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.core.BotPlugin.MESSAGE_BLOCK
import com.mikuac.shiro.core.BotPlugin.MESSAGE_IGNORE
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
            message.contains("[CQ:") &&
                    (!message.contains("[CQ:reply"))
                    && (!message.contains("[CQ:at")) -> return MESSAGE_IGNORE
            message.startsWithAny("[", "/") && message.contains("/") && (!message.contains("[CQ:face"))
                -> return bot?.quoteReply(event)!!
            else -> return MESSAGE_IGNORE
        }

        return MESSAGE_BLOCK
    }
}


data class ParsedMessage(
    val replyMessageId: Int? = null,
    val atUserId: Long? = null,
    val command:  String = "",
    val description: String = "",
    val rawText: String = ""
)


fun Bot.quoteReply(event: GroupMessageEvent): Int {
    println(event.message)

    val message = event.message ?: return MESSAGE_BLOCK
    val groupId = event.groupId ?: return MESSAGE_BLOCK
    val senderId = event.userId

    val parsed = parseMessage(message)
    if (parsed.command.isEmpty()) {
        return MESSAGE_IGNORE
    }

    val targetId = when {
        parsed.replyMessageId != null -> {
            this.getMsg(parsed.replyMessageId)?.data?.sender?.userId?.toLongOrNull()
        }
        parsed.atUserId != null -> parsed.atUserId
        else -> senderId
    } ?: senderId

    val isTargetSelf = targetId == senderId

    val replyMsg = buildReplyMessage(
        senderId,
        targetId,
        parsed.command,
        parsed.description,
        isTargetSelf
    )

    val finalMsg = if (event.messageId!= null) {
        "[CQ:reply,id=${event.messageId}]$replyMsg"
    } else {
        replyMsg
    }

    this.sendGroupMsg(groupId, finalMsg, false)
    return MESSAGE_IGNORE
}

fun parseMessage(message: String): ParsedMessage {
    var replyMessageId: Int? = null
    var atUserId: Long? = null
    var rawText = message

    val cqReplyRegex = """\[CQ:reply,id=(-?\d+)]""".toRegex()
    val cqAtRegex = """\[CQ:at,qq=(\d+)]""".toRegex()

    cqReplyRegex. find(message)?.let {
        replyMessageId = it.groupValues[1].toIntOrNull()
        rawText = rawText.replace(it.value, "").trim()
    }

    cqAtRegex.findAll(message).forEach {
        val qq = it.groupValues[1].toLongOrNull()
        if (qq != null && atUserId == null) {
            atUserId = qq
        }
        rawText = rawText. replace(it.value, "").trim()
    }

    if (!rawText. startsWith("/") && !rawText.startsWith("[")) {
        return ParsedMessage(
            replyMessageId = replyMessageId,
            atUserId = atUserId,
            rawText = rawText
        )
    }

    val commandText = rawText.substring(1).trim()

    val parts = commandText.split(Regex("\\s+"), limit = 2)
    val command = parts[0]
    val description = if (parts.size > 1) parts[1] else ""

    return ParsedMessage(
        replyMessageId = replyMessageId,
        atUserId = atUserId,
        command = command,
        description = description,
        rawText = rawText
    )
}

fun buildReplyMessage(
    senderId: Long,
    targetId: Long,
    command: String,
    description: String,
    isTargetSelf: Boolean
): String {
    val senderAt = "[CQ:at,qq=$senderId]"
    val targetAt = if (isTargetSelf) "自己" else "[CQ:at,qq=$targetId]"

    return if (description.isEmpty()) {
        "$senderAt ${command}了 $targetAt！"
    } else {
        "$senderAt $command $targetAt $description！"
    }
}
