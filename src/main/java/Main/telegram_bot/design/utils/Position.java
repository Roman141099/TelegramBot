package Main.telegram_bot.design.utils;

import lombok.Data;

@Data
public class Position {
    private double lat;
    private double lon;

    public Position() {
    }

    public Position(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public String toString() {
        return lat + "&" + lon;
    }
}
