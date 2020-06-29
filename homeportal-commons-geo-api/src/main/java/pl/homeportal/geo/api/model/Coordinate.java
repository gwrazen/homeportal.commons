package pl.homeportal.geo.api.model;

/**
 * Created by Grzegorz Wrazen on 23/03/2015.
 */
public class Coordinate {

    private final double longitude;
    private final double latitude;

    public Coordinate(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
