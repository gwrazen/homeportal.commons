package pl.homeportal.geo.api;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import pl.homeportal.geo.api.model.Address;
import pl.homeportal.geo.api.model.Coordinate;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.List;

/**
 * Created by Grzegorz Wrazen on 23/03/2015.
 */
public class GeoCoordinateFinder {
    private static final String CLIENT_ID = "clientId";
    private final String clientID;

    public GeoCoordinateFinder(String clientID) {
        this.clientID = clientID;
    }

    public Coordinate lookupCoordinate(Address address) throws IOException, InvalidKeyException {

        final Geocoder geocoder = new Geocoder(CLIENT_ID, clientID);
        GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setAddress("Paris, France").setLanguage("en").getGeocoderRequest();
        GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
        List<GeocoderResult> results = geocoderResponse.getResults();

        return new Coordinate(0, 0);
    }
}

