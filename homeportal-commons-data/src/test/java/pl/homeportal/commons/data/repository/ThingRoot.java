package pl.homeportal.commons.data.repository;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Store;
import pl.homeportal.commons.data.entity.AbstractEntity;
import pl.homeportal.commons.data.search.bridge.PropertyTypeBridge;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * Nadklasa **bez** {@code @Indexed} — zaindeksowane sa wylacznie jej podtypy.
 *
 * Odwzorowuje ksztalt modelu hopa ({@code PortalOffer} + 12 podklas), gdzie korzeniem
 * kazdego zapytania jest wlasnie taka klasa abstrakcyjna.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ThingRoot extends AbstractEntity<Integer>
{
    @Field(store = Store.YES)
    @FieldBridge(impl = PropertyTypeBridge.class)
    private String city;

    public ThingRoot()
    {
    }

    public ThingRoot(String city)
    {
        this.city = city;
    }

    public String getCity()
    {
        return city;
    }
}
