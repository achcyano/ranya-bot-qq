package me.achqing.ranya1.plugin

import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.annotation.MessageHandlerFilter
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.enums.AtEnum
import org.springframework.stereotype.Component
import java.util.regex.Matcher


@Shiro
@Component
class Test0 {
    /*
     * @param bot
     * @param event
     * @param matcher
     */
    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NEED)
    fun hello(bot: Bot,event: GroupMessageEvent, matcher: Matcher?) {
        // 以注解方式调用可以根据自己的需要来为方法设定参数
        // 例如群组消息可以传递 GroupMessageEvent, Bot, Matcher 多余的参数会被设定为 null
        println(event.getMessage())
        bot.sendGroupMsg(event.groupId, "Hello, world!",false)

    }
}
