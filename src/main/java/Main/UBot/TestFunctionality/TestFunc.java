package Main.UBot.TestFunctionality;

import Main.UBot.com.BigWeatherForecast;
import Main.UBot.com.Day;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.stream.Collectors;

public class TestFunc{
    public static void main(String[] args) throws IOException {
        //1600440507
        record();
        BigWeatherForecast bigWeatherForecast = new BigWeatherForecast(53.2, 50.15).create();
        System.out.println(bigWeatherForecast.shortDescription());
        bigWeatherForecast.getDays().forEach(System.out::println);
    }
    static void record(){
        HttpURLConnection httpURLConnection = null;
        String resp = null;
        try {
            httpURLConnection = (HttpURLConnection) new URL("https://api.openweathermap.org/data/2.5/onecall?lat=53.2&lon=50.15&" +
                    "exclude=current,minutely,hourly" +
                    "&units=metric&appid=8cc70e2b9c11a25458c209faa5ff525c").openConnection();
            resp = new String(httpURLConnection.getInputStream().readAllBytes());
            httpURLConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject j_Obj = gson.fromJson(resp, JsonObject.class);
        FileWriter fw = null;
        try {
            fw = new FileWriter("src/main/java/Main/UBot/TestFunctionality/for7days.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fw.write(gson.toJson(j_Obj));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void serial(){

    }
}
