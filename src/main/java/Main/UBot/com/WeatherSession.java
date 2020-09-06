package Main.UBot.com;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Optional;
import java.util.Random;

public class WeatherSession extends Session {
    private boolean isChoosedCity;
    transient public static final String[] wrongNameCityMessage = {
            "Unknown city, try again", "Please, verify your city name",
    "That is not correct city name, try again", "There is no city with that name in our data base, try again"
    };
    public WeatherSession() {
        super();
    }

    public WeatherSession(boolean sessionOpened, boolean isChoosedCity) {
        super(sessionOpened);
        this.isChoosedCity = isChoosedCity;
    }

    @Override
    public String nextStep(String inputTxt, Message message) {
        if (!isChoosedCity){
            isChoosedCity = true;
            return "Please provide city";
        }
        Optional<WeatherParser> resultForecast = Weather.getWeatherNow(inputTxt);
        if(resultForecast.isEmpty()) {
            return wrongNameCityMessage[new Random().nextInt(wrongNameCityMessage.length)];
        }
        try {
            parseAndSendSticker(message, resultForecast.orElseThrow(NotFoundForecastException::new));
        } catch (FileNotFoundException | TelegramApiException e) {
            e.printStackTrace();
        }
        terminateAllProcesses();
        return resultForecast.get().toString();
    }

    private void parseAndSendSticker(Message message, WeatherParser resultForecast)
            throws FileNotFoundException, TelegramApiException {
        Gson wG = new Gson();
        JsonObject j_Obj = wG.fromJson(
                new FileReader("src/main/resources/Weather stickers/stickers.json"), JsonObject.class);
        SendSticker sendSticker = new SendSticker();
        sendSticker.setChatId(message.getChatId());
        sendSticker.setSticker(j_Obj.get(resultForecast.getIcon()).
                getAsString());
        new MainBotController().execute(sendSticker);
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
