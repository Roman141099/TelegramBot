package Main.UBot.com;

public class WeatherParser {
    private String country;
    private String name;
    private Coordinates coord;

    public WeatherParser(String country, String name, Coordinates coord) {
        this.country = country;
        this.name = name;
        this.coord = coord;
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
        return "Country : " + country + "\nPlace : " + name + "\n" + coord;
    }
}
