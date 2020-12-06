package pl.homeportal.commons.data.model.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static pl.homeportal.commons.data.model.feature.FeatureConstants.FEATURE_SEPARATOR;
import static pl.homeportal.commons.data.model.feature.FeatureConstants.NAME_SEPARATOR;
import static pl.homeportal.commons.data.model.feature.FeatureConstants.VALUE_SEPARATOR;

/**
 * |name:value^value^value||title:value^value^value|
 * <p>
 * Create by Grzegorz Wrażeń
 */
public class FeatureConverter
{
    /**
     * Converting String of features to Map of features e.g.
     * |name:value^value^value||title:value^value^value|
     */
    public static Map<String, String> toFeatureMap(String features)
    {
        Map<String, String> featureMap = new HashMap<>();
        StringTokenizer tokenizer = new StringTokenizer(features, FEATURE_SEPARATOR);

        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            String name = extractName(token);
            String[] values = extractValues(token);
            featureMap.put(name, values[0]);
        }

        return featureMap;
    }

    private static String extractName(String fItem)
    {
        try
        {
            return fItem.substring(0, fItem.indexOf(NAME_SEPARATOR));
        }
        catch (Exception e)
        {
            return fItem;
        }
    }

    private static String[] extractValues(String fItem)
    {
        return fItem.substring(fItem.indexOf(NAME_SEPARATOR) + 1).split(VALUE_SEPARATOR);
    }
}
