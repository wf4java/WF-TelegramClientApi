package wf.utils.telegram_client_api.spring.annotation;


import wf.utils.telegram_client_api.models.SenderSelectorType;

import java.lang.annotation.*;


/**
 (Long chatId, TdApi.Message message, Boolean itsMe, TdApi.UpdateNewMessage update, ClientExecutor clientExecutor)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TelegramClientMessageHandler {
    
    SenderSelectorType selectorType() default SenderSelectorType.ALL;

}
