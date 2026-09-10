package pl.homeportal.commons.data.search.bridge;

import org.junit.Test;
import pl.homeportal.commons.text.Constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FeatureBridgeTest
{
    private final FeatureBridge featureBridge = new FeatureBridge();

    @Test
    public void testObjectToStringCorrect()
    {
        String features = featureBridge.toIndexedValue(before(), null);
        assertEquals(after(), features);
    }

    @Test
    public void testObjectToStringNull()
    {
        String features = featureBridge.toIndexedValue(null, null);
        assertNull(features);
    }

    @Test
    public void testObjectToStringEmptyString()
    {
        String features = featureBridge.toIndexedValue(Constants.SPACE, null);
        assertNull(features);
    }

    @Test
    public void testObjectToStringSpecialCharacters()
    {
        String features = featureBridge.toIndexedValue(Constants.DOT, null);
        assertNull(features);
    }

    private String before()
    {
        return "|MARKET:wtórny||PROPERTY_TYPE:wolnostojący|HOUSE_CELLAR:Jest||LAND_AREA_SIZE:4002||WINDOWS:plastikowe||HEATING:węglowe,||HOUSE_ROOF_TYPE:skośny||MEDIA:prąd,   woda,   kanalizacja,   oczyszczalnia,||SURROUNDING:wieś,   las,   jezioro,|";
    }

    private String after()
    {
        return "wtórny wolnostojący jest 4002 plastikowe węglowe skośny prąd woda kanalizacja oczyszczalnia wieś las jezioro";
    }
}