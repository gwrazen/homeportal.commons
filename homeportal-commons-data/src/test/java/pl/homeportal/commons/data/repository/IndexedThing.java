package pl.homeportal.commons.data.repository;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import pl.homeportal.commons.data.entity.AbstractEntity;
import pl.homeportal.commons.data.search.bridge.FeatureBridge;
import pl.homeportal.commons.data.search.bridge.NumericBridge;
import pl.homeportal.commons.data.search.bridge.PropertyTypeBridge;

import javax.persistence.Entity;

/**
 * Encja wylacznie na potrzeby testu integracyjnego: kazde pole jest indeksowane
 * innym bridge'em, zeby sprawdzic, ze zapytanie zbudowane przez SearchQuery trafia
 * w to, co bridge faktycznie zapisal.
 */
@Entity
@Indexed
public class IndexedThing extends AbstractEntity<Integer>
{
    @Field(store = Store.YES)
    @FieldBridge(impl = PropertyTypeBridge.class)
    private String city;

    @Field(store = Store.YES)
    @FieldBridge(impl = FeatureBridge.class)
    private String features;

    @Field(store = Store.YES)
    @FieldBridge(impl = NumericBridge.class)
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
