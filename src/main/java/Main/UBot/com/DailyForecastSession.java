package Main.UBot.com;

import com.google.gson.*;
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

public class DailyForecastSession extends Session {
    private boolean isChoosedCity;
    private boolean resultRecieved;
    private final transient BiConsumer<SendMessage, User> buttonsMarkUp = this::setButtons;
    private transient List<String> buttons = new ArrayList<>();

    @Override
    public String nextStep(String inputTxt, Message message, User user) {
        if (!isChoosedCity) {
            isChoosedCity = true;
            return "Укажите город";
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
            city = bigF.getCity();
        } else {
            List<City> locations = City.location(inputTxt.trim());
            if (locations.size() == 0) {
                return WeatherSession.wrongNameCityMessage[new Random().nextInt(4)];
            }
            if(!resultRecieved){
                buttons = locations.stream().map(City::getDisplay_name).collect(Collectors.toList());
                buttons.add("|HOME|");
                resultRecieved = true;
                return "Выбери то, что тебе нужно";
            }
            City choosed = locations.stream().filter(o -> o.getDisplay_name().equals(inputTxt)).findFirst().orElseThrow(NotFoundForecastException::new);
            lat = choosed.getPosition().getLat();
            lon = choosed.getPosition().getLon();
            bigF = new BigWeatherForecast(lat, lon).create();
            city = choosed.getTitle().replaceAll("(?iU)([^[a-z][а-яё]])+", " ");
            System.out.println("-----CITY " + city);
        }
        HttpURLConnection http = null;
        try {
            http = WeatherForecast.getPhoto(lat, lon);
            InputStream ip = http.getInputStream();
            SendPhoto sp = new SendPhoto();
            sp.setPhoto("Map", ip);
            sp.setChatId(message.getChatId());
            new MainBotController().execute(sp);
            ip.close();
        }catch (IOException | TelegramApiException e){
            e.printStackTrace();
        }finally {
            assert http != null;
            http.disconnect();
        }
        terminateAllProcesses();
        user.setCurrentRequestedCity(city);
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
            InlineKeyboardButton iButton = new InlineKeyboardButton("Details").
                    setCallbackData("/DetailsOnBigForecast<" + user.getCurrentRequestedCity() + ">");
            inlineButs.setKeyboard(List.of(List.of(iButton)));
            sendMessage.setReplyMarkup(inlineButs);
        }else{
            ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup();
            rkm.setResizeKeyboard(true);
            List<KeyboardRow> list = buttons.stream().map(o -> {
                KeyboardRow kRow = new KeyboardRow();
                kRow.add(o);
                return kRow;
            }).collect(Collectors.toList());
            rkm.setKeyboard(list);
            sendMessage.setReplyMarkup(rkm);
        }
    }
}
