package Main.UBot.com;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class SettingsSession extends Session {
    private boolean inMainMenu = true;
    private boolean isRequestedCityEdit;
    private boolean isReadyForEditCity;
    private boolean isReadyToDelete;
    private transient final List<String> defaultButs = List.of("Язык", "Настройки города", "Режим бота", "Остальное", "|HOME|");
    private final transient List<String> cityButs = List.of("Добавить город", "Нет, спасибо", "Удалить город", "Удалить все города");
    private transient List<String> buttons = new ArrayList<>();
    private final transient BiConsumer<SendMessage, User> buttonsSets = this::setButtons;

    @Override
    public String nextStep(String inputTxt, Message message, User user) {
        if (inputTxt.equalsIgnoreCase("настройки города")) {
            isRequestedCityEdit = true;
            inMainMenu = false;
            buttons = new ArrayList<>(cityButs);
            int[] indexes = {1};
            return user.citiesList().size() == 0 ? "Ты не добавил ни одного города\nХочешь сделать это?" : "Добавленные города :\n"
                    + user.citiesList().stream().map(o -> (indexes[0]++) + ".'" + o + "'").
                    collect(Collectors.joining(",\n")) + "\nИзменить это?";
        }
        if (isRequestedCityEdit) {
            if (inputTxt.equalsIgnoreCase("Добавить город")) {
                if (user.citiesList().size() + 1 > 6) {
                    return "Прости⛔\nТы можешь добавлять максимум 6 городов." +
                            " Пожалуйста, удалите один или несколько чтобы добавить новый";
                }
                buttons = new ArrayList<>(user.citiesList());
                isReadyForEditCity = true;
                isRequestedCityEdit = false;
                return (user.citiesList().size() == 0 ? "Список городов пуст" : "Здесь представлены ранее добавленные города")
                        + "\nНапишите название города";
            } else if (inputTxt.equalsIgnoreCase("нет, спасибо")) {
                buttons = new ArrayList<>(defaultButs);
                isReadyForEditCity = isRequestedCityEdit = false;
                inMainMenu = true;
                return "OK! Как хочешь";
            } else if (inputTxt.equals("Удалить город")) {
                if (user.citiesList().size() == 0) {
                    return "У вас пока нет добавленных городов, сперва сделай это";
                }
                buttons = new ArrayList<>(user.citiesList());
                isRequestedCityEdit = false;
                isReadyForEditCity = true;
                isReadyToDelete = true;
                return "Выбери город для удаления";
            } else if (inputTxt.equals("Удалить все города")) {
                if (user.citiesList().size() == 0) {
                    return "Твой список городов пуст\nХочешь добавить город?";
                }
                user.citiesList().clear();
                isReadyForEditCity = isRequestedCityEdit = false;
                inMainMenu = true;
                return "Готово! Все города удалены";
            }
        }
        if (isReadyForEditCity) {
            if (user.citiesList().stream().anyMatch(o -> o.equalsIgnoreCase(inputTxt.trim()))) {
                if (isReadyToDelete) {
                    user.citiesList().remove(inputTxt.trim());
                    inMainMenu = true;
                    isReadyForEditCity = isRequestedCityEdit = isReadyToDelete = false;
                    return "Удалено!";
                }
                SendSticker sendSticker = new SendSticker();
                sendSticker.setChatId(message.getChatId());
                sendSticker.setSticker("CAACAgIAAxkBAAEBTpxfVkD4E3heo-m0ZGAifgef3hCwlgACeAADQbVWDNm6y3pBU7OPGwQ");
                try {
                    new MainBotController().execute(sendSticker);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return "Прости, ты уже добавил город с таким названием, попробуй еще";
            }
            if (inputTxt.equals("Назад◀")) {
                inMainMenu = true;
                isReadyForEditCity = isRequestedCityEdit = false;
                return "OK!";
            }
            Optional<WeatherForecast> wp = Weather.getWeatherNow(inputTxt);
            if (wp.isEmpty()) {
                SendSticker sendSticker = new SendSticker();
                sendSticker.setChatId(message.getChatId());
                sendSticker.setSticker("CAACAgIAAxkBAAEBTpxfVkD4E3heo-m0ZGAifgef3hCwlgACeAADQbVWDNm6y3pBU7OPGwQ");
                try {
                    new MainBotController().execute(sendSticker);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return WeatherSession.wrongNameCityMessage[new Random().nextInt(4)];
            } else {
                user.addCity((inputTxt.toUpperCase().charAt(0) + inputTxt.toLowerCase().substring(1)).trim());
                isReadyForEditCity = isRequestedCityEdit = false;
                buttons = new ArrayList<>(defaultButs);
                inMainMenu = true;
                SendSticker sendSticker = new SendSticker();
                sendSticker.setChatId(message.getChatId());
                sendSticker.setSticker("CAACAgIAAxkBAAEBTp5fVkD82Q9Kv8mkfuL-iz8lw4pa4QACzgIAAzigCk7PkcJpCsuWGwQ");
                try {
                    new MainBotController().execute(sendSticker);
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
        } else {
            int[] indAndCount = {0, 0};
            List<KeyboardRow> keysRow = buttons.stream().collect(Collectors.groupingBy(
                    o -> {
                        if (indAndCount[1] > 1) {
                            indAndCount[1] = 1;
                            return ++indAndCount[0];
                        }
                        indAndCount[1]++;
                        return indAndCount[0];
                    }
            )).values().stream().map(o -> {
                KeyboardRow kR = new KeyboardRow();
                kR.addAll(o);
                return kR;
            }).collect(Collectors.toList());
            if (isReadyForEditCity) {
                KeyboardRow backRow = new KeyboardRow();
                backRow.add("Назад◀");
                keysRow.add(0, backRow);
            }
            rkm.setKeyboard(keysRow);
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
