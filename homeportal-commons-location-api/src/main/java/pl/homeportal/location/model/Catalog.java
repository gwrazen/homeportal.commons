package pl.homeportal.location.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Grzegorz Wrazen on 07/05/2014.
 */
@XStreamAlias("catalog")
public class Catalog {
    @XStreamAsAttribute
    private String name;

    @XStreamAsAttribute
    private String type;

    @XStreamAsAttribute
    private String date;

    @XStreamImplicit(itemFieldName = "row")
    private List<Row> rows = new ArrayList<Row>();

    public List<Row> getRows() {
        return rows;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
