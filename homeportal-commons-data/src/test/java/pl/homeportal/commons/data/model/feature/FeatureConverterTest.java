package pl.homeportal.commons.data.model.feature;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.util.Map;



public class FeatureConverterTest
{
    @Test
    public void toFeatureMap()
    {
        // given
        final String features = "|name:value1^value2^value3||title:value4^value5^value6|";

        // when
        Map<String, String> converted = FeatureConverter.toFeatureMap(features);

        // then
        assertThat(converted.keySet(), hasSize(2));
    }
}