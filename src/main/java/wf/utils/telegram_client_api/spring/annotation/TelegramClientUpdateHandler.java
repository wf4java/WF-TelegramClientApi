package wf.utils.telegram_client_api.spring.annotation;

import it.tdlight.jni.TdApi;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TelegramClientUpdateHandler {

    Class<? extends TdApi.Update> updateType();


}
