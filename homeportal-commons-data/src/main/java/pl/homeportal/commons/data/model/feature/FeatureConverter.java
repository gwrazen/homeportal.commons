package pl.homeportal.commons.data.model.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author gwrazen
 * |name:value^value^value||title:value^value^value|
 */
public class FeatureConverter
{
    public Map<String, Object> convert(String features)
    {
        Map<String, Object> featureMap = new HashMap<String, Object>();

        StringTokenizer tokenizer = new StringTokenizer(features, FeatureConstants.FEATURE_SEPARATOR);

        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            String name = extractName(token);
            String[] values = extractValues(token);
            featureMap.put(name, values[0]);
        }

        return featureMap;
    }

    private String[] extractValues(String fItem)
    {
        String[] values = fItem.substring(fItem.indexOf(FeatureConstants.NAME_SEPARATOR) + 1, fItem.length()).split(FeatureConstants.VALUE_SEPARATOR);
        return values;
    }

    private String extractName(String fItem)
    {
        try
        {
            String name = fItem.substring(0, fItem.indexOf(FeatureConstants.NAME_SEPARATOR));
            return name;
        }
        catch (Exception e)
        {
            return fItem;
        }
    }
}
