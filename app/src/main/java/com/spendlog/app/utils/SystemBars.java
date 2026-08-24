package com.spendlog.app.utils;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SystemBars {

    // Con targetSdk 36 Android dibuja la app de borde a borde y deja de reservar
    // el espacio de la barra de estado: sin esto el reloj del sistema queda encima
    // del titulo en las cuatro pantallas.
    //
    // No sirve android:fitsSystemWindows en la raiz. Se probo: el margen se lo
    // lleva la raiz, que tiene fondo gris, y queda una franja clara arriba donde
    // deberia seguir el verde del encabezado. Por eso el hueco se aplica al
    // encabezado, que asi pinta su verde por detras de la barra de estado.
    public static void apply(View header, View bottomView) {
        // guardamos el padding del layout antes de sumarle nada, porque el sistema
        // puede volver a mandar los insets y no queremos ir acumulando margen
        // en horizontal la barra de estado y la de navegacion se van a los lados,
        // asi que hay que sumar tambien el hueco izquierdo y el derecho
        int headerTop = header.getPaddingTop();
        int headerLeft = header.getPaddingLeft();
        int headerRight = header.getPaddingRight();
        ViewCompat.setOnApplyWindowInsetsListener(header, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(headerLeft + bars.left, headerTop + bars.top,
                    headerRight + bars.right, view.getPaddingBottom());
            return windowInsets;
        });

        int bottomPadding = bottomView.getPaddingBottom();
        int bottomLeft = bottomView.getPaddingLeft();
        int bottomRight = bottomView.getPaddingRight();
        ViewCompat.setOnApplyWindowInsetsListener(bottomView, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bottomLeft + bars.left, view.getPaddingTop(),
                    bottomRight + bars.right, bottomPadding + bars.bottom);
            return windowInsets;
        });
    }
}
