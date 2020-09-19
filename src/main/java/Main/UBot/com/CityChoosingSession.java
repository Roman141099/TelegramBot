package Main.UBot.com;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.function.BiConsumer;

public class CityChoosingSession extends Session {
    private boolean saidHello;
    private final transient List<String> buttons = new ArrayList<>(Arrays.asList("Да, почему нет", "Нет, спасибо"));
    private final transient BiConsumer<SendMessage, User> buttonsMarkUp = this::setButtons;
    @Override
    public String nextStep(String inputTxt, Message message, User user) {
        if(!saidHello){
            saidHello = true;
            return "Эй! Я могу показывать тебе прогноз погоды в городах по всему миру. Для твоего удобства ты можешь сразу добавить приоритетный для тебя город, " +
                    "чтобы тебе было удобнее и быстрее узнавать погоду там, где ты хочешь. Ебанем?";
        }
        if(inputTxt.equalsIgnoreCase("нет, спасибо")){
            terminateAllProcesses();
            return "ОК! Как хочешь :)";
        }else if(inputTxt.equalsIgnoreCase("да, почему нет")){
            buttons.remove(0);
            return "OK! Задай город";
        }
        Optional<WeatherForecast> wp = Weather.getWeatherNow(inputTxt);
        if(wp.isEmpty()){
            return WeatherSession.wrongNameCityMessage[new Random().nextInt(4)];
        }
        user.addCity((inputTxt.toUpperCase().charAt(0) + inputTxt.toLowerCase().substring(1)).trim());
        SendSticker sendSticker = new SendSticker();
        sendSticker.setChatId(message.getChatId());
        sendSticker.setSticker("CAACAgIAAxkBAAIqcl9ac3LA5yVVR-SFL71iDr6kisY2AAIVAAPANk8TzVamO2GeZOcbBA");
        try {
            new MainBotController().execute(sendSticker);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        terminateAllProcesses();
        return "Готово!";
    }

    @Override
    public BiConsumer<SendMessage, User> getButtonsMarkUp() {
        return buttonsMarkUp;
    }

    private void setButtons(SendMessage sendMessage, User user){
        ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup();
        rkm.setResizeKeyboard(true);
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.addAll(buttons);
        rkm.setKeyboard(List.of(keyboardRow));
        sendMessage.setReplyMarkup(rkm);
    }

    @Override
    public void terminateAllProcesses() {
        sessionOpened = saidHello = false;
    }
}
