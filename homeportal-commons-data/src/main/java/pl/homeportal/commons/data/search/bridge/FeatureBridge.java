package pl.homeportal.commons.data.search.bridge;

import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;
import pl.homeportal.commons.data.model.feature.FeatureConstants;
import pl.homeportal.commons.data.search.encoding.ValueEncoders;

import static pl.homeportal.commons.data.model.feature.FeatureConstants.FEATURE_SEPARATOR;
import static pl.homeportal.commons.text.Constants.BACKSLASH;
import static pl.homeportal.commons.text.Constants.SPACE;

/**
 * Rozbija blob cech |NAZWA:wartosc| na pojedyncze wartosci i koduje kazda przez
 * {@link ValueEncoders#FEATURE} — to samo kodowanie stosuje strona zapytania dla
 * parametrow zadeklarowanych jako cechy.
 */
public class FeatureBridge implements ValueBridge<Object, String>
{
    @Override
    public String toIndexedValue(Object object, ValueBridgeToIndexedValueContext context)
    {
        if (null == object)
        {
            return null;
        }

        final String original = (String) object;
        if (original.trim().isEmpty())
        {
            return null;
        }

        final StringBuilder features = new StringBuilder();
        for (String feature : original.split(getSeparator()))
        {
            final int index = feature.indexOf(FeatureConstants.NAME_SEPARATOR);
            if (index == -1)
            {
                continue;
            }

            final String encoded = ValueEncoders.FEATURE.encode(feature.substring(index + 1));
            if (encoded == null)
            {
                continue;
            }

            features.append(encoded);
            features.append(SPACE);
        }

        final String indexed = features.toString().trim();

        return indexed.isEmpty() ? null : indexed;
    }

    private String getSeparator()
    {
        return BACKSLASH + FEATURE_SEPARATOR;
    }
}
