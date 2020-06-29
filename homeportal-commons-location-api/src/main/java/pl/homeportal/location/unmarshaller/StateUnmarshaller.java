package pl.homeportal.location.unmarshaller;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.apache.log4j.Logger;
import pl.homeportal.location.model.Col;
import pl.homeportal.location.model.LocationDocument;
import pl.homeportal.location.model.Row;
import pl.homeportal.model.entities.State;

import javax.ejb.Stateless;
import javax.xml.xpath.XPathExpressionException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Grzegorz Wrazen on 06/05/2014.
 */
@Stateless
public class StateUnmarshaller {
    private final static Logger LOG = Logger.getLogger(StateUnmarshaller.class.getSimpleName());

    private static final String XML_FILE = "location/states.xml";

    private int index = 0;

    public List<State> unmarshall() {
        List<State> states = new LinkedList<State>();

        try {
            InputStream iStream = getClass().getClassLoader().getResourceAsStream(XML_FILE);
            XStream xstream = new XStream(new StaxDriver());
            xstream.autodetectAnnotations(true);
            xstream.processAnnotations(LocationDocument.class);
            LocationDocument document = (LocationDocument) xstream.fromXML(iStream);

            List<Row> rows = document.getCatalog().getRows();
            index = 0;
            for (Row row : rows) {
                State state = unmarshallState(row);
                if (state != null) {
                    states.add(state);
                }
            }

        } catch (Exception e) {
            LOG.warn("Problem with unmarshalling XML document state", e);
        }

        return states;
    }

    private State unmarshallState(Row row) throws XPathExpressionException {
        boolean isState = false;
        String name = "";
        String externalID = "";

        for (Col col : row.getCols()) {
            if (col.getName().equals("NAZWA")) {
                name = col.getValue();
            } else if (col.getName().equals("WOJ")) {
                externalID = col.getValue();
            } else if (col.getName().equals("NAZDOD")) {
                if (col.getValue().startsWith("wojew")) {
                    isState = true;
                }
            }

        }

        if (isState) {
            State state = new State();
            state.setName(name.toLowerCase());
            state.setExternalId(externalID);

            LOG.info("Unmarshalled STATE: " + ++index + ", " + state.getName() + ", externalID: " + state.getExternalId());

            return state;
        }


        return null;
    }

}
