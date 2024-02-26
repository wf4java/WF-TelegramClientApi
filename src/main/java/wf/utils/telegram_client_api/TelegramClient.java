package wf.utils.telegram_client_api;

import it.tdlight.client.*;
import it.tdlight.common.ExceptionHandler;
import it.tdlight.common.Init;
import it.tdlight.common.utils.CantLoadLibrary;
import it.tdlight.jni.TdApi;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import wf.utils.telegram_client_api.models.ClientExecutor;
import wf.utils.telegram_client_api.models.MessageHandler;
import wf.utils.telegram_client_api.models.SimpleAuthorizationState;
import wf.utils.telegram_client_api.models.exception.TelegramClientRequestException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;


@Log4j2
@Getter
public class TelegramClient {

    @Getter(AccessLevel.NONE)
    private final CountDownLatch authWaitLatch;

    private final SimpleTelegramClient client;
    private final ClientExecutor clientExecutor;
    private final List<MessageHandler> messageHandlers;

    private TdApi.User me;
    private TdApi.AuthorizationState authorizationState;
    private SimpleAuthorizationState simpleAuthorizationState;





    static {
        try { Init.start(); }
        catch (CantLoadLibrary e) {throw new RuntimeException(e);}
    }


    public TelegramClient(int apiId, String apiHash) {
        this(createDefaultSettings(apiId, apiHash) );
    }
    public TelegramClient(TDLibSettings settings) {
        this(settings, AuthenticationData.qrCode());
    }

    @SneakyThrows
    public TelegramClient(TDLibSettings settings, AuthenticationData authenticationData) {
        this.authWaitLatch = new CountDownLatch(1);
        this.client = new SimpleTelegramClient(settings);
        this.messageHandlers = new CopyOnWriteArrayList<>();
        this.clientExecutor = new ClientExecutor(this);

        this.client.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);
        this.client.addUpdatesHandler(this::onUpdate);
        this.client.addDefaultExceptionHandler(this::onException);

        this.client.start(authenticationData);

        this.authWaitLatch.await();

        if(simpleAuthorizationState != SimpleAuthorizationState.LOGGED)
            throw new RuntimeException("Failed to connect/login to Telegram!");

        this.loadMe();
    }





    public <T extends TdApi.Update> void addCommandHandler(String commandName, CommandHandler handler) {
        this.client.addCommandHandler(commandName, handler);
    }

    public void addMessageHandler(MessageHandler messageHandler) {
        this.messageHandlers.add(messageHandler);
    }

    public <T extends TdApi.Update> void addUpdateHandler(Class<T> updateType, GenericUpdateHandler<T> handler) {
        this.client.addUpdateHandler(updateType, handler);
    }


    public void addUpdatesHandler(GenericUpdateHandler<TdApi.Update> handler) {
        this.client.addUpdatesHandler(handler);
    }

    public void addUpdateExceptionHandler(ExceptionHandler updateExceptionHandler) {
        this. client.addUpdateExceptionHandler(updateExceptionHandler);
    }

    public void addDefaultExceptionHandler(ExceptionHandler defaultExceptionHandlers) {
        this.client.addDefaultExceptionHandler(defaultExceptionHandlers);
    }


    public void onException(Throwable throwable) {
        log.error("Exception occurred in Telegram client", throwable);
    }

    public void onUpdate(TdApi.Update update) {
        for (MessageHandler messageHandler : messageHandlers)
            messageHandler.onUpdate(update, clientExecutor);

        if(update instanceof TdApi.UpdateNewMessage updateNewMessage) {
            TdApi.Message message = updateNewMessage.message;
            for (MessageHandler messageHandler : messageHandlers)
                messageHandler.onMessage(message.chatId, message, clientExecutor, itsMe(message.senderId), (TdApi.UpdateNewMessage) update);

            if(message.content instanceof TdApi.MessageText messageText)
                for (MessageHandler messageHandler : messageHandlers)
                    messageHandler.onTextMessage(messageText.text.text, message.chatId, message, clientExecutor, itsMe(message.senderId), (TdApi.UpdateNewMessage) update);
        }
    }


    private void onUpdateAuthorizationState(TdApi.UpdateAuthorizationState update) {
        TdApi.AuthorizationState authorizationState = update.authorizationState;

        this.authorizationState = authorizationState;
        this.simpleAuthorizationState = SimpleAuthorizationState.getFromAuthorizationState(authorizationState);


        if (authorizationState.getConstructor() == TdApi.AuthorizationStateReady.CONSTRUCTOR)
            log.info("Logged in");

        else if (authorizationState.getConstructor() == TdApi.AuthorizationStateClosing.CONSTRUCTOR)
            log.info("Closing...");

        else if (authorizationState.getConstructor() == TdApi.AuthorizationStateClosed.CONSTRUCTOR)
            log.info("Closed");

        else if (authorizationState.getConstructor() == TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR)
            log.info("Logging out...");


        if(simpleAuthorizationState != SimpleAuthorizationState.WAIT)
            authWaitLatch.countDown();

    }

    private void loadMe() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Result<TdApi.User>> userResultRef = new AtomicReference<>();

        this.client.send(new TdApi.GetMe(), result -> {
            userResultRef.set(result);
            latch.countDown();
        });

        try { latch.await(); }
        catch (InterruptedException e) { throw new RuntimeException("Failed to get user information", e); }

        Result<TdApi.User> userResult = userResultRef.get();
        if (userResult.isError())
            throw new TelegramClientRequestException(userResult.getError().message);

        this.me = userResult.get();
    }

    public boolean isLoggedIn() {
        return simpleAuthorizationState == SimpleAuthorizationState.LOGGED;
    }

    public boolean itsMe(long id) {
        return id == me.id;
    }

    public boolean itsMe(TdApi.MessageSender messageSender) {
        return itsMe(getSenderId(messageSender));
    }


    private static TDLibSettings createDefaultSettings(int apiId, String apiHash) {
        TDLibSettings settings = TDLibSettings.create(new APIToken(apiId, apiHash));

        Path sessionPath = Paths.get("telegram-data");
        settings.setDatabaseDirectoryPath(sessionPath.resolve("database"));
        settings.setDownloadedFilesDirectoryPath(sessionPath.resolve("downloads"));

        return settings;
    }


    public static long getSenderId(TdApi.MessageSender sender){
        if(sender instanceof TdApi.MessageSenderUser user){
            return user.userId;
        }else if(sender instanceof TdApi.MessageSenderChat chat){
            return chat.chatId;
        }
        return 0;
    }

    public static TdApi.InputMessageText inputMessageTextFromText(String text) {
        return new TdApi.InputMessageText(new TdApi.FormattedText(text, new TdApi.TextEntity[0]), false, false);
    }

}
