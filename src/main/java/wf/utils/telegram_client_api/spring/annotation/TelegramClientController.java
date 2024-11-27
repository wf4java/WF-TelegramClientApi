package wf.utils.telegram_client_api.spring.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface TelegramClientController {

}
