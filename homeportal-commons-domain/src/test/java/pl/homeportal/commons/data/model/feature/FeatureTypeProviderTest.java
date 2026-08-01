package pl.homeportal.commons.data.model.feature;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FeatureTypeProviderTest
{
    /**
     * Regresja: initForRentObject() i initForSaleObject() wypelnialy listy biurowe,
     * wiec oferty typu "obiekt" nie mialy w ogole filtrow cech.
     */
    @Test
    public void objectListsAreNotEmpty()
    {
        assertFalse(FeatureTypeProvider.forRentObject().isEmpty());
        assertFalse(FeatureTypeProvider.forSaleObject().isEmpty());
    }

    /**
     * Regresja: forRentLand() zwracalo forRentHall, czyli cechy hal dla gruntow.
     */
    @Test
    public void rentLandReturnsLandFeatures()
    {
        final List<FeatureType> land = FeatureTypeProvider.forRentLand();

        assertTrue(land.contains(FeatureType.PROPERTY_TYPE_LAND));
        assertFalse(land.equals(FeatureTypeProvider.forRentHall()));
    }

    /**
     * Regresja: FLOOR_QUANTITY bylo dodawane dwa razy do forSaleHouse.
     */
    @Test
    public void listsHaveNoDuplicates()
    {
        assertNoDuplicates(FeatureTypeProvider.forSaleHouse());
        assertNoDuplicates(FeatureTypeProvider.forSaleOffice());
        assertNoDuplicates(FeatureTypeProvider.forSaleObject());
        assertNoDuplicates(FeatureTypeProvider.forRentOffice());
        assertNoDuplicates(FeatureTypeProvider.forRentObject());
        assertNoDuplicates(FeatureTypeProvider.forRentApartment());
        assertNoDuplicates(FeatureTypeProvider.forRentHouse());
        assertNoDuplicates(FeatureTypeProvider.forRentHall());
        assertNoDuplicates(FeatureTypeProvider.forRentLand());
        assertNoDuplicates(FeatureTypeProvider.forSaleApartment());
        assertNoDuplicates(FeatureTypeProvider.forSaleHall());
        assertNoDuplicates(FeatureTypeProvider.forSaleLand());
    }

    /**
     * Gettery oddawaly zywa, mutowalna statyczna liste — jeden konsument mogl
     * trwale zmienic zestaw cech widziany przez wszystkich pozostalych.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void listsAreNotModifiable()
    {
        FeatureTypeProvider.forSaleApartment().add(FeatureType.MARKET);
    }

    @Test
    public void rentListsCarryRentOnlyFeatures()
    {
        assertTrue(FeatureTypeProvider.forRentApartment().contains(FeatureType.LEASE_TERM));
        assertFalse(FeatureTypeProvider.forSaleApartment().contains(FeatureType.LEASE_TERM));
    }

    private void assertNoDuplicates(List<FeatureType> types)
    {
        assertEquals(types.size(), new HashSet<>(types).size());
    }
}
