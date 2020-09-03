package Main.UBot.com;

public class WeatherParser {
    private final String country;
    private final String name;
    private final String description;
    private final String degrees;

    public WeatherParser(String country, String name, String description, String degrees) {
        this.country = country;
        this.name = name;
        this.description = description;
        this.degrees = degrees;
    }

    static class Coordinates{
        double lon;
        double lat;

        public Coordinates(int lon, int lat) {
            this.lon = lon;
            this.lat = lat;
        }

        @Override
        public String toString() {
            return "Coordinates : " +
                    "lotitude : " + lon +
                    ", latitude : " + lat;
        }
    }

    @Override
    public String toString() {
        return String.format("Now weather state :\n%s\n%s\n%s\n%s", description, degrees, name, country);
    }
}
