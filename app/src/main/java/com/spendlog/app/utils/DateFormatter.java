package com.spendlog.app.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {

    private static final Locale COLOMBIA = new Locale("es", "CO");

    // la base guarda la fecha con este formato porque como texto ordena igual que como fecha
    private static final String DATABASE_FORMAT = "yyyy-MM-dd";

    public static String forDatabase(Date date) {
        return new SimpleDateFormat(DATABASE_FORMAT, COLOMBIA).format(date);
    }

    public static String monthForDatabase(Date date) {
        return new SimpleDateFormat("yyyy-MM", COLOMBIA).format(date);
    }

    public static String longFormat(Date date) {
        return new SimpleDateFormat("d 'de' MMMM, yyyy", COLOMBIA).format(date);
    }

    public static String shortFormat(String storedDate) {
        Date date = parse(storedDate);
        return date == null ? storedDate : new SimpleDateFormat("d MMM", COLOMBIA).format(date);
    }

    // para los extremos de un rango, donde hace falta el dia y tambien el ano
    public static String dayMonthAndYear(Date date) {
        return new SimpleDateFormat("d MMM yyyy", COLOMBIA).format(date);
    }

    public static String monthAndYear(Date date) {
        String label = new SimpleDateFormat("MMMM yyyy", COLOMBIA).format(date);
        // el locale devuelve el mes en minuscula y las pantallas lo muestran capitalizado
        return label.substring(0, 1).toUpperCase(COLOMBIA) + label.substring(1);
    }

    // devuelve null si el texto no es una fecha valida, para que quien la use
    // decida que hacer en vez de reventar
    public static Date parseStored(String storedDate) {
        return parse(storedDate);
    }

    private static Date parse(String storedDate) {
        try {
            return new SimpleDateFormat(DATABASE_FORMAT, COLOMBIA).parse(storedDate);
        } catch (ParseException e) {
            return null;
        }
    }
}
