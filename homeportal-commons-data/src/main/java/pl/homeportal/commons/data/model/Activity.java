package pl.homeportal.commons.data.model;

/**
 * 1 - Sale
 * 2 - Rent
 *
 * @author Ryba
 */
public class Activity
{
    public static final String SALE = "1";
    public static final String RENT = "2";
    public static final String SALE_STRING = "SALE";
    public static final String RENT_STRING = "RENT";

    public static String asString(String ID)
    {
        if (SALE.equals(ID))
            return SALE_STRING;
        else
        if (RENT.equals(ID))
            return RENT_STRING;

        throw new IllegalArgumentException("Activity does not exist! ");
    }
}
