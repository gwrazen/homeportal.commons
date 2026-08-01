package pl.homeportal.commons.data.entity;

/**
 * Minimalny kontrakt encji potrzebny warstwom, ktore chca logowac operacje CRUD,
 * ale nie moga zalezec od JPA. Implementuje go AbstractEntity z modulu -data.
 *
 * Dzieki temu homeportal-commons-logging nie musi zalezec od -data, a wiec
 * homeportal-commons-mail przestaje tranzytywnie ciagnac Hibernate, Hibernate
 * Search i Lucene tylko po to, zeby wyslac maila.
 */
public interface Identifiable
{
    String getIdAsString();

    boolean isPersisted();

    boolean isTransient();
}
