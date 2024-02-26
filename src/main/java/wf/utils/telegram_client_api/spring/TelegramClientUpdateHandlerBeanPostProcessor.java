package wf.utils.telegram_client_api.spring;

import it.tdlight.common.ConstructorDetector;
import it.tdlight.jni.TdApi;
import lombok.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Lazy;
import wf.utils.telegram_client_api.TelegramClient;
import wf.utils.telegram_client_api.models.ClientExecutor;
import wf.utils.telegram_client_api.models.SenderSelectorType;
import wf.utils.telegram_client_api.spring.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Getter
public class TelegramClientUpdateHandlerBeanPostProcessor implements BeanPostProcessor {


    private final TelegramClient telegramClient;

    private final ArrayList<UpdateHandler> updateHandlers = new ArrayList<>(4);
    private final ArrayList<MessageHandler> messageHandlers = new ArrayList<>(4);
    private final ArrayList<MessageHandler> textMessageHandlers = new ArrayList<>(4);


    @Lazy
    public TelegramClientUpdateHandlerBeanPostProcessor(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
        telegramClient.addMessageHandler(new wf.utils.telegram_client_api.models.MessageHandler() {
            @Override
            public void onTextMessage(String text, Long chatId, TdApi.Message message, ClientExecutor clientExecutor, Boolean itsMe, TdApi.UpdateNewMessage update) {
                for(MessageHandler messageHandler : textMessageHandlers) {
                    if(messageHandler.getSenderSelectorType() == SenderSelectorType.ONLY_NOT_MY && itsMe) return;
                    if(messageHandler.getSenderSelectorType() == SenderSelectorType.ONLY_MY && !itsMe) return;

                    invoke(messageHandler.getHandledMethod(), text, chatId, message, clientExecutor, itsMe, update);
                }
            }

            @Override
            public void onMessage(Long chatId, TdApi.Message message, ClientExecutor clientExecutor, Boolean itsMe, TdApi.UpdateNewMessage update) {
                for(MessageHandler messageHandler : messageHandlers) {
                    if(messageHandler.getSenderSelectorType() == SenderSelectorType.ONLY_NOT_MY && itsMe) return;
                    if(messageHandler.getSenderSelectorType() == SenderSelectorType.ONLY_MY && !itsMe) return;

                    invoke(messageHandler.getHandledMethod(), chatId, message, clientExecutor, itsMe, update);
                }
            }

            @Override
            public void onUpdate(TdApi.Update update, ClientExecutor clientExecutor) {
                for(UpdateHandler updateHandler : updateHandlers) {
                    if(updateHandler.getUpdateType()
                            != null && ConstructorDetector.getConstructor(updateHandler.getUpdateType()) != update.getConstructor()) return;

                    invoke(updateHandler.getHandledMethod(), updateHandler.getUpdateType() != null ?
                            updateHandler.getUpdateType().cast(update) : update);
                }
            }
        });
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        if (beanClass.isAnnotationPresent(TelegramClientController.class)) {
            Method[] methods = beanClass.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(TelegramClientUpdateHandler.class))
                    updateHandlers.add(new UpdateHandler(new HandledMethod(bean, method),
                            method.getAnnotation(TelegramClientUpdateHandler.class).updateType()));

                if (method.isAnnotationPresent(TelegramClientMessageHandler.class))
                    messageHandlers.add(new MessageHandler(new HandledMethod(bean, method),
                            method.getAnnotation(TelegramClientMessageHandler.class).selectorType()));

                if (method.isAnnotationPresent(TelegramClientTextMessageHandler.class))
                    messageHandlers.add(new MessageHandler(new HandledMethod(bean, method),
                            method.getAnnotation(TelegramClientTextMessageHandler.class).selectorType()));
            }
        }
        return bean;
    }



    private static void invoke(HandledMethod handledMethod, Object... objects) {
        Class<?>[] parameterTypes = handledMethod.getMethod().getParameterTypes();
        List<Object> args = new ArrayList<>();

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            for (Object object : objects) { if (parameterType.isInstance(object)) { args.add(object); break; } }
            if (args.size() <= i) { args.add(null); }
        }

        try { handledMethod.getMethod().invoke(handledMethod.getObj(), args.toArray()); }
        catch (IllegalAccessException | InvocationTargetException e) {throw new RuntimeException(e);}
    }



    @Getter
    @RequiredArgsConstructor
    private final static class HandledMethod {
        private final Object obj;
        private final Method method;
    }

    @Getter
    @RequiredArgsConstructor
    private final static class MessageHandler {
        private final HandledMethod handledMethod;
        private final SenderSelectorType senderSelectorType;
    }

    @Getter
    @RequiredArgsConstructor
    private final static class UpdateHandler {
        private final HandledMethod handledMethod;
        private final Class<? extends TdApi.Update> updateType;
    }

}
