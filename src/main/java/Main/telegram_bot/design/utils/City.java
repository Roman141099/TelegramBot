package Main.telegram_bot.design.utils;

import com.google.gson.*;
import lombok.Data;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
@Data
public class City {
    private Position position;
    private String title;
    private String description;
    private String display_name;


    static final transient JsonDeserializer<Position> positionJsonDeserializer = (json, typeOfT, context) -> {
        Position position = new Position();
        JsonElement j = json.getAsJsonObject().get("position");
        position.setLat(j.getAsJsonObject().getAsJsonPrimitive("lat").getAsDouble());
        position.setLon(j.getAsJsonObject().getAsJsonPrimitive("lon").getAsDouble());
        return position;
    };

    static final transient JsonDeserializer<City> cityJsonDeserializer = (json, typeOfT, context) -> {
        JsonObject cityObj = json.getAsJsonObject();
        City city = new City();
        city.setPosition(context.deserialize(cityObj, Position.class));
        city.setTitle(cityObj.getAsJsonObject().get("title").getAsString());
        city.setDescription(cityObj.getAsJsonPrimitive("description").getAsString());
        city.setDisplay_name(cityObj.getAsJsonPrimitive("display_name").getAsString());
        return city;
    };

    static final transient JsonDeserializer<CitiesVariables> citiesVariablesJsonDeserializer = (json, typeOfT, context) -> {
        CitiesVariables citiesVariables = new CitiesVariables();
        JsonArray jsonElements = json.getAsJsonObject().getAsJsonArray("result");
        for (JsonElement j:
                jsonElements) {
            citiesVariables.addCity(context.deserialize(j, City.class));
        }
        return citiesVariables;
    };

    public City() {
    }

    public City(Position position, String title, String description, String display_name) {
        this.position = position;
        this.title = title;
        this.description = description;
        this.display_name = display_name;
    }
    @Data
    static class CitiesVariables{
        final List<City> cities;

        public CitiesVariables() {
            cities = new ArrayList<>();
        }

        void addCity(City city){
            cities.add(city);
        }

        public Stream<City> cities(){
            return cities.stream();
        }
    }

    public static List<City> resultCitiesArray(String inputTxt) {
        List<City> coords = new ArrayList<>();
        String[] userResponse = inputTxt.split("(?iU)[\\W\\d]+");
        String query = "http://search.maps.sputnik.ru/search?q=" + (userResponse.length > 1 ?
                String.join("%20", userResponse) : userResponse[0]);
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Gson citiesGson = new GsonBuilder().
                    registerTypeAdapter(City.class, cityJsonDeserializer).
                    registerTypeAdapter(Position.class, positionJsonDeserializer).
                    registerTypeAdapter(CitiesVariables.class, citiesVariablesJsonDeserializer).
                    create();
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(query).openConnection();
            String response = new String(httpURLConnection.getInputStream().readAllBytes());
            JsonObject jo = gson.fromJson(response, JsonObject.class);
            CitiesVariables citiesVariables = citiesGson.fromJson(jo, CitiesVariables.class);
            citiesVariables.cities().forEach(System.out::println);
            coords = citiesVariables.cities;
            httpURLConnection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return coords;
    }

    public static City cityFromCoordinates(double lat, double lon){
        String query = String.format("%s?lat=%s&lon=%s", "http://whatsthere.maps.sputnik.ru/point",
                String.valueOf(lat).replaceFirst(",", "."),
                String.valueOf(lon).replaceFirst(",", "."));
        City city = new City();
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Gson citiesGson = new GsonBuilder().
                    registerTypeAdapter(City.class, cityJsonDeserializerFromCoordinates).create();
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(query).openConnection();
            String response = new String(httpURLConnection.getInputStream().readAllBytes());
            JsonObject jo = gson.fromJson(response, JsonObject.class);
            city = citiesGson.fromJson(jo, City.class);
            city.setPosition(new Position(lat, lon));
            httpURLConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return city;
    }



    public static JsonDeserializer<City> cityJsonDeserializerFromCoordinates = (json, typeOfT, context) -> {
        City city = new City();
        JsonObject props = json.getAsJsonObject().get("result").getAsJsonObject().getAsJsonArray("address").get(0).getAsJsonObject().
                getAsJsonArray("features").get(0).getAsJsonObject().get("properties").getAsJsonObject();
        city.setDescription(props.getAsJsonPrimitive("description").getAsString());
        city.setDisplay_name(props.getAsJsonPrimitive("display_name").getAsString());
        city.setTitle(props.getAsJsonPrimitive("title").getAsString());
        return city;
    };

    @Override
    public String toString() {
        return "City{" +
                "position=" + position +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", display_name='" + display_name + '\'' +
                '}';
    }
}