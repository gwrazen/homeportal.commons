package pl.homeportal.mail.portals;

import java.util.LinkedHashMap;

/**
 * Created by gwrazen on 25/09/2015.
 */
public class CityResult<T,V> extends LinkedHashMap
{
    private int quantity;

    public int getQuantity()
    {
        return quantity;
    }

    public void increment(int quantity)
    {
        this.quantity += quantity;
    }
}
