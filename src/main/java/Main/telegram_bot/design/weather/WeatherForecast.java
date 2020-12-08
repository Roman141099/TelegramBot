package Main.telegram_bot.design.weather;

import lombok.Data;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Locale;

@Data
public class WeatherForecast {
    private String country, cityName, description, degrees, icon, mainDescription;
    private double feelsLike, humidity;
    private int windSpeed;
    private long dt, sunset, sunrise;
    private double latitude, longitude;

    public WeatherForecast() {
        //Does nothing
    }

    public WeatherForecast(String country, String cityName, String description, String degrees, String icon,
                           String mainDescription, double feelsLike, double humidity, int windSpeed, long sunrise,
                           long sunset, long dt, double latitude, double longitude) {
        this.country = country;
        this.cityName = cityName;
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

    public WeatherForecast setCityName(String cityName) {
        this.cityName = cityName;
        return this;
    }

    public String shortDescription() {
        return String.format("Температура воздуха в городе %s равна %s\n%s", cityName,  degrees, description);
    }

    @Override
    public String toString() {
        String formattedPars = """
                Итак, погода сейчас, точное время :
                \uD83D\uDDD3 %s
                В городе %s (%s) :
                \uD83C\uDF21Температура сейчас %s\u2103,
                ощущается как %3.2f \u2103.
                ☁Описание - %s
                \uD83D\uDCA7Влажность воздуха ≈ %2.2f%s
                \uD83C\uDF2AСкорость ветра равна %d км/ч.
                \uD83C\uDF05Рассвет в %s .
                \uD83C\uDF07Заход в %s .""";
        Date date = new Date(dt * 1000L);
        Locale currentLocale = Locale.forLanguageTag(country);
        String dateTime = String.format(currentLocale, "%1$td/%1$tm %1$tH:%1$tM", date);
        date.setTime(sunrise * 1000L);
        String sunriseTime = String.format(currentLocale, "%1$tH:%1$tM", date);
        date.setTime(sunset * 1000L);
        String sunsetTime = String.format(currentLocale, "%1$tH:%1$tM", date);
        return String.format(formattedPars, dateTime, cityName, country, degrees, feelsLike,
                description.substring(0, 1).toUpperCase() + description.substring(1) + ".", humidity, "%.", windSpeed, sunriseTime, sunsetTime);
    }
}
