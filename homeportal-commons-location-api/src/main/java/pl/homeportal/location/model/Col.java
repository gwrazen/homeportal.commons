package pl.homeportal.location.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

/**
 * Created by Grzegorz Wrazen on 07/05/2014.
 */
@XStreamAlias("col")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"value"})
public class Col {
    @XStreamAlias("name")
    @XStreamAsAttribute
    private String name;

    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
