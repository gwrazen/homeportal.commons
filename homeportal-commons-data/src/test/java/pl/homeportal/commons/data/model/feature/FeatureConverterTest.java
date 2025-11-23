package pl.homeportal.commons.data.model.feature;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;

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
}