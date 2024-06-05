package pl.homeportal.commons.data.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 1 - Sale
 * 2 - Rent
 *
 * @author Grzegorz Wrażeń
 */
public class Activity
{
    private static final int INITIAL_CAPACITY = 2;

    public static final String SALE = "1";
    public static final String RENT = "2";

    public static final String SALE_STRING = "SALE";
    public static final String RENT_STRING = "RENT";

    private static final String SALE_URI = "sprzedaz";
    private static final String RENT_URI = "wynajem";

    private static final String MESSAGE_SALE = "activity.sale";
    private static final String MESSAGE_RENT = "activity.rent";

    private static final Map<String, String> ACTIVITIES_TO_STRINGS = new HashMap<>(INITIAL_CAPACITY);
    private static final Map<String, String> URIS_TO_ACTIVITIES = new HashMap<>(INITIAL_CAPACITY);
    private static final Map<String, String> ACTIVITIES_TO_URIS = new HashMap<>(INITIAL_CAPACITY);
    private static final Map<String, String> ACTIVITIES_TO_MESSAGES = new HashMap<>(INITIAL_CAPACITY);

    static
    {
        ACTIVITIES_TO_STRINGS.put(SALE, SALE_STRING);
        ACTIVITIES_TO_STRINGS.put(RENT, RENT_STRING);

        URIS_TO_ACTIVITIES.put(SALE_URI, SALE);
        URIS_TO_ACTIVITIES.put(RENT_URI, RENT);

        ACTIVITIES_TO_URIS.put(SALE, SALE_URI);
        ACTIVITIES_TO_URIS.put(RENT, RENT_URI);

        ACTIVITIES_TO_MESSAGES.put(SALE, MESSAGE_SALE);
        ACTIVITIES_TO_MESSAGES.put(RENT, MESSAGE_RENT);
    }

    public static String asString(String activityID)
    {
        return ACTIVITIES_TO_STRINGS.get(activityID);
    }

    public static String getActivityByUri(String uri)
    {
        return URIS_TO_ACTIVITIES.get(uri);
    }

    public static String getUriByActivity(String activityID)
    {
        return ACTIVITIES_TO_URIS.get(activityID);
    }

    public static String getTranslationKeyByActivity(String activityID)
    {
        return ACTIVITIES_TO_MESSAGES.get(activityID);
    }
}
