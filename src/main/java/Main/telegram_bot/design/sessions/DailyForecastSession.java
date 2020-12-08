package Main.telegram_bot.design.sessions;

import Main.telegram_bot.design.exceptions.NotFoundForecastException;
import Main.telegram_bot.design.starter.Launcher;
import Main.telegram_bot.design.utils.City;
import Main.telegram_bot.design.utils.User;
import Main.telegram_bot.design.weather.BigWeatherForecast;
import Main.telegram_bot.design.weather.WeatherForecast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import static Main.telegram_bot.design.utils.City.*;

public class DailyForecastSession extends Session {
    private static final Logger LOG = LoggerFactory.getLogger(DailyForecastSession.class);
    private boolean isChoosedCity;
    private boolean resultRecieved;
    private final transient BiConsumer<SendMessage, User> buttonsMarkUp = this::setButtons;
    private transient List<String> buttons = new ArrayList<>();

    @Override
    public String nextStep(String inputTxt, Message message, User user) {
        if (!isChoosedCity) {
            isChoosedCity = true;
            buttons = user.citiesList().stream().map(City::getDisplay_name).collect(Collectors.toList());
            return "Укажите название города";
        }
        BigWeatherForecast bigF;
        String city;
        double lat;
        double lon;
        if (message.hasLocation()) {
            Location location = message.getLocation();
            lat = location.getLatitude();
            lon = location.getLongitude();
            bigF = new BigWeatherForecast(lat, lon).create();
            City choosed = cityFromCoordinates(lat, lon);
            String[] ar = choosed.getDescription().split(",");
            city = ar.length > 1 ? ar[1].trim() : ar[0].trim();
            user.setCurrentRequestedCity(choosed);
        } else {
            if (user.citiesList().stream().anyMatch(o -> o.getDisplay_name().equalsIgnoreCase(inputTxt.trim()))) {
                City choosed = user.citiesList().stream().filter(o -> o.getDisplay_name().equals(inputTxt.trim())).
                        findFirst().orElseThrow(NotFoundForecastException::new);
                lat = choosed.getPosition().getLat();
                lon = choosed.getPosition().getLon();
                bigF = new BigWeatherForecast(lat, lon).create();
                city = choosed.getTitle();
                user.setCurrentRequestedCity(choosed);
            } else {
                List<City> locations = resultCitiesArray(inputTxt.trim());
                if (locations.size() == 0) {
                    LOG.warn("Not found searched city");
                    return WeatherSession.wrongNameCityMessage[new Random().nextInt(4)];
                }
                if (!resultRecieved) {
                    LOG.info("{} cities found by request", locations.size());
                    buttons = locations.stream().map(City::getDisplay_name).collect(Collectors.toList());
                    buttons.add("|HOME|");
                    resultRecieved = true;
                    return "Выбери тот город, который тебе нужен";
                }
                City choosed = locations.stream().filter(o -> o.getDisplay_name().equals(inputTxt)).findFirst().orElseThrow(NotFoundForecastException::new);
                lat = choosed.getPosition().getLat();
                lon = choosed.getPosition().getLon();
                bigF = new BigWeatherForecast(lat, lon).create();
                user.setCurrentRequestedCity(choosed);
                city = choosed.getTitle();
                user.setCurrentRequestedCity(choosed);
            }
        }
        if (user.isMapShown()) {
            HttpURLConnection http = null;
            try {
                http = WeatherForecast.getPhoto(lat, lon);
                InputStream ip = http.getInputStream();
                SendPhoto sp = new SendPhoto();
                sp.setPhoto("Map", ip);
                sp.setChatId(message.getChatId());
                new Launcher().execute(sp);
                ip.close();
            } catch (IOException | TelegramApiException e) {
                LOG.error("Can't send map of {}", inputTxt, e);
            } finally {
                assert http != null;
                http.disconnect();
            }
        }
        terminateAllProcesses();
        return "Прогноз погоды в городе '" + city + "' :\n" + bigF.shortDescription();
    }

    @Override
    public void terminateAllProcesses() {
        isChoosedCity = sessionOpened = resultRecieved = false;
    }

    @Override
    public BiConsumer<SendMessage, User> getButtonsMarkUp() {
        return buttonsMarkUp;
    }

    private void setButtons(SendMessage sendMessage, User user) {
        if (!sessionOpened && !resultRecieved) {
            InlineKeyboardMarkup inlineButs = new InlineKeyboardMarkup();
            InlineKeyboardButton iButton = new InlineKeyboardButton("Детально").
                    setCallbackData("/DetailsOnBigForecast<" + user.getCurrentRequestedCity().getPosition() + ">");
            inlineButs.setKeyboard(List.of(List.of(iButton)));
            sendMessage.setReplyMarkup(inlineButs);
        } else {
            ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup();
            rkm.setResizeKeyboard(true);
            List<KeyboardRow> list = buttons.stream().map(o -> {
                KeyboardRow kRow = new KeyboardRow();
                kRow.add(o);
                return kRow;
            }).collect(Collectors.toList());
            rkm.setKeyboard(list);
            rkm.setResizeKeyboard(true);
            sendMessage.setReplyMarkup(rkm);
        }
    }
}
