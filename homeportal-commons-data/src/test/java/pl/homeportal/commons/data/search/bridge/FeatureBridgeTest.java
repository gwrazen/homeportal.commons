package pl.homeportal.commons.data.search.bridge;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FeatureBridgeTest
{
    private final FeatureBridge featureBridge = new FeatureBridge();

    @Test
    public void testObjectToStringCorrect()
    {
        String features = featureBridge.objectToString(before());
        assertEquals(after(), features);
    }

    @Test
    public void testObjectToStringEmptyString()
    {
        String features = featureBridge.objectToString(" ");
        assertNull(features);
    }
    @Test
    public void testObjectToStringSpecialCharacters()
    {
        String features = featureBridge.objectToString(".");
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