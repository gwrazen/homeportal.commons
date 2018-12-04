package pl.homeportal.mail.portals;

import pl.homeportal.i18n.Language;
import pl.homeportal.model.entities.City;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by gwrazen on 14/09/2015.
 */
public class PortalsOffersDTO
{
    private Set<String> emails;
    private Map<City, CityResult<String, Integer>> cityMap = new LinkedHashMap<City, CityResult<String, Integer>>();

    public Language getLanguage()
    {
        return Language.POLISH;
    }

    public Set<String> getEmails()
    {
        return emails;
    }

    public void setEmails(Set<String> emails)
    {
        this.emails = emails;
    }

    public void setSaleApartmentsCount(String key, City city, int quantity)
    {
        if(cityMap.get(city) == null)
        {
            cityMap.put(city, new CityResult<String, Integer>());
        }

        cityMap.get(city).put(key, quantity);
        ((CityResult)cityMap.get(city)).increment(quantity);
    }

    public void setSaleHousesCount(String key, City city, int quantity)
    {
        if(cityMap.get(city) == null)
        {
            cityMap.put(city, new CityResult<String, Integer>());
        }

        cityMap.get(city).put(key, quantity);
        ((CityResult)cityMap.get(city)).increment(quantity);
    }

    public void setSaleLandsCount(String key, City city, int quantity)
    {
        if(cityMap.get(city) == null)
        {
            cityMap.put(city, new CityResult<String, Integer>());
        }

        cityMap.get(city).put(key, quantity);
        ((CityResult)cityMap.get(city)).increment(quantity);
    }

    public void setSaleOfficesCount(String key, City city, int quantity)
    {
        if(cityMap.get(city) == null)
        {
            cityMap.put(city, new CityResult<String, Integer>());
        }

        cityMap.get(city).put(key, quantity);
        ((CityResult)cityMap.get(city)).increment(quantity);
    }

    public void setSaleHallsCount(String key, City city, int quantity)
    {
        if(cityMap.get(city) == null)
        {
            cityMap.put(city, new CityResult<String, Integer>());
        }

        cityMap.get(city).put(key, quantity);
        ((CityResult)cityMap.get(city)).increment(quantity);
    }

    public void setSaleObjectsCount(String key, City city, int quantity)
    {
        if(cityMap.get(city) == null)
        {
            cityMap.put(city, new CityResult<String, Integer>());
        }

        cityMap.get(city).put(key, quantity);
        ((CityResult)cityMap.get(city)).increment(quantity);
    }

    public void setRentApartmentsCount(String key, City city, int quantity)
    {
        if(cityMap.get(city) == null)
        {
            cityMap.put(city, new CityResult<String, Integer>());
        }

        cityMap.get(city).put(key, quantity);
        ((CityResult)cityMap.get(city)).increment(quantity);
    }

    public void setRentHousesCount(String key, City city, int quantity)
    {
        if(cityMap.get(city) == null)
        {
            cityMap.put(city, new CityResult<String, Integer>());
        }

        cityMap.get(city).put(key, quantity);
        ((CityResult)cityMap.get(city)).increment(quantity);
    }

    public void setRentLandsCount(String key, City city, int quantity)
    {
        if(cityMap.get(city) == null)
        {
            cityMap.put(city, new CityResult<String, Integer>());
        }

        cityMap.get(city).put(key, quantity);
        ((CityResult)cityMap.get(city)).increment(quantity);
    }

    public void setRentOfficesCount(String key, City city, int quantity)
    {
        if(cityMap.get(city) == null)
        {
            cityMap.put(city, new CityResult<String, Integer>());
        }

        cityMap.get(city).put(key, quantity);
        ((CityResult)cityMap.get(city)).increment(quantity);
    }

    public void setRentHallsCount(String key, City city, int quantity)
    {
        if(cityMap.get(city) == null)
        {
            cityMap.put(city, new CityResult<String, Integer>());
        }

        cityMap.get(city).put(key, quantity);
        ((CityResult)cityMap.get(city)).increment(quantity);
    }

    public void setRentObjectsCount(String key, City city, int quantity)
    {
        if(cityMap.get(city) == null)
        {
            cityMap.put(city, new CityResult<String, Integer>());
        }

        cityMap.get(city).put(key, quantity);
        ((CityResult)cityMap.get(city)).increment(quantity);
    }

    public Map<City, CityResult<String, Integer>> getCityMap()
    {
        return cityMap;
    }
}
