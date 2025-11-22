package pl.homeportal.commons.data.model;

/**
 * Created by Grzegorz Wrażeń on 22/11/2025 at 09:24
 */
public class Market
{
    public static final String PRIMARY   = "1";
    public static final String SECONDARY = "2";

    public static final String PRIMARY_STRING   = "pierwotny";
    public static final String SECONDARY_STRING = "wtórny";

    public static final boolean isPrimary(String marketAsString)
    {
        return PRIMARY_STRING.equalsIgnoreCase(marketAsString);
    }

    public static final boolean isSecondary(String marketAsString)
    {
        return SECONDARY_STRING.equalsIgnoreCase(marketAsString);
    }

    public String asId(String marketAsString)
    {
        if (isPrimary(marketAsString))
        {
            return PRIMARY;
        }
        if (isSecondary(marketAsString))
        {
            return SECONDARY;
        }
        return null;
    }
}
