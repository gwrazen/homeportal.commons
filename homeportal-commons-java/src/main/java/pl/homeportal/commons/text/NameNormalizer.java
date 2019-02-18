package pl.homeportal.commons.text;

import org.apache.commons.lang3.StringUtils;
import java.text.Normalizer;

/**
 *
 * fixme use constants
 * Created by gwrazen on 28/07/2015.
 */
public class NameNormalizer
{
    public static String normalize(String input)
    {
        String string = input.replaceAll("\\s", "").toLowerCase();
        string = Normalizer.normalize(string, Normalizer.Form.NFD);
        string = string.replaceAll("[^\\p{ASCII}]", "");

        return string;
    }

    public static String cleanAccent(String string)
    {
        string = string.toLowerCase();
        string = StringUtils.stripAccents(string);
        string = string.replaceAll("[^a-zA-Z]", " ");
        string = string.replaceAll("\\s+", " ");
        string = string.replaceAll("\\s", "-");

        return string;
    }
}
