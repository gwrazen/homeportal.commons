package pl.homeportal.commons.data.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.ValueBridgeRef;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;
import pl.homeportal.commons.data.search.bridge.IdentifierAsStringBridge;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * Created by Grzegorz Wrażeń on 12.03.2017.
 */
@Setter
@Getter
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public class AbstractEntity<IDENTITY extends Number> implements Identifiable, Serializable
{
    /**
     * ⚠️ {@code @KeywordField(name = "id")} jest tu od Search 6/7 i NIE jest ozdobnikiem.
     * Do 5.x identyfikator byl zwyklym polem indeksu o nazwie "id" i zapytania odwolywaly sie
     * do niego wprost. W 6/7 identyfikator dokumentu przestal nim byc, wiec kazde zapytanie
     * postaci {@code id:123} przestawalo trafiac w cokolwiek — bez bledu, samym zerem wynikow.
     * Ta adnotacja przywraca tamto pole wszystkim encjom portalu i hopa naraz.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    @KeywordField(name = "id", valueBridge = @ValueBridgeRef(type = IdentifierAsStringBridge.class))
    protected IDENTITY id;

    @Override
    public String getIdAsString()
    {
        return String.valueOf(id.intValue());
    }

    @Override
    public boolean isPersisted()
    {
        return id != null ? true : false;
    }

    @Override
    public boolean isTransient()
    {
        return id == null ? true : false;
    }

    @Override
    public String toString()
    {
        return new StringBuffer()
                .append(getClass().getSimpleName())
                .append(" [id=")
                .append(getId())
                .append("]")
                .toString();
    }
}
