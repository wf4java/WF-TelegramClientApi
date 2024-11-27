package wf.utils.telegram_client_api.spring.annotation;

import wf.utils.telegram_client_api.models.SenderSelectorType;

import java.lang.annotation.*;



/**
 (String text, long chatId, TdApi.Message message, boolean itsMe, TdApi.UpdateNewMessage update, ClientExecutor clientExecutor)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TelegramClientTextMessageHandler {

    SenderSelectorType selectorType() default SenderSelectorType.ALL;

}
