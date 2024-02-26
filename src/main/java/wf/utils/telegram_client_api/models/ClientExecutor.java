package wf.utils.telegram_client_api.models;

import it.tdlight.client.GenericResultHandler;
import it.tdlight.client.Result;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import wf.utils.telegram_client_api.TelegramClient;
import wf.utils.telegram_client_api.models.exception.TelegramClientRequestException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@RequiredArgsConstructor
public class ClientExecutor {

    private final TelegramClient telegramClient;





    public TdApi.Message sendMessage(Long chatId, String text) {
        TdApi.SendMessage message = new TdApi.SendMessage();
        message.chatId = chatId;
        message.inputMessageContent = inputMessageTextFromText(text);
        return send(message);
    }

    @SneakyThrows
    public CompletableFuture<TdApi.Message> sendMessageAsync(Long chatId, String text) {
        TdApi.SendMessage message = new TdApi.SendMessage();
        message.chatId = chatId;
        message.inputMessageContent = inputMessageTextFromText(text);
        return sendAsync(message);
    }

    @SneakyThrows
    public TdApi.Message sendMessageReply(Long chatId, long replyId, String text) {
        TdApi.SendMessage message = new TdApi.SendMessage();
        message.chatId = chatId;
        message.replyToMessageId = replyId;
        message.inputMessageContent = inputMessageTextFromText(text);
        return send(message);
    }

    @SneakyThrows
    public CompletableFuture<TdApi.Message> sendMessageReplyAsync(Long chatId, long replyId, String text) {
        TdApi.SendMessage message = new TdApi.SendMessage();
        message.chatId = chatId;
        message.replyToMessageId = replyId;
        message.inputMessageContent = inputMessageTextFromText(text);
        return sendAsync(message);
    }

    @SneakyThrows
    public TdApi.Message editMessageText(Long chatId, long messageId, String newText) {
        TdApi.EditMessageText editMessage = new TdApi.EditMessageText();
        editMessage.chatId = chatId;
        editMessage.messageId = messageId;
        editMessage.inputMessageContent = inputMessageTextFromText(newText);
        return send(editMessage);
    }

    @SneakyThrows
    public CompletableFuture<TdApi.Message> editMessageTextAsync(Long chatId, long messageId, String newText) {
        TdApi.EditMessageText editMessage = new TdApi.EditMessageText();
        editMessage.chatId = chatId;
        editMessage.messageId = messageId;
        editMessage.inputMessageContent = inputMessageTextFromText(newText);
        return sendAsync(editMessage);
    }

    @SneakyThrows
    public void deleteMessage(Long chatId, long messageId) {
        TdApi.DeleteMessages deleteMessages = new TdApi.DeleteMessages();
        deleteMessages.chatId = chatId;
        deleteMessages.messageIds = new long[]{messageId};
        sendForOk(deleteMessages);
    }

    @SneakyThrows
    public void deleteMessageAsync(Long chatId, long messageId) {
        TdApi.DeleteMessages deleteMessages = new TdApi.DeleteMessages();
        deleteMessages.chatId = chatId;
        deleteMessages.messageIds = new long[]{messageId};
        sendForOk(deleteMessages);
    }


    @SneakyThrows
    public TdApi.Message forwardMessage(Long chatId, Long fromChatId, long messageId) {
        TdApi.ForwardMessages forwardMessage = new TdApi.ForwardMessages();
        forwardMessage.chatId = chatId;
        forwardMessage.fromChatId = fromChatId;
        forwardMessage.messageIds = new long[]{messageId};
        return send(forwardMessage).messages[0];
    }

    @SneakyThrows
    public CompletableFuture<TdApi.Messages> forwardMessageAsync(Long chatId, Long fromChatId, long messageId) {
        TdApi.ForwardMessages forwardMessage = new TdApi.ForwardMessages();
        forwardMessage.chatId = chatId;
        forwardMessage.fromChatId = fromChatId;
        forwardMessage.messageIds = new long[]{messageId};
        return sendAsync(forwardMessage);
    }




    public <R extends TdApi.Object> CompletableFuture<R> sendAsync(TdApi.Function<R> function) {
        CompletableFuture<R> future = new CompletableFuture<>();

        send(function, result -> {
            if (result.isError()) {
                future.completeExceptionally(new TelegramClientRequestException(result.getError().message));
            } else {
                future.complete(result.get());
            }
        });

        return future;
    }

    public void sendForOk(TdApi.Function<TdApi.Ok> function) {
        send(function, result -> { });
    }

    public <R extends TdApi.Object> R send(TdApi.Function<R> function) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Result<R>> userResultRef = new AtomicReference<>();

        send(function, result -> {
            try { userResultRef.set(result);}
            finally { latch.countDown(); }
        });

        try { latch.await(); }
        catch (InterruptedException e) { e.printStackTrace(); throw new RuntimeException(e); }

        Result<R> result = userResultRef.get();
        if (result.isError())
            throw new TelegramClientRequestException(result.getError().message);

        return result.get();
    }


    public <R extends TdApi.Object> void send(TdApi.Function<R> function, GenericResultHandler<R> resultHandler) {
        telegramClient.getClient().send(function, resultHandler);
    }

    @Deprecated
    public <R extends TdApi.Object> Result<R> execute(TdApi.Function<R> function) {
        return telegramClient.getClient().execute(function);
    }


    public TdApi.User getMe() {
        return telegramClient.getMe();
    }


    public boolean itsMe(long id) {
        return telegramClient.itsMe(id);
    }

    public boolean itsMe(TdApi.MessageSender messageSender) {
        return telegramClient.itsMe(messageSender);
    }

    public static long getSenderId(TdApi.MessageSender sender) {
        return TelegramClient.getSenderId(sender);
    }

    public static TdApi.InputMessageText inputMessageTextFromText(String text) {
        return TelegramClient.inputMessageTextFromText(text);
    }

    public SimpleTelegramClient getClient() {
        return telegramClient.getClient();
    }

}
