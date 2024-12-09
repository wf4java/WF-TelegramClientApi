package wf.utils.telegram_client_api;

import it.tdlight.ClientFactory;
import it.tdlight.Init;
import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import it.tdlight.util.UnsupportedNativeLibraryException;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;


@Log4j2
@Getter
public class TelegramClient {

    @Getter(AccessLevel.NONE)
    private final CountDownLatch authWaitLatch;
    @Getter(AccessLevel.NONE)
    private final ExecutorService executorService;

    private final SimpleTelegramClient client;
    private final ClientExecutor clientExecutor;
    private final List<MessageHandler> messageHandlers;


    private TdApi.User me;
    private TdApi.AuthorizationState authorizationState;
    private SimpleAuthorizationState simpleAuthorizationState;





    static {
        try { Init.init(); }
        catch (UnsupportedNativeLibraryException e) {throw new RuntimeException(e);}
    }


    public TelegramClient(int apiId, String apiHash) {
        this(createDefaultSettings(apiId, apiHash) );
    }
    public TelegramClient(TDLibSettings settings) {
        this(settings, AuthenticationSupplier.qrCode());
    }

    @SneakyThrows
    public TelegramClient(TDLibSettings settings, AuthenticationSupplier authenticationSupplier) {
        try (SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory()) {

            this.authWaitLatch = new CountDownLatch(1);

            SimpleTelegramClientBuilder builder = clientFactory.builder(settings);
            this.messageHandlers = new CopyOnWriteArrayList<>();
            this.clientExecutor = new ClientExecutor(this);
            this.executorService = new ThreadPoolExecutor(3, 25, 120, TimeUnit.SECONDS, new SynchronousQueue<>());

            builder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);
            builder.addUpdatesHandler(this::onUpdate);
            builder.addDefaultExceptionHandler(this::onException);

            this.client = builder.build(authenticationSupplier);

            this.authWaitLatch.await();

            if (simpleAuthorizationState != SimpleAuthorizationState.LOGGED)
                throw new RuntimeException("Failed to connect/login to Telegram!");

            this.loadMe();
        }
    }






    public void addMessageHandler(MessageHandler messageHandler) {
        this.messageHandlers.add(messageHandler);
    }




    private void onException(Throwable throwable) {
        log.error("Exception occurred in Telegram client", throwable);
    }

    private void onUpdate(TdApi.Update update) {
        executorService.submit(() -> {
            for (MessageHandler messageHandler : messageHandlers)
                messageHandler.onUpdate(update, clientExecutor);

            if(update instanceof TdApi.UpdateNewMessage updateNewMessage) {
                TdApi.Message message = updateNewMessage.message;
                for (MessageHandler messageHandler : messageHandlers)
                    messageHandler.onMessage(message.chatId, message, itsMe(message.senderId), (TdApi.UpdateNewMessage) update, clientExecutor);

                if(message.content instanceof TdApi.MessageText messageText)
                    for (MessageHandler messageHandler : messageHandlers)
                        messageHandler.onTextMessage(messageText.text.text, message.chatId, message, itsMe(message.senderId), (TdApi.UpdateNewMessage) update, clientExecutor);
            }
        });
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
        this.me = clientExecutor.send(new TdApi.GetMe());
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
        return new TdApi.InputMessageText(new TdApi.FormattedText(text, new TdApi.TextEntity[0]), new TdApi.LinkPreviewOptions(), false);
    }

}
