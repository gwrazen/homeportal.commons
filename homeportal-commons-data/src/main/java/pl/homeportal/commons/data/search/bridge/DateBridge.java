package pl.homeportal.commons.data.search.bridge;

import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;
import java.util.Date;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;

public class DateBridge implements ValueBridge<Object, String>
{
    @Override
    public String toIndexedValue(Object object, ValueBridgeToIndexedValueContext context)
    {
        if (object != null)
        {
            String date = DateTools.dateToString((Date) object, Resolution.SECOND);
            return date;
        }

        return null;
    }
}
