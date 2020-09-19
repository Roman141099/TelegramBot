package Main.UBot.com;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Day {
    private final long dt, sunset, sunrise;
    private final short humidity;
    private final float wind_speed;
    private final Temp temp;
    private final FeelTemp feels_like;
    private final Weather weather;

    public long getDt() {
        return dt;
    }

    public long getSunset() {
        return sunset;
    }

    public long getSunrise() {
        return sunrise;
    }

    public short getHumidity() {
        return humidity;
    }

    public float getWind_speed() {
        return wind_speed;
    }

    public Temp getTemp() {
        return temp;
    }

    public FeelTemp getFeels_like() {
        return feels_like;
    }

    public Weather getWeather() {
        return weather;
    }

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

    public static class Temp {
        private float day, min, max, night, eve, morn;

        public float getDay() {
            return day;
        }

        public float getMin() {
            return min;
        }

        public float getMax() {
            return max;
        }

        public float getNight() {
            return night;
        }

        public float getEve() {
            return eve;
        }

        public float getMorn() {
            return morn;
        }

        public void setDay(float day) {
            this.day = day;
        }

        public void setMin(float min) {
            this.min = min;
        }

        public void setMax(float max) {
            this.max = max;
        }

        public void setNight(float night) {
            this.night = night;
        }

        public void setEve(float eve) {
            this.eve = eve;
        }

        public void setMorn(float morn) {
            this.morn = morn;
        }

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

    public static class FeelTemp {
        private float day, night, eve, morn;

        public float getDay() {
            return day;
        }

        public float getNight() {
            return night;
        }

        public float getEve() {
            return eve;
        }

        public float getMorn() {
            return morn;
        }

        public void setDay(float day) {
            this.day = day;
        }

        public void setNight(float night) {
            this.night = night;
        }

        public void setEve(float eve) {
            this.eve = eve;
        }

        public void setMorn(float morn) {
            this.morn = morn;
        }

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

    public static class Weather {
        private String main, description, icon;

        public String getMain() {
            return main;
        }

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }

        public void setMain(String main) {
            this.main = main;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

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
