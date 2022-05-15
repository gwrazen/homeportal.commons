package pl.homeportal.commons.data.search.bridge;

import org.hibernate.search.bridge.StringBridge;

import static org.apache.commons.lang3.StringUtils.stripAccents;
import static pl.homeportal.commons.text.Constants.EMPTY_STRING;
import static pl.homeportal.commons.text.Constants.REGEXP_SPACE;


public class PropertyTypeBridge implements StringBridge
{
    /**
     * Method removes all spaces and lower casing
     */
    @Override
    public String objectToString(Object object)
    {
        if (null != object)
        {
            String string = (String) object;
            string = string.replaceAll(REGEXP_SPACE, EMPTY_STRING);
            string = stripAccents(string);
            string = string.toLowerCase();
            return string;
        }

        return null;
    }
}
