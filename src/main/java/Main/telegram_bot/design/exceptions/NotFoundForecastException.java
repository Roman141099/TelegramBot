package Main.telegram_bot.design.exceptions;

public class NotFoundForecastException extends RuntimeException{
    public NotFoundForecastException(String message) {
        super(message);
    }

    public NotFoundForecastException() {
        super("Weather object can not be null");
    }
}
