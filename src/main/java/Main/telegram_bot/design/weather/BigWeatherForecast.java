package Main.telegram_bot.design.weather;

import Main.telegram_bot.design.utils.Day;
import com.google.gson.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class BigWeatherForecast {
    private final double lat;
    private final double lon;
    private String timezone;
    private long timezone_offset;
    private transient List<Day> days;
    private transient String city;
    private transient final Map<String, String> emoji1 = Map.of(
            "01d", "☀",
            "02d", "⛅",
            "03d", "☁",
            "04d", "☁",
            "09d", "\uD83C\uDF27",
            "10d", "\uD83C\uDF26",
            "11d", "\uD83C\uDF29",
            "13d", "❄",
            "50d", "\uD83C\uDF2B");
    private transient Map<String, String> emoji2 = Map.of(
            "01n", "\uD83C\uDF1A",
            "02n", "☁",
            "03n", "☁",
            "04n", "☁",
            "09n", "\uD83C\uDF27",
            "10n", "\uD83C\uDF27",
            "11n", "⛈",
            "13n", "❄",
            "50n", "\uD83C\uDF2B");
    private transient Map<String, String> emojis;

    public List<Day> getDays() {
        return days;
    }

    public String getCity() {
        return city;
    }

    private final transient JsonDeserializer<Day.Weather> weatherDeserializer = (json, typeOfT, context) -> {
        Day.Weather weather = new Day.Weather();
        JsonObject j_object = json.getAsJsonArray().get(0).getAsJsonObject();
        weather.setMain(j_object.getAsJsonPrimitive("main").getAsString());
        weather.setDescription(j_object.getAsJsonPrimitive("description").getAsString());
        weather.setIcon(j_object.getAsJsonPrimitive("icon").getAsString());
        return weather;
    };
    private final transient JsonDeserializer<BigWeatherForecast> mainDeserializer = ((json, typeOfT, context) -> {
        JsonArray j_array = json.getAsJsonObject().getAsJsonArray("daily");
        List<Day> daysList = new ArrayList<>(7);
        for (JsonElement j :
                j_array) {
            daysList.add(context.deserialize(j, Day.class));
        }
        setDays(daysList);
        timezone = json.getAsJsonObject().getAsJsonPrimitive("timezone").getAsString();
        timezone_offset = json.getAsJsonObject().getAsJsonPrimitive("timezone_offset").getAsLong();
        city = timezone.split("/")[1];
        return this;
    });

    public void setDays(List<Day> days) {
        this.days = days;
    }

    public BigWeatherForecast(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        emojis = new HashMap<>(emoji1);
        emojis.putAll(emoji2);
    }

    public BigWeatherForecast create() {
        Gson gsonReturner = new GsonBuilder().
                registerTypeAdapter(BigWeatherForecast.class, mainDeserializer).
                registerTypeAdapter(Day.Weather.class, weatherDeserializer).
                create();
        String resp = "";
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(String.format("%slat=%f&lon=%f%s", "https://api.openweathermap.org/data/2.5/onecall?",
                    lat, lon, "&exclude=current,minutely,hourly&units=metric&appid=8cc70e2b9c11a25458c209faa5ff525c&lang=ru")).openConnection();
            resp = new String(httpURLConnection.getInputStream().readAllBytes());
            httpURLConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gsonReturner.fromJson(resp, BigWeatherForecast.class);
    }

    public String shortDescription() {
        return days.stream().map(o -> String.format("%s %d %s, %s\n\t· День : %2.2f \u2103\n\t· %s",
                emojis.get(o.getWeather().getIcon()),
                o.getDate(ZoneId.of(timezone)).getDayOfMonth(),
                o.getDate(ZoneId.of(timezone)).getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ru")),
                o.getDate(ZoneId.of(timezone)).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ru")),
                o.getTemp().getDay(), o.getWeather().getDescription().substring(0, 1).toUpperCase() +
                        o.getWeather().getDescription().substring(1))).collect(Collectors.joining("\n"));
    }

    @Override
    public String toString() {
        return days.stream().map(o -> String.format("%s %d %s, %s\n\t· День : %2.2f \u2103\n\t· Ночь : %2.2f \u2103\n\t· Ощущается как : %2.2f \u2103\n\t· %s",
                emojis.get(o.getWeather().getIcon()),
                o.getDate(ZoneId.of(timezone)).getDayOfMonth(),
                o.getDate(ZoneId.of(timezone)).getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ru")),
                o.getDate(ZoneId.of(timezone)).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ru")),
                o.getTemp().getDay(), o.getTemp().getNight(), o.getFeels_like().getDay(),
                o.getWeather().getDescription())).collect(Collectors.joining("\n"));
    }
}
