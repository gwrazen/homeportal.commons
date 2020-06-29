package pl.homeportal.location.unmarshaller;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.apache.log4j.Logger;
import pl.homeportal.location.model.Col;
import pl.homeportal.location.model.LocationDocument;
import pl.homeportal.location.model.Row;
import pl.homeportal.model.entities.City;

import javax.ejb.Stateless;
import javax.xml.xpath.XPathExpressionException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Grzegorz Wrazen on 06/05/2014.
 */
@Stateless
public class CityUnmarshaller {
    private final static Logger LOG = Logger.getLogger(CityUnmarshaller.class.getSimpleName());

    private static final String XML_FILE = "location/states.xml";

    private int index = 0;

    public List<City> unmarshall() {
        List<City> cities = new LinkedList<City>();

        try {
            InputStream iStream = getClass().getClassLoader().getResourceAsStream(XML_FILE);
            XStream xstream = new XStream(new StaxDriver());
            xstream.autodetectAnnotations(true);
            xstream.processAnnotations(LocationDocument.class);
            LocationDocument document = (LocationDocument) xstream.fromXML(iStream);

            List<Row> rows = document.getCatalog().getRows();
            index = 0;
            for (Row row : rows) {
                City city = unmarshallCity(row);
                if (city != null) {
                    cities.add(city);
                }
            }

        } catch (Exception e) {
            LOG.warn("Problem with unmarshalling XML document city", e);
        }

        return cities;
    }

    private City unmarshallCity(Row row) throws XPathExpressionException {

        boolean isCity = false;

        String cityName = "";
        String externalStateID = "";
        String externalId = "";

        for (Col col : row.getCols()) {
            if (col.getName().equals("WOJ")) {
                externalStateID = col.getValue();
            } else if (col.getName().equals("NAZWA")) {
                cityName = col.getValue();
            } else if (col.getName().equals("SYM")) {
                externalId = col.getValue();
            } else if (col.getName().equals("NAZDOD") && col.getValue().indexOf("miasto") != -1) {
                isCity = true;
            }
        }

        if (isCity) {
            City city = new City();
            city.setName(cityName);
//            city.setExternalId(externalId);
            city.setExternalStateId(externalStateID);

            LOG.info("Parsed city " + ++index + ", name: " + city.getName());
            return city;
        }

        return null;
    }
}
