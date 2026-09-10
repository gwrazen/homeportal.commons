package pl.homeportal.commons.data.repository;

import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.ValueBridgeRef;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import pl.homeportal.commons.data.entity.AbstractEntity;
import pl.homeportal.commons.data.search.bridge.PropertyTypeBridge;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

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
    @FullTextField(projectable = Projectable.YES, valueBridge = @ValueBridgeRef(type = PropertyTypeBridge.class))
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
