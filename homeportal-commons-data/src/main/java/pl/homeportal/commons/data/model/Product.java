package pl.homeportal.commons.data.model;

/**
 *  1 - House
 *  2 - Land
 *  3 - Office
 *  4 - Apartment
 *  5 - Hall
 *  
 * @author Ryba
 *
 */
public class Product
{
    public static final String HOUSE     = "1";
    public static final String LAND      = "2";
    public static final String OFFICE    = "3";
    public static final String APARTMENT = "4";
    public static final String HALL      = "5";
    public static final String OBJECT    = "6";

    public static final String HOUSE_STRING     = "HOUSE";
    public static final String LAND_STRING      = "LAND";
    public static final String OFFICE_STRING    = "OFFICE";
    public static final String APARTMENT_STRING = "APARTMENT";
    public static final String HALL_STRING      = "HALL";
    public static final String OBJECT_STRING    = "OBJECT";

    public static String asString(String ID)
    {
        if(HOUSE.equals(ID))
            return HOUSE_STRING;
        else
        if(LAND.equals(ID))
            return LAND_STRING;
        else
        if(OFFICE.equals(ID))
            return OFFICE_STRING;
        else
        if(APARTMENT.equals(ID))
            return APARTMENT_STRING;
        else
        if(HALL.equals(ID))
            return HALL_STRING;
        else
        if(OBJECT.equals(ID))
            return OBJECT_STRING;

        throw new IllegalArgumentException("Product does not exist! ");
    }
}
