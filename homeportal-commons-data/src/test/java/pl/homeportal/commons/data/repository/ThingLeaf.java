package pl.homeportal.commons.data.repository;

import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;

/** Jedyny zaindeksowany typ w hierarchii {@link ThingRoot}. */
@Entity
@Indexed(index = "things")
public class ThingLeaf extends ThingRoot
{
    public ThingLeaf()
    {
    }

    public ThingLeaf(String city)
    {
        super(city);
    }
}
