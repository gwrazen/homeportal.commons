package pl.homeportal.commons.text;

import org.apache.xerces.util.XMLChar;

import static org.apache.commons.lang3.StringUtils.stripAccents;
import static pl.homeportal.commons.text.Constants.EMPTY_STRING;
import static pl.homeportal.commons.text.Constants.REGEXP_SPACE;
import static pl.homeportal.commons.text.Constants.SPACE;

/**
 * Created by Grzegorz Wrazen on 28-07-2015
 */
public class StringUtils
{
    // Nazwa mowi wprost, ze cyfry TEZ sa usuwane — stara nazwa (NOT_ALPHANUMERIC)
    // sugerowala cos przeciwnego. Zachowanie zostaje bez zmian, bo z tego regexu
    // powstaja slugi i URL-e, ktore sa juz zaindeksowane.
    private static final String NOT_ALPHABETIC = "[^a-zA-Z]";
    private static final String NOT_ASCII = "[^\\p{ASCII}]";
    private static final String ONE_OR_MORE = "\\s+";

    public static String normalize(String input)
    {
        return normalize(input, EMPTY_STRING);
    }

    public static String normalize(String input, String spaceReplacement)
    {
        if (input == null)
        {
            return null;
        }

        input = input.trim();
        input = stripAccents(input);
        input = input.replaceAll(NOT_ALPHABETIC, SPACE);
        input = input.replaceAll(ONE_OR_MORE, SPACE);
        input =  input.trim();
        input = input.replaceAll(REGEXP_SPACE, spaceReplacement);

        return input.toLowerCase();
    }

    public static String stripInvalidXmlCharacters(String input)
    {
        if (input == null)
        {
            return null;
        }

        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++)
        {
            char c = input.charAt(i);

            if (XMLChar.isValid(c))
            {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
