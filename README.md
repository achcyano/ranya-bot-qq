# ranya-bot-qq
Ranya#1是娱乐向的QQ机器人

## 功能特性

### Quote Reply - 引用回复功能

参考 [XiaoMengXinX/Telegram_QuoteReply_Bot-Go](https://github.com/XiaoMengXinX/Telegram_QuoteReply_Bot-Go) 实现的引用回复功能，支持通过简单的命令生成有趣的互动消息。

**基本用法：**
- `/动词 [补充内容]` - 对自己或回复的消息执行动作
- `\动词 [补充内容]` - 反转主客体（回复消息时使用）
- `/动词@QQ号 [补充内容]` - 指定目标用户

详细说明请查看 [QUOTE_REPLY_FEATURE.md](QUOTE_REPLY_FEATURE.md)

## 构建和运行

1. 复制 `src/main/resources/application.yaml.template` 为 `application.yaml`
2. 配置 Shiro WebSocket 服务器地址和 API 密钥
3. 运行应用：`./gradlew bootRun`

## 致谢

- Quote Reply 功能设计灵感来源于 [XiaoMengXinX/Telegram_QuoteReply_Bot-Go](https://github.com/XiaoMengXinX/Telegram_QuoteReply_Bot-Go)

