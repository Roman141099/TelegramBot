package Main.UBot.com;

import java.util.Random;

public class WeatherSession extends Session {
    private boolean isChoosedCity;
    transient private final String[] wrongNameCityMessage = {
            "Unknown city, try again", "Please, verify your city name",
    "That is not correct city name, try again", "There is no city with that name in our data base, try again"
    };
    public WeatherSession() {
        super();
    }

    public WeatherSession(boolean sessionOpened, boolean isChoosedCity) {
        super(sessionOpened);
        this.isChoosedCity = isChoosedCity;
    }

    public boolean isChoosedCity() {
        return isChoosedCity;
    }

    public void setChoosedCity(boolean choosedCity) {
        isChoosedCity = choosedCity;
    }

    public String nextStep(String inputTxt) {
        if (!isChoosedCity){
            isChoosedCity = true;
            return "Please provide city";
        }
        String resultForecast = Weather.getWeather(inputTxt);
        if(resultForecast.equals("No city")){
            return wrongNameCityMessage[new Random().nextInt(wrongNameCityMessage.length)];
        }
        setSessionOpened(false);
        setChoosedCity(false);
        return resultForecast;
    }

    @Override
    public String toString() {
        return "WeatherSession{" +
                "isChoosedCity=" + isChoosedCity +
                ", sessionOpened=" + sessionOpened +
                '}';
    }
}
