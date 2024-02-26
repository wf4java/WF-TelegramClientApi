import it.tdlight.client.TelegramError;
import it.tdlight.jni.TdApi;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import wf.utils.telegram_client_api.TelegramClient;

public class Test {

    private static TelegramClient telegramClient;

    public static void main(String[] args) {
        configureLog4j();
        telegramClient = new TelegramClient(62187451,"6f5d7980a54aee778293300284df2bf0");

        for (int i = 0; i < 200; i++) {
            System.out.println(telegramClient.getMe().firstName);
        }
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
