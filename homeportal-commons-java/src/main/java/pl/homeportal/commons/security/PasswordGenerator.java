package pl.homeportal.commons.security;

import java.security.SecureRandom;

/**
 * Generator hasel i kluczy API.
 *
 * Zrodlem losowosci jest SecureRandom, nie Math.random(): to drugie to wspoldzielony
 * LCG o stanie 48-bitowym, z ktorego po zaobserwowaniu jednej wygenerowanej wartosci
 * da sie odtworzyc wszystkie kolejne w tej samej JVM.
 *
 * Created by gwrazen on 25/01/14
 */
public class PasswordGenerator
{
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate(int n)
    {
        // Losowanie wprost z jednego alfabetu — poprzednia wersja najpierw losowala
        // jedna z trzech grup, przez co cyfry (10 znakow) mialy taka sama laczna
        // wage co male litery (26 znakow).
        final char[] pw = new char[n];
        for (int i = 0; i < n; i++)
        {
            pw[i] = ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length()));
        }

        return new String(pw);
    }
}
