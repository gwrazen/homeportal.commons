package pl.homeportal.location.importer;

import org.apache.log4j.Logger;
import pl.homeportal.location.unmarshaller.CityUnmarshaller;
import pl.homeportal.location.unmarshaller.StateUnmarshaller;
import pl.homeportal.location.unmarshaller.StreetUnmarshaller;
import pl.homeportal.model.entities.City;
import pl.homeportal.model.entities.State;
import pl.homeportal.model.entities.Street;
import pl.homeportal.service.LocationService;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Grzegorz Wrazen on 06/05/2014.
 */
@Stateless
public class LocationImporter {
    private final static Logger LOG = Logger.getLogger(LocationImporter.class.getSimpleName());

    @Autowired
    private LocationService locationService;

    @Autowired
    private StateUnmarshaller stateUnmarshaller;

    @Autowired
    private CityUnmarshaller cityUnmarshaller;

    @Autowired
    private StreetUnmarshaller streetUnmarshaller;

    public void importAll() {
        Map<String, State> stateMap = new HashMap<String, State>();
        Map<String, City> cityMap = new HashMap<String, City>();

        locationService.clean();

        List<State> states = stateUnmarshaller.unmarshall();
        for (State state : states) {
            stateMap.put(state.getExternalId(), locationService.addState(state));
        }

        List<City> cities = cityUnmarshaller.unmarshall();
        for (City city : cities) {
            city.setState(stateMap.get(city.getExternalStateId()));
            cityMap.put(city.getExternalId(), locationService.addCity(city));
        }

        List<Street> streets = streetUnmarshaller.unmarshall();
        for (Street street : streets) {
            street.setCity(cityMap.get(street.getExternalCityId()));
            locationService.createStreet(street);
        }

    }

    public void importStates() {
        locationService.cleanStates();

        List<State> states = stateUnmarshaller.unmarshall();
        for (State state : states) {
            locationService.addState(state);
        }

    }

    public void importCities() {
        locationService.cleanCities();

        List<City> cities = cityUnmarshaller.unmarshall();
        for (City city : cities) {
            city.setState(null);
            locationService.addCity(city);
        }

    }

    public void importStreets() {
        locationService.cleanStreets();

        List<Street> streets = streetUnmarshaller.unmarshall();
        for (Street street : streets) {
            locationService.createStreet(street);
        }

    }

}
