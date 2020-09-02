package Main.UBot.com;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class Weather {
    private final static String WEATHER_API_KEY = "8cc70e2b9c11a25458c209faa5ff525c";
    protected static String getWeather(String cityInput){
        String URL = "https://api.openweathermap.org/data/2.5/weather?q=" + cityInput + "&units=metric&appid=" + WEATHER_API_KEY;
        HttpURLConnection weatherRequest = null;
        String jsonResponse = "No info";
        try {
            weatherRequest = (HttpURLConnection) new URL(URL).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            assert weatherRequest != null;
            try (BufferedReader bR = new BufferedReader(
                    new InputStreamReader(weatherRequest.getInputStream()))){
                jsonResponse = bR.lines().collect(Collectors.joining());
            }
        } catch (IOException e) {
            return "No city";
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        WeatherParser wP = gson.fromJson(jsonResponse, WeatherParser.class);
        weatherRequest.disconnect();
        return wP.toString();
    }
}
