package pl.homeportal.location.unmarshaller;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.apache.log4j.Logger;
import pl.homeportal.location.model.Col;
import pl.homeportal.location.model.LocationDocument;
import pl.homeportal.location.model.Row;
import pl.homeportal.model.entities.Street;

import javax.ejb.Stateless;
import javax.xml.xpath.XPathExpressionException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Grzegorz Wrazen on 06/05/2014.
 */
@Stateless
public class StreetUnmarshaller {
    private static final Logger LOG = Logger.getLogger(StateUnmarshaller.class.getSimpleName());

    private static final String XML_FILE = "location/streets.xml";

    private int index = 0;

    public List<Street> unmarshall() {
        List<Street> streets = new LinkedList<Street>();

        try {
            InputStream iStream = getClass().getClassLoader().getResourceAsStream(XML_FILE);
            XStream xstream = new XStream(new StaxDriver());
            xstream.autodetectAnnotations(true);
            xstream.processAnnotations(LocationDocument.class);
            LocationDocument document = (LocationDocument) xstream.fromXML(iStream);

            List<Row> rows = document.getCatalog().getRows();
            index = 0;
            for (Row row : rows) {
                Street street = unmarshallStreet(row);
                if (street != null) {
                    streets.add(street);
                }
            }

        } catch (Exception e) {
            LOG.warn("Problem with unmarshalling XML document street", e);
        }

        return streets;

    }

    private Street unmarshallStreet(Row row) throws XPathExpressionException {
        Street street = new Street();

        String nameFirst = "";
        String nameSecond = "";

        List<Col> cols = row.getCols();
        for (Col col : cols) {
            if (col.getName().equals("WOJ")) {
                street.setExternalStateId(col.getValue());
            } else if (col.getName().equals("SYM")) {
                street.setExternalCityId(col.getValue());
            } else if (col.getName().equals("NAZWA_1")) {
                nameFirst = col.getValue();
            } else if (col.getName().equals("NAZWA_2")) {
                nameSecond = col.getValue();
            }
        }

        street.setName((nameSecond + nameFirst).trim());

        LOG.info("Parsed street " + ++index + ", name: " + street.getName());

        return street;
    }
}
