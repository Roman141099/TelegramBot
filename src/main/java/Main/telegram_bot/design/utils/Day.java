package Main.telegram_bot.design.utils;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Data
public class Day {
    private final long dt, sunset, sunrise;
    private final short humidity;
    private final float wind_speed;
    private final Temp temp;
    private final FeelTemp feels_like;
    private final Weather weather;

    public Day(long dt, long sunset, long sunrise, short humidity,
               float wind_speed, Temp temp, FeelTemp feels_like, Weather weather) {
        this.dt = dt;
        this.sunset = sunset;
        this.sunrise = sunrise;
        this.humidity = humidity;
        this.wind_speed = wind_speed;
        this.temp = temp;
        this.feels_like = feels_like;
        this.weather = weather;
    }
    @Data
    public static class Temp {
        private float day, min, max, night, eve, morn;

        @Override
        public String toString() {
            return "Temp{" +
                    "day=" + day +
                    ", min=" + min +
                    ", max=" + max +
                    ", night=" + night +
                    ", eve=" + eve +
                    ", morn=" + morn +
                    '}';
        }
    }

    @Data
    public static class FeelTemp {
        private float day, night, eve, morn;

        @Override
        public String toString() {
            return "FeelTemp{" +
                    "day=" + day +
                    ", night=" + night +
                    ", eve=" + eve +
                    ", morn=" + morn +
                    '}';
        }
    }

    @Data
    public static class Weather {
        private String main, description, icon;

        @Override
        public String toString() {
            return "Weather{" +
                    "main='" + main + '\'' +
                    ", description='" + description + '\'' +
                    ", icon='" + icon + '\'' +
                    '}';
        }
    }

    public LocalDateTime getDate(ZoneId zoneId) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(dt), zoneId);
    }

    @Override
    public String toString() {
        return "Day{\n" +
                "dt=" + new Date(dt * 1000L) +
                "\n, sunset=" + sunset +
                "\n, sunrise=" + sunrise +
                "\n, humidity=" + humidity +
                "\n, wind_speed=" + wind_speed +
                "\n, temp=" + temp +
                "\n, feels_like=" + feels_like +
                "\n, weather=" + weather +
                '}';
    }
}
