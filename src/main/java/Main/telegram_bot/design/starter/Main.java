package Main.telegram_bot.design.starter;

import ch.qos.logback.classic.util.ContextInitializer;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class Main {
    public static void main(String[] args){
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "src/main/resources/logback.xml");

        LoggerFactory.getLogger(Main.class).info("STARTING BOT");

        ApiContextInitializer.init();
        TelegramBotsApi tgBotsAPI = new TelegramBotsApi();
        Launcher currentBot = new Launcher();
        try {
            tgBotsAPI.registerBot(currentBot);
        } catch (TelegramApiRequestException e) {
            LoggerFactory.getLogger(Main.class).error("Error with starting bot", e);
        }
    }
}
