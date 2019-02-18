package pl.homeportal.commons.i18n;

/**
 * Created with IntelliJ IDEA.
 * User: gwrazen
 * Date: 31/01/14
 * Time: 23:38
 * To change this template use File | Settings | File Templates.
 */
public enum Language
{
    POLISH("pl"),
    ENGLISH("en");

    private String value;

    private Language(String language)
    {
        this.value = language;
    }

    public String getValue()
    {
        return value;
    }

    public static Language getByValue(String value)
    {
        for (Language language : values())
        {
            if(language.getValue().equalsIgnoreCase(value))
                return language;
        }

        return null;
    }
}
