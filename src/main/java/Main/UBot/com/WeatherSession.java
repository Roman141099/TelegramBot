package Main.UBot.com;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class WeatherSession extends Session {
    private boolean isChoosedCity;
    transient public static final String[] wrongNameCityMessage = {
            "Неизвестный город, попробуй еще", "Пожалуйста, проверь название города",
    "Это неправильное название города, попробуй еще", "В моей базе данных нет города с таким названием, давай еще раз"
    };
    private final transient List<String> buttons = new ArrayList<>(Collections.singletonList("|HOME|"));
    private final transient BiConsumer<SendMessage, User> buttonsMarkUp = this::setButtons;
    public WeatherSession() {
        super();
    }
    public WeatherSession(boolean sessionOpened, boolean isChoosedCity) {
        super(sessionOpened);
        this.isChoosedCity = isChoosedCity;
    }

    @Override
    public String nextStep(String inputTxt, Message message, User user) {
        if (!isChoosedCity){
            isChoosedCity = true;
            return "Напиши название города";
        }
//        if(message.hasLocation()){
//            Location location = message.getLocation();
//
//        }
        String[] city = inputTxt.split("(?iU)[\\W\\d]+");
        inputTxt = (city.length > 1 ? String.join(" ", city) : city[0]);
        Optional<WeatherForecast> resultForecast = Weather.getWeatherNow(inputTxt);
        if(resultForecast.isEmpty()) {
            return wrongNameCityMessage[new Random().nextInt(wrongNameCityMessage.length)];
        }
        try {
            parseAndSendSticker(message, resultForecast.orElseThrow(NotFoundForecastException::new));
        } catch (FileNotFoundException | TelegramApiException e) {
            e.printStackTrace();
        }
        user.setCurrentRequestedCity(inputTxt);
        terminateAllProcesses();
        return resultForecast.get().shortDescription();
    }
    @Override
    public BiConsumer<SendMessage, User> getButtonsMarkUp() {
        return buttonsMarkUp;
    }

    private void parseAndSendSticker(Message message, WeatherForecast resultForecast)
            throws FileNotFoundException, TelegramApiException {
        Gson wG = new Gson();
        JsonObject j_Obj = wG.fromJson(
                new FileReader("src/main/resources/Weather stickers/stickers.json"), JsonObject.class);
        SendSticker sendSticker = new SendSticker();
        sendSticker.setChatId(message.getChatId());
        sendSticker.setSticker(j_Obj.get(resultForecast.getIcon()).
                getAsString());
        new MainBotController().execute(sendSticker);
        HttpURLConnection http = null;
        try {
            http = WeatherForecast.getPhoto(resultForecast.getLatitude(), resultForecast.getLongitude());
            InputStream ip = http.getInputStream();
            SendPhoto sp = new SendPhoto();
            sp.setPhoto("Map", ip);
            sp.setChatId(message.getChatId());
            new MainBotController().execute(sp);
            ip.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        http.disconnect();
    }

    private void setButtons(SendMessage sendMessage, User user){
        if(!sessionOpened) {
            InlineKeyboardMarkup inlineButs = new InlineKeyboardMarkup();
            InlineKeyboardButton iButton = new InlineKeyboardButton("Details").
                    setCallbackData("/DetailsOn<" + user.getCurrentRequestedCity() + ">");
            inlineButs.setKeyboard(List.of(List.of(iButton)));
            sendMessage.setReplyMarkup(inlineButs);
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
