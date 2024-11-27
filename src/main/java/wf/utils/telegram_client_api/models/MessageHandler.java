package wf.utils.telegram_client_api.models;


import it.tdlight.jni.TdApi;

public interface MessageHandler {


    public default void onTextMessage(String text, Long chatId, TdApi.Message message, Boolean itsMe, TdApi.UpdateNewMessage update, ClientExecutor clientExecutor) { }


    public default void onMessage(Long chatId, TdApi.Message message, Boolean itsMe, TdApi.UpdateNewMessage update, ClientExecutor clientExecutor) { }


    public default void onUpdate(TdApi.Update update, ClientExecutor clientExecutor) { }



}
