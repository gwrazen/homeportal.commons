package pl.homeportal.commons.data.model.feature;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.List;
import java.util.Map;



public class FeatureConverterTest
{
    @Test
    public void toFeatureMap()
    {
        // given
        final String features = "|name:value1^value2^value3||title:value4^value5^value6|";

        // when
        Map<String, String> convertedMap = FeatureConverter.toFeatureMap(features);
        String convertedString = FeatureConverter.toFeatures(convertedMap);

        // then
        assertThat(convertedMap.keySet(), hasSize(2));
        assertThat(convertedString, equalTo(features));
    }

    /**
     * Regresja: toFeatureMap zapisywalo tylko values[0], wiec cecha wielowartosciowa
     * traciła wszystko poza pierwsza wartoscia przy kazdej serializacji.
     */
    @Test
    public void toFeatureMap_keepsAllValuesOfAMultiValuedFeature()
    {
        // given
        final String features = "|MEDIA:prąd^woda^gaz|";

        // when
        Map<String, String> converted = FeatureConverter.toFeatureMap(features);

        // then
        assertThat(converted.get("MEDIA"), equalTo("prąd^woda^gaz"));
        assertThat(FeatureConverter.toFeatures(converted), equalTo(features));
    }

    /**
     * Regresja: VALUE_SEPARATOR to "^", czyli w regexie kotwica o zerowej dlugosci —
     * split(VALUE_SEPARATOR) nie dzielil wejscia w ogole.
     */
    @Test
    public void toFeatureValues_splitsMultiValuedFeature()
    {
        // when
        Map<String, List<String>> values = FeatureConverter.toFeatureValues("|MEDIA:prąd^woda^gaz||MARKET:wtórny|");

        // then
        assertThat(values.get("MEDIA"), hasSize(3));
        assertThat(values.get("MEDIA").get(2), equalTo("gaz"));
        assertThat(values.get("MARKET"), hasSize(1));
    }

    /**
     * Regresja: iteracja po HashMap dawala rozna kolejnosc dla tej samej mapy, wiec
     * ponowna serializacja niezmienionej oferty brudzila wiersz i wymuszala re-indeks.
     */
    @Test
    public void toFeatures_isDeterministic()
    {
        // given
        final String features = "|zzz:1||aaa:2||mmm:3|";

        // when
        final String first = FeatureConverter.toFeatures(FeatureConverter.toFeatureMap(features));

        // then
        for (int i = 0; i < 50; i++)
        {
            assertThat(FeatureConverter.toFeatures(FeatureConverter.toFeatureMap(features)), equalTo(first));
        }
        assertThat(first, equalTo("|aaa:2||mmm:3||zzz:1|"));
    }

    @Test
    public void toFeatureMap_handlesBlankAndMalformedInput()
    {
        assertThat(FeatureConverter.toFeatureMap(null).keySet(), hasSize(0));
        assertThat(FeatureConverter.toFeatureMap("").keySet(), hasSize(0));
        // token bez dwukropka: nazwa == token, wartosc == token (zachowanie sprzed zmiany)
        assertThat(FeatureConverter.toFeatureMap("|brak-separatora|").get("brak-separatora"), equalTo("brak-separatora"));
    }
}
