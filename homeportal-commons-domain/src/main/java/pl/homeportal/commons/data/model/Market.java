package pl.homeportal.commons.data.model;

/**
 * Created by Grzegorz Wrażeń on 22/11/2025 at 09:24
 */
public class Market
{
    public static final String PRIMARY   = "1";
    public static final String SECONDARY = "2";

    public static final String PRIMARY_STRING   = "pierwotny";
    public static final String SECONDARY_STRING = "wtórny";

    public static final boolean isPrimary(String marketAsString)
    {
        return PRIMARY_STRING.equalsIgnoreCase(marketAsString);
    }

    public static final boolean isSecondary(String marketAsString)
    {
        return SECONDARY_STRING.equalsIgnoreCase(marketAsString);
    }

    /**
     * Normalizuje rynek do identyfikatora ({@code "1"} / {@code "2"}), przyjmujac <b>obie</b>
     * postacie: nazwe ({@code "pierwotny"}) i gotowy identyfikator ({@code "1"}).
     *
     * <p>⚠️ <b>Idempotencja nie jest tu wygoda, tylko naprawa cichego bledu.</b> Do 2026-08-31
     * ta metoda rozumiala wylacznie nazwy, a identyfikator oddawala jako {@code null} — czyli
     * mowila „nie wiem" o wartosci, ktora dostala w reku. Kosztowalo to caly rynek nowych ofert:
     * kreator zapisuje cechy surowa wartoscia z formularza (a {@code <select>} wysyla ID), wiec
     * {@code Offer.getMarket()} oddawal {@code null} dla <b>96%</b> ofert 2026Q3, mimo ze rynek
     * byl w bazie. Zmierzone na produkcji 2026-08-31: 1164 oferty z cecha {@code MARKET:2},
     * 116 z {@code MARKET:1}, wobec 47 zapisanych nazwa.
     *
     * <p>Wartosc nierozpoznana nadal wraca jako {@code null} — brak danych zostaje brakiem danych.
     */
    public static String asId(String marketAsString)
    {
        if (isPrimary(marketAsString))
        {
            return PRIMARY;
        }
        if (isSecondary(marketAsString))
        {
            return SECONDARY;
        }
        // juz znormalizowane — przepuszczamy bez zmian, zeby wielokrotne wywolanie nic nie psulo
        if (PRIMARY.equals(marketAsString) || SECONDARY.equals(marketAsString))
        {
            return marketAsString;
        }
        return null;
    }

    /**
     * Odwrotnosc {@link #asId(String)} — identyfikator na nazwe, rowniez idempotentnie:
     * nazwa podana na wejsciu wraca bez zmian, zeby zlozenie obu metod w dowolnej kolejnosci
     * dawalo ten sam wynik.
     */
    public static String asString(String marketAsId)
    {
        if (PRIMARY.equalsIgnoreCase(marketAsId))
        {
            return PRIMARY_STRING;
        }
        if (SECONDARY.equalsIgnoreCase(marketAsId))
        {
            return SECONDARY_STRING;
        }
        if (isPrimary(marketAsId) || isSecondary(marketAsId))
        {
            return marketAsId.toLowerCase();
        }
        return null;
    }
}
