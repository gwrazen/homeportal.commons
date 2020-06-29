package pl.homeportal.location.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Grzegorz Wrazen on 07/05/2014.
 */
@XStreamAlias("row")
public class Row {
    @XStreamImplicit(itemFieldName = "col")
    private List<Col> cols = new ArrayList<Col>();

    public List<Col> getCols() {
        return cols;
    }

    public void setCols(List<Col> cols) {
        this.cols = cols;
    }
}
