package Main.UBot.com;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class Main {
    public static void main(String[] args){
        ApiContextInitializer.init();
        TelegramBotsApi tgBotsAPI = new TelegramBotsApi();
        BotBotBot currentBot = new BotBotBot();
        try {
            tgBotsAPI.registerBot(currentBot);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }

    }
}
