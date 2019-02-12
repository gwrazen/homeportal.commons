package pl.homeportal.commons.data.search.bridge;

import java.util.Date;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.hibernate.search.bridge.builtin.StringBridge;

public class DateBridge extends StringBridge
{
    @Override
    public String objectToString(Object object)
    {
        if ( object != null )
        {
            String date = DateTools.dateToString((Date) object, Resolution.SECOND);
            return date;
        }

        return null;
    }
}
