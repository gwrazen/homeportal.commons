package pl.homeportal.commons.data.search.bridge;

import org.hibernate.search.bridge.StringBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 * @author gwrazen
 */
public class NumericBridge implements StringBridge
{
    private static final Logger LOG = LoggerFactory.getLogger(NumericBridge.class.getSimpleName());

    private static final int MAX_LENGTH = 10;
    private static final int ZERO = 0;
    /**
     * <pre>Method index Long value instead of Double</pre>
     * @param o <code>Double</code>
     * @return <code>String</code>
     */
    public String objectToString(Object o)
    {
        if ( null != o )
        {
            try
            {
                Number number = (Number)o;
                String value = String.valueOf(number.intValue());
                if(value.length() < MAX_LENGTH)
                {
                    value = pad(value);
                }

                return value;
            }
            catch (Exception e)
            {
                LOG.warn("Unable to convert from Number to String: " + o);
            }
        }
        return null;
    }

    private String pad(String value)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < MAX_LENGTH - value.length() ; ++i )
        {
            builder.append(ZERO);
        }
        builder.append(value);

        return builder.toString();
    }

    public Long stringToObject(String value)
    {
        if ( null != value )
        {
            try
            {
                return Long.parseLong(value);
            }
            catch (Exception e)
            {
                LOG.warn("Unable to convert from String to Number: " + value);
            }
        }

        return null;
    }
}
