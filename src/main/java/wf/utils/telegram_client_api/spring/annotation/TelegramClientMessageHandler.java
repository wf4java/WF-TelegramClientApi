package wf.utils.telegram_client_api.spring.annotation;

import org.springframework.core.annotation.AliasFor;
import wf.utils.telegram_client_api.models.SenderSelectorType;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TelegramClientMessageHandler {

    SenderSelectorType selectorType() default SenderSelectorType.ALL;

}
