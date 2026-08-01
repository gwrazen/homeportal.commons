package pl.homeportal.commons.data.model.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isBlank;
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
    private static final Pattern VALUES = Pattern.compile(Pattern.quote(VALUE_SEPARATOR));

    /**
     * Converting String of features to Map of features e.g.
     * |name:value^value^value||title:value^value^value|
     *
     * Wartoscia jest cala czesc po dwukropku, razem z separatorami wartosci —
     * wczesniej brana byla tylko values[0], wiec kazda cecha wielowartosciowa
     * (np. MEDIA:prąd^woda^gaz) tracila wszystko poza pierwsza wartoscia przy
     * kazdej serializacji. Rozbicie na liste daje {@link #toFeatureValues}.
     */
    public static Map<String, String> toFeatureMap(String features)
    {
        final Map<String, String> featureMap = new LinkedHashMap<>();
        if (isBlank(features))
        {
            return featureMap;
        }

        StringTokenizer tokenizer = new StringTokenizer(features, FEATURE_SEPARATOR);

        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            featureMap.put(extractName(token), extractValue(token));
        }

        return featureMap;
    }

    /**
     * Wariant rozbijajacy cechy wielowartosciowe na liste wartosci.
     */
    public static Map<String, List<String>> toFeatureValues(String features)
    {
        final Map<String, List<String>> featureValues = new LinkedHashMap<>();
        toFeatureMap(features).forEach((name, value) -> featureValues.put(name, splitValues(value)));

        return featureValues;
    }

    /**
     * Kolejnosc wyjscia jest deterministyczna (klucze posortowane) — przy iteracji
     * po HashMap ta sama mapa dawala rozne stringi, wiec ponowna serializacja
     * niezmienionej oferty brudzila wiersz w bazie i wymuszala re-indeks.
     */
    public static String toFeatures(Map<String, String> featureMap)
    {
        if (featureMap == null || featureMap.isEmpty())
        {
            return null;
        }

        final StringBuilder features = new StringBuilder();
        for (Map.Entry<String, String> entry : new TreeMap<>(featureMap).entrySet())
        {
            features.append(FEATURE_SEPARATOR);
            features.append(entry.getKey());
            features.append(NAME_SEPARATOR);
            features.append(entry.getValue());
            features.append(FEATURE_SEPARATOR);
        }
        return features.toString();
    }

    public static List<String> splitValues(String value)
    {
        if (isBlank(value))
        {
            return emptyList();
        }

        // Pattern.quote jest konieczne: VALUE_SEPARATOR to "^", czyli w regexie
        // kotwica poczatku wejscia o zerowej dlugosci — split("^") nie dzielil nic.
        return new ArrayList<>(Arrays.asList(VALUES.split(value)));
    }

    private static String extractName(String fItem)
    {
        final int separator = fItem.indexOf(NAME_SEPARATOR);
        return separator < 0 ? fItem : fItem.substring(0, separator);
    }

    private static String extractValue(String fItem)
    {
        final int separator = fItem.indexOf(NAME_SEPARATOR);
        return separator < 0 ? fItem : fItem.substring(separator + 1);
    }
}
