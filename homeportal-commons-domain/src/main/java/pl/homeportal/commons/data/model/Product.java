package pl.homeportal.commons.data.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 1 - House
 * 2 - Land
 * 3 - Office
 * 4 - Apartment
 * 5 - Hall
 * 6 - Object
 *
 * @author Grzegorz Wrażeń
 */
public class Product
{
    private static final int INITIAL_CAPACITY = 6;

    public static final String HOUSE = "1";
    public static final String LAND = "2";
    public static final String OFFICE = "3";
    public static final String APARTMENT = "4";
    public static final String HALL = "5";
    public static final String OBJECT = "6";

    public static final String HOUSE_STRING = "HOUSE";
    public static final String LAND_STRING = "LAND";
    public static final String OFFICE_STRING = "OFFICE";
    public static final String APARTMENT_STRING = "APARTMENT";
    public static final String HALL_STRING = "HALL";
    public static final String OBJECT_STRING = "OBJECT";

    public static final String HOUSES_STRING = "HOUSES";
    public static final String LANDS_STRING = "LANDS";
    public static final String OFFICES_STRING = "OFFICES";
    public static final String APARTMENTS_STRING = "APARTMENTS";
    public static final String HALLS_STRING = "HALLS";
    public static final String OBJECTS_STRING = "OBJECTS";

    public static final String HOUSE_LIST_URI = "domy";
    public static final String LAND_LIST_URI = "dzialki";
    public static final String OFFICE_LIST_URI = "lokale";
    public static final String APARTMENT_LIST_URI = "mieszkania";
    public static final String HALL_LIST_URI = "hale-magazyny";
    public static final String OBJECT_LIST_URI = "obiekty";

    public static final String HOUSE_DETAILS_URI = "dom";
    public static final String LAND_DETAILS_URI = "dzialka";
    public static final String OFFICE_DETAILS_URI = "lokal";
    public static final String APARTMENT_DETAILS_URI = "mieszkanie";
    public static final String HALL_DETAILS_URI = "hala-magazyn";
    public static final String OBJECT_DETAILS_URI = "obiekt";

    public static final String APARTMENTS_MSG_KEY = "product.apartments";
    public static final String HOUSES_MSG_KEY = "product.houses";
    public static final String LANDS_MSG_KEY = "product.lands";
    public static final String OFFICES_MSG_KEY = "product.offices";
    public static final String HALLS_MSG_KEY = "product.halls";
    public static final String OBJECTS_MSG_KEY = "product.objects";

    public static final String APARTMENT_MSG_KEY = "product.apartment";
    public static final String HOUSE_MSG_KEY = "product.house";
    public static final String LAND_MSG_KEY = "product.land";
    public static final String OFFICE_MSG_KEY = "product.office";
    public static final String HALL_MSG_KEY = "product.hall";
    public static final String OBJECT_MSG_KEY = "product.object";


    private static final Map<String, String> PRODUCTS_TO_STRINGS_DETAILS = new HashMap<>(INITIAL_CAPACITY);
    private static final Map<String, String> PRODUCTS_TO_STRINGS_LIST = new HashMap<>(INITIAL_CAPACITY);
    private static final Map<String, String> PRODUCTS_TO_LIST_URIS = new HashMap<>(INITIAL_CAPACITY);
    private static final Map<String, String> LIST_URIS_TO_PRODUCTS = new HashMap<>(INITIAL_CAPACITY);
    private static final Map<String, String> PRODUCTS_TO_DETAILS_URIS = new HashMap<>(INITIAL_CAPACITY);
    private static final Map<String, String> DETAILS_URIS_TO_PRODUCTS = new HashMap<>(INITIAL_CAPACITY);
    private static final Map<String, String> PRODUCTS_TO_MESSAGES = new HashMap<>(INITIAL_CAPACITY);
    private static final Map<String, String> PRODUCT_TO_MESSAGES = new HashMap<>(INITIAL_CAPACITY);

    static
    {
        PRODUCTS_TO_STRINGS_DETAILS.put(HOUSE, HOUSE_STRING);
        PRODUCTS_TO_STRINGS_DETAILS.put(LAND, LAND_STRING);
        PRODUCTS_TO_STRINGS_DETAILS.put(OFFICE, OFFICE_STRING);
        PRODUCTS_TO_STRINGS_DETAILS.put(APARTMENT, APARTMENT_STRING);
        PRODUCTS_TO_STRINGS_DETAILS.put(HALL, HALL_STRING);
        PRODUCTS_TO_STRINGS_DETAILS.put(OBJECT, OBJECT_STRING);

        PRODUCTS_TO_STRINGS_LIST.put(HOUSE, HOUSES_STRING);
        PRODUCTS_TO_STRINGS_LIST.put(LAND, LANDS_STRING);
        PRODUCTS_TO_STRINGS_LIST.put(OFFICE, OFFICES_STRING);
        PRODUCTS_TO_STRINGS_LIST.put(APARTMENT, APARTMENTS_STRING);
        PRODUCTS_TO_STRINGS_LIST.put(HALL, HALLS_STRING);
        PRODUCTS_TO_STRINGS_LIST.put(OBJECT, OBJECTS_STRING);

        PRODUCTS_TO_LIST_URIS.put(HOUSE, HOUSE_LIST_URI);
        PRODUCTS_TO_LIST_URIS.put(LAND, LAND_LIST_URI);
        PRODUCTS_TO_LIST_URIS.put(OFFICE, OFFICE_LIST_URI);
        PRODUCTS_TO_LIST_URIS.put(APARTMENT, APARTMENT_LIST_URI);
        PRODUCTS_TO_LIST_URIS.put(HALL, HALL_LIST_URI);
        PRODUCTS_TO_LIST_URIS.put(OBJECT, OBJECT_LIST_URI);

        LIST_URIS_TO_PRODUCTS.put(HOUSE_LIST_URI, HOUSE);
        LIST_URIS_TO_PRODUCTS.put(LAND_LIST_URI, LAND);
        LIST_URIS_TO_PRODUCTS.put(OFFICE_LIST_URI, OFFICE);
        LIST_URIS_TO_PRODUCTS.put(APARTMENT_LIST_URI, APARTMENT);
        LIST_URIS_TO_PRODUCTS.put(HALL_LIST_URI, HALL);
        LIST_URIS_TO_PRODUCTS.put(OBJECT_LIST_URI, OBJECT);

        PRODUCTS_TO_DETAILS_URIS.put(HOUSE, HOUSE_DETAILS_URI);
        PRODUCTS_TO_DETAILS_URIS.put(LAND, LAND_DETAILS_URI);
        PRODUCTS_TO_DETAILS_URIS.put(OFFICE, OFFICE_DETAILS_URI);
        PRODUCTS_TO_DETAILS_URIS.put(APARTMENT, APARTMENT_DETAILS_URI);
        PRODUCTS_TO_DETAILS_URIS.put(HALL, HALL_DETAILS_URI);
        PRODUCTS_TO_DETAILS_URIS.put(OBJECT, OBJECT_DETAILS_URI);

        DETAILS_URIS_TO_PRODUCTS.put(HOUSE_DETAILS_URI, HOUSE);
        DETAILS_URIS_TO_PRODUCTS.put(LAND_DETAILS_URI, LAND);
        DETAILS_URIS_TO_PRODUCTS.put(OFFICE_DETAILS_URI, OFFICE);
        DETAILS_URIS_TO_PRODUCTS.put(APARTMENT_DETAILS_URI, APARTMENT);
        DETAILS_URIS_TO_PRODUCTS.put(HALL_DETAILS_URI, HALL);
        DETAILS_URIS_TO_PRODUCTS.put(OBJECT_DETAILS_URI, OBJECT);

        PRODUCTS_TO_MESSAGES.put(HOUSE, HOUSES_MSG_KEY);
        PRODUCTS_TO_MESSAGES.put(LAND, LANDS_MSG_KEY);
        PRODUCTS_TO_MESSAGES.put(OFFICE, OFFICES_MSG_KEY);
        PRODUCTS_TO_MESSAGES.put(APARTMENT, APARTMENTS_MSG_KEY);
        PRODUCTS_TO_MESSAGES.put(HALL, HALLS_MSG_KEY);
        PRODUCTS_TO_MESSAGES.put(OBJECT, OBJECTS_MSG_KEY);

        PRODUCT_TO_MESSAGES.put(APARTMENT, APARTMENT_MSG_KEY);
        PRODUCT_TO_MESSAGES.put(HOUSE, HOUSE_MSG_KEY);
        PRODUCT_TO_MESSAGES.put(LAND, LAND_MSG_KEY);
        PRODUCT_TO_MESSAGES.put(OFFICE, OFFICE_MSG_KEY);
        PRODUCT_TO_MESSAGES.put(HALL, HALL_MSG_KEY);
        PRODUCT_TO_MESSAGES.put(OBJECT, OBJECT_MSG_KEY);
    }

    public static String asStringForDetails(String productID)
    {
        return PRODUCTS_TO_STRINGS_DETAILS.get(productID);
    }

    public static String asStringForList(String productID)
    {
        return PRODUCTS_TO_STRINGS_LIST.get(productID);
    }

    public static String getProductByUri(String uri)
    {
        return LIST_URIS_TO_PRODUCTS.get(uri) != null ? LIST_URIS_TO_PRODUCTS.get(uri) : DETAILS_URIS_TO_PRODUCTS.get(uri);
    }

    public static String getUriByProduct(String productID)
    {
        return PRODUCTS_TO_LIST_URIS.get(productID);
    }

    public static String getUriForDetailsByProduct(String productID)
    {
        return PRODUCTS_TO_DETAILS_URIS.get(productID);
    }

    public static String getTranslationKeyByProducts(String productID)
    {
        return PRODUCTS_TO_MESSAGES.get(productID);
    }
    public static String getTranslationKeyByProduct(String productID)
    {
        return PRODUCT_TO_MESSAGES.get(productID);
    }
}
