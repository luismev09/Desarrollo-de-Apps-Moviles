package com.spendlog.app.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {

    public static String format(double amount) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        // el peso colombiano no se usa con decimales en el dia a dia
        currencyFormat.setMaximumFractionDigits(0);
        // el formato de es-CO separa el simbolo del numero con un espacio duro,
        // el que no parte la linea, y la especificacion lo escribe pegado: $847.500
        return currencyFormat.format(amount).replace(" ", "");
    }
}
