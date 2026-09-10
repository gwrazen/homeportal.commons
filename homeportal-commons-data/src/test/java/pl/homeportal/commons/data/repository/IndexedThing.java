package pl.homeportal.commons.data.repository;

import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.ValueBridgeRef;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import pl.homeportal.commons.data.entity.AbstractEntity;
import pl.homeportal.commons.data.search.bridge.FeatureBridge;
import pl.homeportal.commons.data.search.bridge.NumericBridge;
import pl.homeportal.commons.data.search.bridge.PropertyTypeBridge;

import jakarta.persistence.Entity;

/**
 * Encja wylacznie na potrzeby testu integracyjnego: kazde pole jest indeksowane
 * innym bridge'em, zeby sprawdzic, ze zapytanie zbudowane przez SearchQuery trafia
 * w to, co bridge faktycznie zapisal.
 */
@Entity
@Indexed
public class IndexedThing extends AbstractEntity<Integer>
{
    @FullTextField(projectable = Projectable.YES, valueBridge = @ValueBridgeRef(type = PropertyTypeBridge.class))
    private String city;

    @FullTextField(projectable = Projectable.YES, valueBridge = @ValueBridgeRef(type = FeatureBridge.class))
    private String features;

    @FullTextField(projectable = Projectable.YES, valueBridge = @ValueBridgeRef(type = NumericBridge.class))
    private Long price;

    public IndexedThing()
    {
    }

    public IndexedThing(String city, String features, Long price)
    {
        this.city = city;
        this.features = features;
        this.price = price;
    }

    public String getCity()
    {
        return city;
    }

    public String getFeatures()
    {
        return features;
    }

    public Long getPrice()
    {
        return price;
    }
}
