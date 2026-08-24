package com.spendlog.app.utils;

import com.spendlog.app.R;

public class CategoryIcon {

    // devuelve el id del drawable vectorial de la categoria. Antes esto devolvia
    // un emoji dentro de un TextView, pero cada dispositivo lo dibujaba distinto
    // segun la fuente que tuviera instalada; un vector se ve igual en todos.
    //
    // los nombres coinciden con las categorias semilla; las que crea el usuario caen en el generico
    public static int forCategory(String categoryName) {
        switch (categoryName) {
            case "Transporte":
                return R.drawable.ic_categoria_transporte;
            case "Insumos":
                return R.drawable.ic_categoria_insumos;
            case "Arriendo":
                return R.drawable.ic_categoria_arriendo;
            case "Servicios":
                return R.drawable.ic_categoria_servicios;
            default:
                return R.drawable.ic_categoria_otros;
        }
    }
}
