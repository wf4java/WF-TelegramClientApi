package wf.utils.telegram_client_api.models;

import it.tdlight.client.GenericResultHandler;
import it.tdlight.client.Result;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import wf.utils.telegram_client_api.TelegramClient;
import wf.utils.telegram_client_api.models.exception.TelegramClientRequestException;

import java.util.Collection;
import java.util.concurrent.*;

@Getter
@RequiredArgsConstructor
public class ClientExecutor {

    private final TelegramClient telegramClient;




    public int getChatMessagesCount(long chatId){
        return send(new TdApi.GetChatMessageCount(chatId, new TdApi.SearchMessagesFilterEmpty(),false)).count;
    }

    public TdApi.Ok setReaction(long chatId, long messageId, String emoji){
        return send(new TdApi.AddMessageReaction(chatId, messageId, new TdApi.ReactionTypeEmoji(emoji),false,true));
    }


    public CompletableFuture<TdApi.Ok> setReactionAsync(long chatId, long messageId, String emoji){
        return sendAsync(new TdApi.AddMessageReaction(chatId, messageId, new TdApi.ReactionTypeEmoji(emoji),false,true));
    }


    public TdApi.Chat getChat(long chatId) {
        return send(new TdApi.GetChat(chatId));
    }

    public CompletableFuture<TdApi.Chat> getChatAsync(long chatId) {
        return sendAsync(new TdApi.GetChat(chatId));
    }

    public TdApi.Message getMessage(long chatId, long messageId){
        return send(new TdApi.GetMessage(chatId, messageId));
    }

    public CompletableFuture<TdApi.Message> getMessageAsync(long chatId, long messageId){
        return sendAsync(new TdApi.GetMessage(chatId, messageId));
    }

    public TdApi.Messages getChatHistory(long chatId, long messagesId, int limit, int offset) {
        return send(new TdApi.GetChatHistory(chatId, messagesId, offset, limit, false));
    }

    public CompletableFuture<TdApi.Messages> getChatHistoryAsync(long chatId, long messagesId, int limit, int offset) {
        return sendAsync(new TdApi.GetChatHistory(chatId, messagesId, offset, limit, false));
    }




    public TdApi.Message sendMessage(Long chatId, String text) {
        TdApi.SendMessage message = new TdApi.SendMessage();
        message.chatId = chatId;
        message.inputMessageContent = inputMessageTextFromText(text);
        return send(message);
    }


    public CompletableFuture<TdApi.Message> sendMessageAsync(Long chatId, String text) {
        TdApi.SendMessage message = new TdApi.SendMessage();
        message.chatId = chatId;
        message.inputMessageContent = inputMessageTextFromText(text);
        return sendAsync(message);
    }

    
    public TdApi.Message sendMessageReply(Long chatId, long replyId, String text) {
        TdApi.SendMessage message = new TdApi.SendMessage();
        message.chatId = chatId;
        message.replyToMessageId = replyId;
        message.inputMessageContent = inputMessageTextFromText(text);
        return send(message);
    }

    
    public CompletableFuture<TdApi.Message> sendMessageReplyAsync(Long chatId, long replyId, String text) {
        TdApi.SendMessage message = new TdApi.SendMessage();
        message.chatId = chatId;
        message.replyToMessageId = replyId;
        message.inputMessageContent = inputMessageTextFromText(text);
        return sendAsync(message);
    }

    
    public TdApi.Message editMessageText(Long chatId, long messageId, String newText) {
        TdApi.EditMessageText editMessage = new TdApi.EditMessageText();
        editMessage.chatId = chatId;
        editMessage.messageId = messageId;
        editMessage.inputMessageContent = inputMessageTextFromText(newText);
        return send(editMessage);
    }

    
    public CompletableFuture<TdApi.Message> editMessageTextAsync(Long chatId, long messageId, String newText) {
        TdApi.EditMessageText editMessage = new TdApi.EditMessageText();
        editMessage.chatId = chatId;
        editMessage.messageId = messageId;
        editMessage.inputMessageContent = inputMessageTextFromText(newText);
        return sendAsync(editMessage);
    }

    
    public TdApi.Ok deleteMessage(Long chatId, long messageId) {
        TdApi.DeleteMessages deleteMessages = new TdApi.DeleteMessages();
        deleteMessages.chatId = chatId;
        deleteMessages.messageIds = new long[]{messageId};
        return send(deleteMessages);
    }

    
    public CompletableFuture<TdApi.Ok> deleteMessageAsync(Long chatId, long messageId) {
        TdApi.DeleteMessages deleteMessages = new TdApi.DeleteMessages();
        deleteMessages.chatId = chatId;
        deleteMessages.messageIds = new long[]{messageId};
        return sendAsync(deleteMessages);
    }


    public TdApi.Ok deleteMessages(Long chatId, Collection<Long> messageIds) {
        TdApi.DeleteMessages deleteMessages = new TdApi.DeleteMessages();
        deleteMessages.chatId = chatId;
        deleteMessages.messageIds = messageIds.stream().mapToLong(Long::valueOf).toArray();
        return send(deleteMessages);
    }


    public CompletableFuture<TdApi.Ok> deleteMessageAsync(Long chatId, Collection<Long> messageIds) {
        TdApi.DeleteMessages deleteMessages = new TdApi.DeleteMessages();
        deleteMessages.chatId = chatId;
        deleteMessages.messageIds = messageIds.stream().mapToLong(Long::valueOf).toArray();
        return sendAsync(deleteMessages);
    }



    public TdApi.Message forwardMessage(Long chatId, Long fromChatId, long messageId) {
        TdApi.ForwardMessages forwardMessage = new TdApi.ForwardMessages();
        forwardMessage.chatId = chatId;
        forwardMessage.fromChatId = fromChatId;
        forwardMessage.messageIds = new long[]{messageId};
        return send(forwardMessage).messages[0];
    }

    
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



    public <R extends TdApi.Object> R send(TdApi.Function<R> function) {
        return sendAsync(function).join();
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
