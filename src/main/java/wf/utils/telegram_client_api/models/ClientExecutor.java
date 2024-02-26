package wf.utils.telegram_client_api.models;

import it.tdlight.client.GenericResultHandler;
import it.tdlight.client.Result;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import wf.utils.telegram_client_api.TelegramClient;
import wf.utils.telegram_client_api.models.exception.TelegramClientRequestException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@RequiredArgsConstructor
public class ClientExecutor {

    private final TelegramClient telegramClient;




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
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Result<R>> userResultRef = new AtomicReference<>();

        send(function, result -> {
            userResultRef.set(result);
            latch.countDown();
        });

        try { latch.await(); }
        catch (InterruptedException e) { throw new RuntimeException(e); }

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

    public SimpleTelegramClient getClient() {
        return telegramClient.getClient();
    }

}
