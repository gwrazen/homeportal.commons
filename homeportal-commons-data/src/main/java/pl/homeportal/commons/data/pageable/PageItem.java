package pl.homeportal.commons.data.pageable;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringStyle;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

@Getter
@Setter
public class PageItem
{
    private String label;
    private String link;
    private String current;

    @Override
    public String toString()
    {
        return reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
