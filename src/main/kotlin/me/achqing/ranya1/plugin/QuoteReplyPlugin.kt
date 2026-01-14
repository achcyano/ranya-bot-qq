package me.achqing.ranya1.plugin

import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import org.springframework.stereotype.Component

/**
 * Quote Reply Plugin - 引用回复插件
 * 
 * 实现类似于 Telegram_QuoteReply_Bot-Go 的功能
 * 支持的格式:
 * - /动词 [对象] - 对自己或回复的消息执行动作
 * - \动词 [对象] - 反转主客体（回复消息时使用）
 * - /动词@QQ号 [对象] - 指定目标用户
 */
@Shiro
@Component
class QuoteReplyPlugin {

    @GroupMessageHandler
    fun handleQuoteReply(bot: Bot, event: GroupMessageEvent) {
        val rawMessage = event.rawMessage ?: return
        
        // 检查消息是否以 / 或 \ 开头
        if (rawMessage.length < 2) return
        
        val isBackslash = rawMessage.startsWith("\\")
        val isSlash = rawMessage.startsWith("/")
        
        if (!isBackslash && !isSlash) return
        
        // 检查第二个字符，如果是ASCII字符但不是$开头，则不处理
        // 这样可以防止处理常规命令如 /help 等
        if (rawMessage.length >= 2) {
            val secondChar = rawMessage[1]
            if (isASCII(secondChar) && secondChar != '$') {
                return
            }
        }
        
        // 移除开头的 / 或 \ 和可能的 $
        var commandText = rawMessage.substring(1)
        if (commandText.startsWith("$")) {
            commandText = commandText.substring(1)
        }
        
        // 解析命令和参数
        val parts = commandText.split(" ", limit = 2)
        if (parts.isEmpty() || parts[0].isEmpty()) return
        
        var keyword = parts[0]
        val additionalText = if (parts.size > 1) parts[1] else ""
        
        // 获取发送者信息
        var senderId = event.userId
        var senderName = getUserName(event)
        
        // 获取目标用户信息
        var targetId = senderId
        var targetName = "自己"
        
        // 检查是否有回复消息
        val replyMsg = event.reply
        if (replyMsg != null) {
            // 有回复消息时，目标是被回复的用户
            targetId = replyMsg.userId
            targetName = getReplyUserName(event, replyMsg)
            
            // 如果使用反斜杠，交换主客体
            if (isBackslash) {
                val tempId = senderId
                val tempName = senderName
                senderId = targetId
                senderName = targetName
                targetId = tempId
                targetName = tempName
            }
        } else {
            // 没有回复消息，检查命令中是否有@用户名
            val keywordParts = keyword.split("@", limit = 2)
            if (keywordParts.size > 1) {
                keyword = keywordParts[0]
                val qqNumber = keywordParts[1]
                // 尝试解析QQ号
                targetId = qqNumber.toLongOrNull() ?: senderId
                if (targetId != senderId) {
                    targetName = qqNumber
                }
            }
        }
        
        // 构建回复消息
        val replyMessage = buildReplyMessage(
            senderName, senderId,
            targetName, targetId,
            keyword, additionalText
        )
        
        // 发送消息
        bot.sendGroupMsg(event.groupId, replyMessage, false)
    }
    
    /**
     * 获取用户名称（优先使用群名片，其次使用昵称）
     */
    private fun getUserName(event: GroupMessageEvent): String {
        return event.sender?.card?.takeIf { it.isNotEmpty() } 
            ?: event.sender?.nickname 
            ?: event.userId.toString()
    }
    
    /**
     * 获取回复消息的用户名称
     */
    private fun getReplyUserName(event: GroupMessageEvent, replyMsg: com.mikuac.shiro.dto.event.message.MessageEvent): String {
        // 尝试从群成员信息中获取用户名称
        // 如果无法获取，使用QQ号
        try {
            val memberInfo = event.bot?.getGroupMemberInfo(event.groupId, replyMsg.userId, false)
            return memberInfo?.data?.card?.takeIf { it.isNotEmpty() }
                ?: memberInfo?.data?.nickname
                ?: replyMsg.userId.toString()
        } catch (e: Exception) {
            return replyMsg.userId.toString()
        }
    }
    
    /**
     * 构建回复消息
     */
    private fun buildReplyMessage(
        senderName: String, senderId: Long,
        targetName: String, targetId: Long,
        keyword: String, additionalText: String
    ): String {
        // 使用CQ码@用户
        val senderMention = "[CQ:at,qq=$senderId]"
        val targetMention = if (targetName == "自己") {
            "自己"
        } else {
            "[CQ:at,qq=$targetId]"
        }
        
        return if (additionalText.isEmpty()) {
            "$senderMention ${keyword}了 $targetMention！"
        } else {
            "$senderMention $keyword $targetMention $additionalText！"
        }
    }
    
    /**
     * 检查字符是否为ASCII字符
     */
    private fun isASCII(c: Char): Boolean {
        return c.code <= 127
    }
}
