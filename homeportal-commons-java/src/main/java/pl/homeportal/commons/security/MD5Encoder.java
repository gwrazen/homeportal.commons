package pl.homeportal.commons.security;

import pl.homeportal.commons.exception.HomeportalSecurityException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static pl.homeportal.commons.text.Constants.MD5;


/**
 * Created by Grzegorz Wrazen
 */

public class MD5Encoder
{
    private static final String ZERO = "0";

    public static String createMD5Hash(String data, String key)
    {
        try
        {
            MessageDigest md5 = MessageDigest.getInstance(MD5);
            // Jawny UTF-8: domyslny charset platformy dawal inny skrot po zmianie
            // JVM/OS dla danych spoza ASCII, czyli uzytkownik przestawal sie logowac.
            md5.update(data.getBytes(StandardCharsets.UTF_8));
            byte result[] = md5.digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < result.length; i++)
            {
                String s = Integer.toHexString(result[i]);
                int length = s.length();
                if (length >= 2)
                {
                    sb.append(s, length - 2, length);
                }
                else
                {
                    sb.append(ZERO);
                    sb.append(s);
                }
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            // Zwracanie null zamienialo brak algorytmu w ciche "haslo == null".
            throw new HomeportalSecurityException("MD5 algorithm not available", e);
        }
    }
}
