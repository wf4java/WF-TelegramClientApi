# WF-TelegramClientApi:
## Maven:
`Java(min): 18`
```xml
  <dependency>
    <groupId>io.github.wf4java</groupId>
    <artifactId>WF-TelegramClientApi</artifactId>
    <version>1.8.0</version>
  </dependency>
```

## Get telegram api id: 
https://core.telegram.org/api/obtaining_api_id

## Example:

```java
TelegramClient telegramClient = new TelegramClient(apiId, "ApiHash");

telegramClient.getClientExecutor().sendMessage(chatId, "Hello man");

//Delete all messages i send
telegramClient.addMessageHandler(new MessageHandler() {
    @Override
    public void onTextMessage(String text, Long chatId, TdApi.Message message, ClientExecutor clientExecutor, Boolean itsMe, TdApi.UpdateNewMessage update) {
        if(!itsMe) return;
        clientExecutor.deleteMessage(chatId, message.id);
    }
});

```
ㅤ