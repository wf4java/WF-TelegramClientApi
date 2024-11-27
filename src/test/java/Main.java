import it.tdlight.jni.TdApi;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import wf.utils.telegram_client_api.TelegramClient;
import wf.utils.telegram_client_api.models.ClientExecutor;
import wf.utils.telegram_client_api.models.MessageHandler;

public class Main {


    public static void main(String[] args) {
        configureLog4j();

        TelegramClient telegramClient = new TelegramClient(0, "ApiHash");

        telegramClient.getClientExecutor().sendMessage(0L, "Hello man");

        //Delete all messages i send
        telegramClient.addMessageHandler(new MessageHandler() {
            @Override
            public void onTextMessage(String text, Long chatId, TdApi.Message message, Boolean itsMe, TdApi.UpdateNewMessage update, ClientExecutor clientExecutor) {

                if(!itsMe) return;
                clientExecutor.deleteMessage(chatId, message.id);
            }
        });

    }



    private static void configureLog4j() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setStatusLevel(Level.INFO);
        AppenderComponentBuilder consoleAppender = builder.newAppender("Console", "CONSOLE")
                .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);

        LayoutComponentBuilder patternLayout = builder.newLayout("PatternLayout")
                .addAttribute("pattern", "%highlight{%d{yyyy-MM-dd}T%d{HH:mm:ss} [%t] %-5level: %msg%n}{STYLE=Logback}");

        consoleAppender.add(patternLayout);

        builder.add(consoleAppender);

        builder.add(builder.newRootLogger(Level.INFO).add(builder.newAppenderRef("Console")));

        Configuration configuration = builder.build();

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        context.start(configuration);
    }

}
