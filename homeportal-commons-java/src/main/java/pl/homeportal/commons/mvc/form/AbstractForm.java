package pl.homeportal.commons.mvc.form;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Grzegorz Wrażeń on 08-03-2023 at 12:09
 */

@Getter
@Setter
public class AbstractForm
{
    private Integer id;
    private String created;
    private String modified;
    private boolean saved;

    public boolean isAddition()
    {
        return id == null ? true : false;
    }

    public boolean isEdition()
    {
        return !isAddition();
    }
}
