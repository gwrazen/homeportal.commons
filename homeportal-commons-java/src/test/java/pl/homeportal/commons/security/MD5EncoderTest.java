package pl.homeportal.commons.security;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MD5EncoderTest
{
    private static final String KEY = "homeportal-key";

    @Test
    public void hashIsStableForAsciiInput()
    {
        assertEquals(MD5Encoder.createMD5Hash("user@homeportal.pl", KEY),
                     MD5Encoder.createMD5Hash("user@homeportal.pl", KEY));
    }

    @Test
    public void hashHasFullMd5Length()
    {
        assertEquals(32, MD5Encoder.createMD5Hash("user@homeportal.pl", KEY).length());
    }

    /**
     * Regresja: data.getBytes() bez charsetu bralo domyslne kodowanie platformy,
     * wiec ten sam login z polskim znakiem dawal inny skrot po zmianie JVM/OS —
     * uzytkownik przestawal sie logowac, bez zadnego bledu w logach.
     */
    @Test
    public void hashOfNonAsciiInputDoesNotDependOnPlatformCharset()
    {
        // wartosc wyliczona dla UTF-8; przy ISO-8859-1 bajty (a wiec i skrot) sa inne
        assertEquals(hashWithUtf8Bytes(), MD5Encoder.createMD5Hash("zażółć", KEY));
    }

    private String hashWithUtf8Bytes()
    {
        // referencja liczona niezaleznie od implementacji MD5Encoder
        try
        {
            java.security.MessageDigest md5 = java.security.MessageDigest.getInstance("MD5");
            md5.update("zażółć".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] digest = md5.digest(KEY.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            final StringBuilder sb = new StringBuilder();
            for (byte b : digest)
            {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        }
        catch (Exception e)
        {
            throw new AssertionError(e);
        }
    }
}
