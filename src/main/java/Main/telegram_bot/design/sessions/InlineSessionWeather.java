package Main.telegram_bot.design.sessions;

import Main.telegram_bot.design.exceptions.NotFoundForecastException;
import Main.telegram_bot.design.starter.Launcher;
import Main.telegram_bot.design.weather.BigWeatherForecast;
import Main.telegram_bot.design.weather.Weather;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InlineSessionWeather {

    public static void sendDetails(CallbackQuery callbackQuery) {
        double[] coords = parseCoords(callbackQuery);
        String response = Weather.getWeatherNow(coords[0], coords[1]).orElseThrow(NotFoundForecastException::new).toString();
        send(response, callbackQuery, "/ShortOn");
    }

    public static void sendShort(CallbackQuery callbackQuery) {
        double[] coords = {};
        try {
            coords = parseCoords(callbackQuery);
        }catch (NotFoundForecastException e){
            e.printStackTrace();
        }
        String response = Weather.getWeatherNow(coords[0], coords[1]).orElseThrow(NotFoundForecastException::new).shortDescription();
        send(response, callbackQuery, "/DetailsOn");
    }

    public static void sendDetailsOnBigForecast(CallbackQuery callbackQuery) {
        double[] coords = parseCoords(callbackQuery);
        String response = new BigWeatherForecast(coords[0], coords[1]).create().toString();
        send(response, callbackQuery, "/ShortOnBigForecast");
    }

    public static void sendShortOnBigForecast(CallbackQuery callbackQuery) {
        double[] coords = parseCoords(callbackQuery);
        String response = new BigWeatherForecast(coords[0], coords[1]).create().shortDescription();
        send(response, callbackQuery, "/DetailsOnBigForecast");
    }

    private static double[] parseCoords(CallbackQuery callbackQuery) throws NotFoundForecastException{
        Pattern pattern = Pattern.compile("<(?<lat>-?\\d+\\.?\\d+)&(?<lon>-?\\d+\\.?\\d+)>", Pattern.UNICODE_CHARACTER_CLASS + Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(callbackQuery.getData());
        double[] coords = new double[2];//[0] - lat, [1] - lon
        if (matcher.find()) {
            coords[0] = Double.parseDouble(matcher.group("lat"));
            coords[1] = Double.parseDouble(matcher.group("lon"));
        } else throw new NotFoundForecastException();
        return coords;
    }

    private static void send(String response, CallbackQuery callbackQuery, String beginning) {
        EditMessageText edm = new EditMessageText();
        EditMessageReplyMarkup edmk = new EditMessageReplyMarkup();
        edm.setChatId(callbackQuery.getMessage().getChatId()).
                setMessageId(callbackQuery.getMessage().getMessageId());
        InlineKeyboardMarkup inlineButs = new InlineKeyboardMarkup();
        InlineKeyboardButton iButton = new InlineKeyboardButton(beginning.startsWith("/ShortOn") ? "Коротко" : "Детально").
                setCallbackData(beginning + callbackQuery.getData().substring(callbackQuery.getData().indexOf('<')));
        edmk.setChatId(callbackQuery.getMessage().getChatId());
        edmk.setMessageId(callbackQuery.getMessage().getMessageId());

        inlineButs.setKeyboard(List.of(List.of(iButton)));
        edmk.setReplyMarkup(inlineButs);
        edm.setText(response);
        try {
            new Launcher().execute(edm);
            new Launcher().execute(edmk);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
