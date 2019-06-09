package pl.homeportal.commons.data.search.bridge;

import org.hibernate.search.bridge.StringBridge;


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
            string = string.replaceAll(" ", "");
            string = string.toLowerCase();
            return string;
        }

        return null;
    }
}
