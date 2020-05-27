package pl.homeportal.commons.text;

import java.text.Normalizer;

import static org.apache.commons.lang3.StringUtils.stripAccents;
import static pl.homeportal.commons.text.Constants.EMPTY_STRING;
import static pl.homeportal.commons.text.Constants.REGEXP_SPACE;
import static pl.homeportal.commons.text.Constants.SPACE;

/**
 * Created by gwrazen on 28-07-2015.
 */
public class StringUtils
{
    private static final String NOT_ALPHANUMERIC = "[^a-zA-Z]";
    private static final String NOT_ASCII = "[^\\p{ASCII}]";
    private static final String ONE_OR_MORE = "\\s+";

    public static String normalize(String input)
    {
        String string = input.replaceAll(REGEXP_SPACE, EMPTY_STRING);
        string = Normalizer.normalize(string, Normalizer.Form.NFD);
        string = string.replaceAll(NOT_ASCII, EMPTY_STRING);

        return string.toLowerCase();
    }

    public static String stripAccentsAndClean(String string, String spaceReplacement)
    {
        string = stripAccents(string);
        string = string.replaceAll(NOT_ALPHANUMERIC, SPACE);
        string = string.replaceAll(ONE_OR_MORE, SPACE);
        string = string.replaceAll(REGEXP_SPACE, spaceReplacement);

        return string.toLowerCase();
    }
}
