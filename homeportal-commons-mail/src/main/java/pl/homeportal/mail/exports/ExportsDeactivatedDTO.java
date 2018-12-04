package pl.homeportal.mail.exports;

import pl.homeportal.i18n.Language;

/**
 * Created by gwrazen on 13/10/2015.
 */
public class ExportsDeactivatedDTO
{
    private String email;
    private Language language = Language.POLISH;

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public Language getLanguage()
    {
        return language;
    }

    public void setLanguage(Language language)
    {
        this.language = language;
    }
}
