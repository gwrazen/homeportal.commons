package pl.homeportal.mail.portals;

import lombok.Data;
import pl.homeportal.i18n.Language;

/**
 * Created by gwrazen on 13/10/2015.
 */
@Data
public class PortalsActivatedDTO
{
    private String email;
    private Language language = Language.POLISH;
}
