package Main.UBot.com;

import com.google.gson.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Weather {
    private static String lang = "en";
    private final static String WEATHER_API_KEY = "8cc70e2b9c11a25458c209faa5ff525c";
    private final static String headURL = "https://api.openweathermap.org/data/2.5/weather?q=";

    private Weather() {
        //doesn't let create object of class and extend him
    }

    protected static Optional<WeatherForecast> getWeatherNow(String cityInput) {
        String URL = headURL + cityInput + "&units=metric&appid=" + WEATHER_API_KEY + "&lang=" + lang;
        HttpURLConnection weatherRequest = null;
        String jsonResponse;
        try {
            weatherRequest = (HttpURLConnection) new URL(URL).openConnection();
            weatherRequest.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            assert weatherRequest != null;
            try (BufferedReader bR = new BufferedReader(
                    new InputStreamReader(weatherRequest.getInputStream()))) {
                jsonResponse = bR.lines().collect(Collectors.joining());
            }
        } catch (IOException e) {
            return Optional.empty();
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(WeatherForecast.class,
                weatherForecastJsonDeserializer).create();
        WeatherForecast wf = gson.fromJson(jsonResponse, WeatherForecast.class);
        weatherRequest.disconnect();
        return Optional.of(wf);
    }

    private static final JsonDeserializer<WeatherForecast> weatherForecastJsonDeserializer = (json, typeOfT, context) -> {
        JsonObject jsonObject = json.getAsJsonObject();
        WeatherForecast wf = new WeatherForecast();
        JsonObject j_arr = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject();
        JsonObject coords = jsonObject.getAsJsonObject("coord");
        JsonObject temps = jsonObject.getAsJsonObject("main");
        JsonObject sys = jsonObject.getAsJsonObject("sys");
        JsonObject wind = jsonObject.getAsJsonObject("wind");

        wf.setMainDescription(j_arr.getAsJsonPrimitive("main").getAsString());
        wf.setDescription(j_arr.getAsJsonPrimitive("description").getAsString());
        wf.setIcon(j_arr.getAsJsonPrimitive("icon").getAsString());

        wf.setLatitude(coords.getAsJsonPrimitive("lat").getAsDouble());
        wf.setLongitude(coords.getAsJsonPrimitive("lon").getAsDouble());

        wf.setDegrees(temps.getAsJsonPrimitive("temp").getAsString());
        wf.setFeelsLike(temps.getAsJsonPrimitive("feels_like").getAsDouble());
        wf.setHumidity(temps.getAsJsonPrimitive("humidity").getAsDouble());

        wf.setCountry(sys.getAsJsonPrimitive("country").getAsString());
        wf.setSunrise(sys.getAsJsonPrimitive("sunrise").getAsLong());
        wf.setSunset(sys.getAsJsonPrimitive("sunset").getAsLong());
        wf.setName(json.getAsJsonObject().getAsJsonPrimitive("name").getAsString());

        wf.setWindSpeed(wind.getAsJsonPrimitive("speed").getAsInt());
        wf.setDt(jsonObject.getAsJsonPrimitive("dt").getAsLong());
        return wf;
    };
}
