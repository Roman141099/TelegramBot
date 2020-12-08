package Main.telegram_bot.design.sessions;

import Main.telegram_bot.design.exceptions.NotFoundForecastException;
import Main.telegram_bot.design.starter.Launcher;
import Main.telegram_bot.design.utils.City;
import Main.telegram_bot.design.utils.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import static Main.telegram_bot.design.utils.City.*;

public class CityChoosingSession extends Session {
    Logger LOG = LoggerFactory.getLogger(CityChoosingSession.class);
    private boolean saidHello;
    private boolean resultRecieved;
    private transient List<String> buttons = new ArrayList<>(Arrays.asList("Да, почему нет", "Нет, спасибо"));
    private final transient BiConsumer<SendMessage, User> buttonsMarkUp = this::setButtons;
    @Override
    public String nextStep(String inputTxt, Message message, User user) {
        if(!saidHello){
            saidHello = true;
            return "Эй! Я могу показывать тебе прогноз погоды в городах по всему миру. Для твоего удобства ты можешь сразу добавить приоритетный для тебя город, " +
                    "чтобы тебе было удобнее и быстрее узнавать погоду там, где ты хочешь. Добавим город?";
        }
        if(inputTxt.equalsIgnoreCase("нет, спасибо")){
            terminateAllProcesses();
            return "ОК! Как хочешь :)";
        }else if(inputTxt.equalsIgnoreCase("да, почему нет")){
            buttons.remove(0);
            return "OK! Задай город";
        }
        City choosed;
        double lat;
        double lon;
        if (message.hasLocation()) {
            Location location = message.getLocation();
            lat = location.getLatitude();
            lon = location.getLongitude();
            choosed = cityFromCoordinates(lat, lon);
        } else {
            List<City> locations = resultCitiesArray(inputTxt.trim());
            if (locations.size() == 0) {
                LOG.warn("Not found searched city");
                return WeatherSession.wrongNameCityMessage[new Random().nextInt(4)];
            }
            if(!resultRecieved){
                buttons = locations.stream().map(City::getDisplay_name).collect(Collectors.toList());
                buttons.add(0, "Нет, спасибо");
                resultRecieved = true;
                LOG.info("{} cities found by request", locations.size());
                return "Выбери то, что тебе нужно";
            }
            choosed = locations.stream().filter(o -> o.getDisplay_name().equals(inputTxt)).findFirst().orElseThrow(NotFoundForecastException::new);
        }
        user.addCity(choosed);
        SendSticker sendSticker = new SendSticker();
        sendSticker.setChatId(message.getChatId());
        sendSticker.setSticker("CAACAgIAAxkBAAIqcl9ac3LA5yVVR-SFL71iDr6kisY2AAIVAAPANk8TzVamO2GeZOcbBA");
        try {
            new Launcher().execute(sendSticker);
        } catch (TelegramApiException e) {
            LOG.error("Can't send sticker", e);
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
        List<KeyboardRow> k_row = buttons.stream().map(o -> {
            KeyboardRow kr = new KeyboardRow();
            kr.add(o);
            return kr;
        }).collect(Collectors.toList());
        rkm.setKeyboard(k_row);
        sendMessage.setReplyMarkup(rkm);
    }

    @Override
    public void terminateAllProcesses() {
        sessionOpened = saidHello = false;
    }
}
