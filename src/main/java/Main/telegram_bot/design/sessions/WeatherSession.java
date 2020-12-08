package Main.telegram_bot.design.sessions;
import Main.telegram_bot.design.exceptions.NotFoundForecastException;
import Main.telegram_bot.design.starter.Launcher;
import Main.telegram_bot.design.utils.City;
import Main.telegram_bot.design.utils.User;
import Main.telegram_bot.design.weather.Weather;
import Main.telegram_bot.design.weather.WeatherForecast;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import static Main.telegram_bot.design.utils.City.*;

public class WeatherSession extends Session {
    private static final Logger logger = LoggerFactory.getLogger(WeatherSession.class);
    private boolean resultRecieved;
    private boolean isChoosedCity;
    transient public static final String[] wrongNameCityMessage = {
            "Неизвестный город, попробуй еще", "Пожалуйста, проверь название города",
    "Это неправильное название города, попробуй еще", "В моей базе данных нет города с таким названием, давай еще раз"
    };
    private transient List<String> buttons = new ArrayList<>(Collections.singletonList("|HOME|"));
    private final transient BiConsumer<SendMessage, User> buttonsMarkUp = this::setButtons;
    public WeatherSession() {
        super();
    }
    public WeatherSession(boolean sessionOpened, boolean isChoosedCity) {
        super(sessionOpened);
        this.isChoosedCity = isChoosedCity;
    }

    @Override
    public String nextStep(String inputTxt, Message message, User user){
        if (!isChoosedCity){
            isChoosedCity = true;
            buttons = user.citiesList().stream().map(City::getDisplay_name).collect(Collectors.toList());
            return "Напиши или выбери название города";
        }
        double lat;
        double lon;
        City choosed;
        if (message.hasLocation()) {
            Location location = message.getLocation();
            lat = location.getLatitude();
            lon = location.getLongitude();
            choosed = cityFromCoordinates(lat, lon);
        } else {
            if(user.citiesList().stream().anyMatch(o -> o.getDisplay_name().equalsIgnoreCase(inputTxt.trim()))){
                choosed = user.citiesList().stream().filter(o -> o.getDisplay_name().equals(inputTxt.trim())).
                        findFirst().orElseThrow(NotFoundForecastException::new);
            }else {
                List<City> locations = resultCitiesArray(inputTxt.trim());
                if (locations.size() == 0) {
                    return WeatherSession.wrongNameCityMessage[new Random().nextInt(4)];
                }
            if(!resultRecieved){
                buttons = locations.stream().map(City::getDisplay_name).collect(Collectors.toList());
                buttons.add("|HOME|");
                resultRecieved = true;
                return "Выбери то, что тебе нужно";
            }
            choosed = locations.stream().filter(o -> o.getDisplay_name().equals(inputTxt)).findFirst().
                    orElseThrow(NotFoundForecastException::new);
            }
            lat = choosed.getPosition().getLat();
            lon = choosed.getPosition().getLon();
        }
        Optional<WeatherForecast> resultForecast = Weather.getWeatherNow(lat, lon);
        if(resultForecast.isEmpty()) {
            buttons = user.citiesList().stream().map(City::getDisplay_name).collect(Collectors.toList());
            buttons.add(0, "|HOME|" );
            return wrongNameCityMessage[new Random().nextInt(wrongNameCityMessage.length)];
        }
        try {
            parseAndSendSticker(message, resultForecast.orElseThrow(NotFoundForecastException::new), user.isMapShown());
        } catch (FileNotFoundException | TelegramApiException e) {
            logger.error("Can't send map of city '{}'", user.getCurrentRequestedCity().getDisplay_name(), e);
        }
        user.setCurrentRequestedCity(choosed);
        terminateAllProcesses();
        return resultForecast.get().shortDescription();
    }
    @Override
    public BiConsumer<SendMessage, User> getButtonsMarkUp() {
        return buttonsMarkUp;
    }

    private void parseAndSendSticker(Message message, WeatherForecast resultForecast, boolean showMap)
            throws FileNotFoundException, TelegramApiException {
        Gson wG = new Gson();
        JsonObject j_Obj = wG.fromJson(
                new FileReader("src/main/resources/Weather stickers/stickers.json"), JsonObject.class);
        SendSticker sendSticker = new SendSticker();
        sendSticker.setChatId(message.getChatId());
        sendSticker.setSticker(j_Obj.get(resultForecast.getIcon()).
                getAsString());
        new Launcher().execute(sendSticker);
        if(showMap) {
            HttpURLConnection http = null;
            try {
                http = WeatherForecast.getPhoto(resultForecast.getLatitude(), resultForecast.getLongitude());
                InputStream ip = http.getInputStream();
                SendPhoto sp = new SendPhoto();
                sp.setPhoto("Map", ip);
                sp.setChatId(message.getChatId());
                new Launcher().execute(sp);
                ip.close();
            } catch (IOException e) {
                logger.error("Can't send map of {}", resultForecast.getCityName(), e);
            }
            http.disconnect();
        }
    }

    private void setButtons(SendMessage sendMessage, User user){
        if(!sessionOpened) {
            try {
                InlineKeyboardMarkup inlineButs = new InlineKeyboardMarkup();
                InlineKeyboardButton iButton = new InlineKeyboardButton("Детально").
                        setCallbackData("/DetailsOn<" + user.getCurrentRequestedCity().getPosition() + ">");
                inlineButs.setKeyboard(List.of(List.of(iButton)));
                sendMessage.setReplyMarkup(inlineButs);
            }catch (Exception e){
                logger.error("Can't set inline keyboard", e);
            }
        } else {
            ReplyKeyboardMarkup rpl = new ReplyKeyboardMarkup();
            List<KeyboardRow> keysRows = buttons.stream().map(o -> {
                KeyboardRow kr = new KeyboardRow();
                kr.add(o);
                return kr;
            }).collect(Collectors.toList());
            rpl.setResizeKeyboard(true);
            rpl.setKeyboard(keysRows);
            sendMessage.setReplyMarkup(rpl);
        }
    }

    @Override
    public void terminateAllProcesses() {
        sessionOpened = isChoosedCity = false;
    }

    @Override
    public String toString() {
        return "WeatherSession{" +
                "isChoosedCity=<" + isChoosedCity +
                ">, sessionOpened=<" + sessionOpened +
                ">}";
    }
}
