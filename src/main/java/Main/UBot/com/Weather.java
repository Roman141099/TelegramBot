package Main.UBot.com;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
        String jsonResponse;
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
        JsonObject j_Obj = gson.fromJson(jsonResponse, JsonObject.class);
        System.out.println(jsonResponse);
        WeatherParser wP = new WeatherParser(getCountry(j_Obj), getName(j_Obj),
                getWeather(j_Obj), getDegrees(j_Obj));
        weatherRequest.disconnect();
        return wP.toString();
    }
    private static String getWeather(JsonObject j_Object){
        JsonArray j_Array = j_Object.getAsJsonArray("weather");
        String desc = j_Array.get(0).getAsJsonObject().get("description").getAsString();
        return desc.toUpperCase().charAt(0) + desc.substring(1);
    }
    private static String getName(JsonObject j_Object){
        return j_Object.get("name").getAsString();
    }
    private static String getCountry(JsonObject j_Object){
        JsonObject j_Sys = j_Object.getAsJsonObject("sys");
        return j_Sys.get("country").getAsString();
    }
    private static String getDegrees(JsonObject j_Object){
        JsonObject j_Main = j_Object.getAsJsonObject("main");
        double degrees = j_Main.get("temp").getAsDouble();
        return degrees > 0 ? "plus " + degrees + " C" : "minus " + Math.abs(degrees) + " C";
    }
}
