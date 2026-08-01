package pl.homeportal.commons.security;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PasswordGeneratorTest
{
    @Test
    public void generatesRequestedLength()
    {
        assertEquals(10, PasswordGenerator.generate(10).length());
    }

    @Test
    public void generatesAlphanumericOnly()
    {
        assertTrue(PasswordGenerator.generate(200).matches("[0-9a-zA-Z]+"));
    }

    /**
     * Regresja: Math.random() to wspoldzielony generator LCG — z tych wartosci
     * powstaja klucze API i hasla resetu, wiec zrodlem musi byc SecureRandom.
     * Testem sprawdzalnym automatycznie jest brak powtorzen w duzej probie.
     */
    @Test
    public void generatesDistinctValues()
    {
        final Set<String> generated = new HashSet<>();
        for (int i = 0; i < 500; i++)
        {
            generated.add(PasswordGenerator.generate(12));
        }

        assertEquals(500, generated.size());
    }
}
