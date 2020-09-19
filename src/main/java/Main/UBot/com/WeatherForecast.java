package Main.UBot.com;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Locale;

public class WeatherForecast {
    private String country, name, description, degrees, icon, mainDescription;
    private double feelsLike, humidity;
    private int windSpeed;
    private long dt, sunset, sunrise;
    private double latitude, longitude;

    public WeatherForecast() {
        //Does nothing
    }

    public WeatherForecast(String country, String name, String description, String degrees, String icon,
                           String mainDescription, double feelsLike, double humidity, int windSpeed, long sunrise,
                           long sunset, long dt, double latitude, double longitude) {
        this.country = country;
        this.name = name;
        this.description = description;
        this.degrees = degrees;
        this.icon = icon;
        this.mainDescription = mainDescription;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.dt = dt;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static HttpURLConnection getPhoto(double latitude, double longitude) {
        String URL_TO_MAP = "http://static-api.maps.sputnik.ru/v1/?";
        String API_KEY = "5032f91e8da6431d8605-f9c0c9a00357";
        String request = URL_TO_MAP + "width=400&height=400&z=10&clng=" + longitude + "&clat=" + latitude + "&apikey=" +
                API_KEY;
        HttpURLConnection httpURLCon = null;
        try {
            httpURLCon = (HttpURLConnection) new URL(request).openConnection();
            httpURLCon.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return httpURLCon;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDegrees(String degrees) {
        this.degrees = degrees;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setMainDescription(String mainDescription) {
        this.mainDescription = mainDescription;
    }

    public void setFeelsLike(double feelsLike) {
        this.feelsLike = feelsLike;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public void setWindSpeed(int windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setDt(long dt) {
        this.dt = dt;
    }

    public void setSunset(long sunset) {
        this.sunset = sunset;
    }

    public void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCountry() {
        return country;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDegrees() {
        return degrees;
    }

    public String getMainDescription() {
        return mainDescription;
    }

    public double getFeelsLike() {
        return feelsLike;
    }

    public double getHumidity() {
        return humidity;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public long getSunrise() {
        return sunrise;
    }

    public long getSunset() {
        return sunset;
    }

    public long getDt() {
        return dt;
    }

    public String getIcon() {
        return icon;
    }

    public String shortDescription() {
        return String.format("Temperature now is %s\nDescription %s", degrees, description);
    }

    @Override
    public String toString() {
        String formattedPars = "Well, now weather states at\n\uD83D\uDDD3 %s\nIn %s (%s) are :\n\uD83C\uDF21Temperature is %s\u2103\nfeels like %3.2f \u2103." +
                "\n☁Description - %s, %s\n\uD83D\uDCA7Humidity ≈ %2.2f%s\n\uD83C\uDF2AWind speed equals %s km/h.\n\uD83C\uDF05Sunrise at %s .\n\uD83C\uDF07Sunset at %s .";
        Date date = new Date(dt * 1000L);
        Locale currentLocale = Locale.forLanguageTag(country);
        String dateTime = String.format(currentLocale, "%1$td/%1$tm %1$tH:%1$tM", date);
        date.setTime(sunrise * 1000L);
        String sunriseTime = String.format(currentLocale, "%1$tH:%1$tM", date);
        date.setTime(sunset * 1000L);
        String sunsetTime = String.format(currentLocale, "%1$tH:%1$tM", date);
        return String.format(formattedPars, dateTime, name, country, degrees + ", ", feelsLike, mainDescription,
                description + ".", humidity, "%.", windSpeed, sunriseTime, sunsetTime);
    }
}
