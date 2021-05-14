package pl.homeportal.commons.encrypt;

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
            md5.update(data.getBytes());
            byte result[] = md5.digest(key.getBytes());
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
            return null;
        }
    }
}
