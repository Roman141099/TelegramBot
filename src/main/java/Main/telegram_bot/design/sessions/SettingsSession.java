package Main.telegram_bot.design.sessions;

import Main.telegram_bot.design.starter.Launcher;
import Main.telegram_bot.design.utils.City;
import Main.telegram_bot.design.utils.User;
import Main.telegram_bot.design.weather.Weather;
import Main.telegram_bot.design.weather.WeatherForecast;
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

public class SettingsSession extends Session {
    private static final Logger LOG = LoggerFactory.getLogger(SettingsSession.class);
    private boolean resultRecieved;
    private boolean inMainMenu = true;
    private boolean requestedCity;
    private boolean isReadyForEditCity;
    private boolean isReadyToDelete;
    private boolean toShowMapSettings;
    private transient final List<String> defaultButs = List.of("Язык", "Настройки города", "Режим бота", "Остальное", "|HOME|");
    private final transient List<String> cityButs = List.of("➕Добавить город", "⬅Назад", "➖Удалить город", "⌫Удалить все города");
    private transient List<String> buttons = new ArrayList<>();
    private final transient BiConsumer<SendMessage, User> buttonsSets = this::setButtons;

    @Override
    public String nextStep(String inputTxt, Message message, User user) {
        LOG.warn("WARNING");
        if (inputTxt.equalsIgnoreCase("настройки города")) {
            requestedCity = true;
            inMainMenu = false;
            buttons = new ArrayList<>(cityButs);
            int[] indexes = {1};
            return user.citiesList().size() == 0 ? "Ты не добавил ни одного города\nХочешь сделать это?" : "Добавленные города :\n"
                    + user.citiesList().stream().map(o -> (indexes[0]++) + ".'" + o.getDisplay_name() + "'").
                    collect(Collectors.joining(",\n")) + "\nИзменить это?";
        }else if(inputTxt.equalsIgnoreCase("Остальное")) {
            inMainMenu = false;
            buttons = Arrays.asList("Показ карты города - " + (user.isMapShown() ? "✅" : "❌"), "⬅Назад");
            toShowMapSettings = true;
            return "Здесь ты можешь выбрать настройку показа карты города, когда ты запрашиваешь погоду";
        }
        if (toShowMapSettings) {
            if(inputTxt.equalsIgnoreCase("⬅Назад")){
                inMainMenu = true;
                toShowMapSettings = false;
                return "OK";
            }
            toShowMapSettings = false;
            inMainMenu = true;
            user.setShowMap(!user.isMapShown());
            return "Показ карты города успешно " + (user.isMapShown() ? "включен✅" : "выключен❌");
        }
        if (requestedCity) {
            if (inputTxt.equalsIgnoreCase("➕добавить город")) {
                if (user.citiesList().size() + 1 > 6) {
                    return "Извини⛔\nТы можешь добавлять максимум 6 городов." +
                            " Пожалуйста, удали один или несколько чтобы добавить новый";
                }
                buttons = user.citiesList().stream().map(City::getDisplay_name).collect(Collectors.toList());
                buttons.add("⬅Назад");
                isReadyForEditCity = true;
                requestedCity = false;
                return (user.citiesList().size() == 0 ? "Список городов пуст" : "На кнопках представлены названия твоих ранее добавленных городов, помни, что ты у каждого города должно быть уникальное имя")
                        + "\nНапишите название города";
            } else if (inputTxt.equalsIgnoreCase("⬅Назад")) {
                buttons = new ArrayList<>(defaultButs);
                requestedCity = false;
                inMainMenu = true;
                return "OK! Как хочешь";
            } else if (inputTxt.equals("➖Удалить город")) {
                if (user.citiesList().size() == 0) {
                    return "У вас пока нет добавленных городов, сперва сделай это";
                }
                buttons = user.citiesList().stream().map(City::getDisplay_name).collect(Collectors.toList());
                buttons.add("⬅Назад");
                requestedCity = false;
                isReadyForEditCity = true;
                isReadyToDelete = true;
                return "Выбери город для удаления";
            } else if (inputTxt.equals("⌫Удалить все города")) {
                if (user.citiesList().size() == 0) {
                    return "Твой список городов пуст\nХочешь добавить город?";
                }
                user.citiesList().clear();
                isReadyForEditCity = requestedCity = false;
                inMainMenu = true;
                return "Готово! Все города удалены";
            }
        }
        if (isReadyForEditCity) {
            if (user.citiesList().stream().anyMatch(o -> o.getDisplay_name().equalsIgnoreCase(inputTxt.trim()))) {
                if (isReadyToDelete) {
                    user.citiesList().remove(user.citiesList().stream().filter(o -> o.getDisplay_name().equalsIgnoreCase(inputTxt.trim())).findFirst().orElseThrow(
                            NullPointerException::new));
                    inMainMenu = true;
                    isReadyForEditCity = requestedCity = isReadyToDelete = false;
                    return "Удалено!";
                }
                SendSticker sendSticker = new SendSticker();
                sendSticker.setChatId(message.getChatId());
                sendSticker.setSticker("CAACAgIAAxkBAAEBTpxfVkD4E3heo-m0ZGAifgef3hCwlgACeAADQbVWDNm6y3pBU7OPGwQ");
                try {
                    new Launcher().execute(sendSticker);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return "Соре, ты уже добавил город с таким названием, попробуй еще";
            }
            if (inputTxt.equals("⬅Назад")) {
                buttons = new ArrayList<>(cityButs);
                requestedCity = true;
                isReadyForEditCity = false;
                inMainMenu = false;
                return "OK!";
            }
            City choosed = null;
            double lat;
            double lon;
            if (message.hasLocation()) {
                Location location = message.getLocation();
                lat = location.getLatitude();
                lon = location.getLongitude();
            } else {
                List<City> locations = City.resultCitiesArray(inputTxt.trim());
                if (locations.size() == 0) {
                    return WeatherSession.wrongNameCityMessage[new Random().nextInt(4)];
                }
                if (!resultRecieved) {
                    buttons = locations.stream().map(City::getDisplay_name).collect(Collectors.toList());
                    requestedCity = false;
                    resultRecieved = true;
                    return "Выбери то, что тебе нужно";
                }
                choosed = locations.stream().filter(o -> o.getDisplay_name().equals(inputTxt)).findFirst().get();
                lat = choosed.getPosition().getLat();
                lon = choosed.getPosition().getLon();
            }

            Optional<WeatherForecast> wp = Weather.getWeatherNow(lat, lon);
            if (wp.isEmpty()) {
                SendSticker sendSticker = new SendSticker();
                sendSticker.setChatId(message.getChatId());
                sendSticker.setSticker("CAACAgIAAxkBAAEBTpxfVkD4E3heo-m0ZGAifgef3hCwlgACeAADQbVWDNm6y3pBU7OPGwQ");
                try {
                    new Launcher().execute(sendSticker);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return WeatherSession.wrongNameCityMessage[new Random().nextInt(4)];
            } else {
                user.addCity(choosed);
                isReadyForEditCity = requestedCity = resultRecieved = false;
                buttons = new ArrayList<>(defaultButs);
                inMainMenu = true;
                SendSticker sendSticker = new SendSticker();
                sendSticker.setChatId(message.getChatId());
                sendSticker.setSticker("CAACAgIAAxkBAAEBTp5fVkD82Q9Kv8mkfuL-iz8lw4pa4QACzgIAAzigCk7PkcJpCsuWGwQ");
                try {
                    new Launcher().execute(sendSticker);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return "Готово!";
            }
        }
        return "Выбери настройку";
    }

    private void setButtons(SendMessage sendMessage, User user) {
        ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup();
        rkm.setResizeKeyboard(true);
        if (inMainMenu) {
            KeyboardRow main = new KeyboardRow();
            main.addAll(Arrays.asList("Язык", "Настройки города"));
            KeyboardRow secondRow = new KeyboardRow();
            secondRow.addAll(Arrays.asList("Режим бота", "Остальное"));
            KeyboardRow lastRow = new KeyboardRow();
            lastRow.add("|HOME|");
            rkm.setKeyboard(List.of(main, secondRow, lastRow));
        } else if (requestedCity) {
            List<KeyboardRow> result = List.of(buttons.subList(0, 2).stream().collect(KeyboardRow::new, KeyboardRow::add, KeyboardRow::addAll),
                    buttons.subList(2, 4).stream().collect(KeyboardRow::new, KeyboardRow::add, KeyboardRow::addAll));
            rkm.setKeyboard(result);
        } else if(isReadyForEditCity){
            List<KeyboardRow> keyboardRows = buttons.stream().map(o -> {
                KeyboardRow kr = new KeyboardRow();
                kr.add(o);
                return kr;
            }).collect(Collectors.toList());
            rkm.setKeyboard(keyboardRows);
        } else if(toShowMapSettings){
            List<KeyboardRow> keyboardRows = buttons.stream().map(o -> {
                KeyboardRow k = new KeyboardRow();
                k.add(o);
                return k;
            }).collect(Collectors.toList());
            rkm.setKeyboard(keyboardRows);
        }
        sendMessage.setReplyMarkup(rkm);
    }

    @Override
    public BiConsumer<SendMessage, User> getButtonsMarkUp() {
        return buttonsSets;
    }

    @Override
    public void terminateAllProcesses() {
        sessionOpened = false;
    }
}
