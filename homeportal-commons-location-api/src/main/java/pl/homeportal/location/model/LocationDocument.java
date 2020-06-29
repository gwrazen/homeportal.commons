package pl.homeportal.location.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Created by Grzegorz Wrazen on 07/05/2014.
 */
@XStreamAlias("teryt")
public class LocationDocument {
    @XStreamAlias("catalog")
    private Catalog catalog;

    public Catalog getCatalog() {
        return catalog;
    }

    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }
}
