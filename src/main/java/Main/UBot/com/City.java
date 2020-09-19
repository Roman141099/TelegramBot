package Main.UBot.com;

import com.google.gson.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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

    public static List<City> location(String inputTxt) {
        List<City> coords = new ArrayList<>();
        String[] userResponse = inputTxt.split("(?iU)[\\W\\d]+");
        String query = "http://search.maps.sputnik.ru/search?q=" + (userResponse.length > 1 ?
                String.join("%20", userResponse) : userResponse[0]);
        HttpURLConnection httpURLConnection = null;
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Gson citiesGson = new GsonBuilder().
                    registerTypeAdapter(City.class, cityJsonDeserializer).
                    registerTypeAdapter(Position.class, positionJsonDeserializer).
                    registerTypeAdapter(CitiesVariables.class, citiesVariablesJsonDeserializer).
                    create();
            httpURLConnection = (HttpURLConnection) new URL(query).openConnection();
            String response = new String(httpURLConnection.getInputStream().readAllBytes());
            JsonObject jo = gson.fromJson(response, JsonObject.class);
            CitiesVariables citiesVariables = citiesGson.fromJson(jo, CitiesVariables.class);
            citiesVariables.cities().forEach(System.out::println);
            coords = citiesVariables.cities;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Objects.requireNonNull(httpURLConnection).disconnect();
            return coords;
        }
    }

    public String getDescription() {
        return description;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

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

class Position {
    private double lat;
    private double lon;

    public Position() {
    }

    public Position(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    @Override
    public String toString() {
        return "Position{" +
                "lat=" + lat +
                ", lon=" + lon +
                '}';
    }
}
