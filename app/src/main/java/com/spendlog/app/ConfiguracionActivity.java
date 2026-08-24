package com.spendlog.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.spendlog.app.database.DatabaseHelper;
import com.spendlog.app.models.Category;
import com.spendlog.app.utils.CategoryIcon;
import com.spendlog.app.utils.CurrencyFormatter;
import com.spendlog.app.utils.SystemBars;

public class ConfiguracionActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "spendlog_prefs";
    public static final String KEY_ALERTS_ENABLED = "alertas_activas";

    private LinearLayout contenedorCategorias;
    private SwitchMaterial switchAlertas;
    private Button botonAgregarCategoria;
    private BottomNavigationView barraNavegacion;

    private DatabaseHelper dbHelper;
    private AlertDialog activeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        dbHelper = new DatabaseHelper(this);

        bindViews();
        SystemBars.apply(findViewById(R.id.encabezado), barraNavegacion);
        setupBottomNavigation();
        setupSwitch();
        setupAddButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barraNavegacion.setSelectedItemId(R.id.nav_config);
        loadCategories();
    }

    private void bindViews() {
        contenedorCategorias = findViewById(R.id.contenedor_categorias);
        switchAlertas = findViewById(R.id.switch_alertas);
        botonAgregarCategoria = findViewById(R.id.boton_agregar_categoria);
        barraNavegacion = findViewById(R.id.barra_navegacion);
    }

    private void setupBottomNavigation() {
        barraNavegacion.setSelectedItemId(R.id.nav_config);
        barraNavegacion.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_inicio) {
                openScreen(DashboardActivity.class);
            } else if (itemId == R.id.nav_historial) {
                openScreen(HistorialActivity.class);
            }
            return true;
        });
    }

    private void setupSwitch() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        switchAlertas.setChecked(preferences.getBoolean(KEY_ALERTS_ENABLED, true));
        switchAlertas.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferences.edit().putBoolean(KEY_ALERTS_ENABLED, isChecked).apply());
    }

    private void setupAddButton() {
        botonAgregarCategoria.setOnClickListener(v -> showNewCategoryDialog());
    }

    private void loadCategories() {
        contenedorCategorias.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Category category : dbHelper.getAllCategories()) {
            View row = inflater.inflate(R.layout.item_categoria, contenedorCategorias, false);

            ImageView iconoCategoria = row.findViewById(R.id.icono_categoria);
            TextView textoNombre = row.findViewById(R.id.texto_nombre);
            TextView textoLimite = row.findViewById(R.id.texto_limite);

            iconoCategoria.setImageResource(CategoryIcon.forCategory(category.getName()));
            textoNombre.setText(category.getName());
            textoLimite.setText(limitLabel(category));

            row.setOnClickListener(v -> showLimitDialog(category));
            contenedorCategorias.addView(row);
        }
    }

    private String limitLabel(Category category) {
        if (category.getMonthlyLimit() > 0) {
            return getString(R.string.limite_con_monto,
                    CurrencyFormatter.format(category.getMonthlyLimit()));
        }
        return getString(R.string.sin_limite);
    }

    private void showLimitDialog(Category category) {
        // tocar dos veces seguidas abria dos dialogos y el campo solo guarda el
        // ultimo: el primero se quedaba sin cerrar al destruirse la pantalla
        if (activeDialog != null && activeDialog.isShowing()) {
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_limite, null);
        EditText campoLimite = dialogView.findViewById(R.id.campo_limite);

        if (category.getMonthlyLimit() > 0) {
            // se redondea igual que la etiqueta de la fila: con un truncado, reabrir
            // el dialogo y confirmar sin tocar nada bajaba el limite guardado
            campoLimite.setText(String.valueOf(Math.round(category.getMonthlyLimit())));
        }

        activeDialog = new AlertDialog.Builder(this)
                .setTitle(category.getName())
                .setView(dialogView)
                .setPositiveButton(R.string.boton_guardar, (dialog, which) -> {
                    dbHelper.updateCategoryLimit(category.getId(),
                            readAmount(campoLimite.getText().toString().trim()));
                    Toast.makeText(this, R.string.limite_actualizado, Toast.LENGTH_SHORT).show();
                    loadCategories();
                })
                .setNegativeButton(R.string.boton_cancelar, null)
                .show();
    }

    private void showNewCategoryDialog() {
        if (activeDialog != null && activeDialog.isShowing()) {
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_categoria, null);
        EditText campoNombre = dialogView.findViewById(R.id.campo_nombre);
        EditText campoLimite = dialogView.findViewById(R.id.campo_limite);

        // el boton positivo se deja sin accion aqui y se le pone el listener despues
        // de mostrar el dialogo: con el listener normal, AlertDialog cierra la ventana
        // pase lo que pase, asi que el aviso de "ingresa un nombre" salia junto con el
        // dialogo cerrandose y el usuario perdia el limite que ya habia escrito
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_nueva_categoria)
                .setView(dialogView)
                .setPositiveButton(R.string.boton_guardar, null)
                .setNegativeButton(R.string.boton_cancelar, null)
                .create();

        dialog.setOnShowListener(shown -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name = campoNombre.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, R.string.error_nombre_vacio, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    dbHelper.saveCategory(name, readAmount(campoLimite.getText().toString().trim()));
                    Toast.makeText(this, R.string.categoria_creada, Toast.LENGTH_SHORT).show();
                    loadCategories();
                    dialog.dismiss();
                }));

        dialog.show();
        activeDialog = dialog;
    }

    // el campo es de solo digitos: dejarlo vacio vale cero, que aqui significa sin limite
    private double readAmount(String amountText) {
        if (amountText.isEmpty()) {
            return 0;
        }
        return Double.parseDouble(amountText);
    }

    private void openScreen(Class<?> screen) {
        Intent intent = new Intent(this, screen);
        // reutiliza la instancia que ya este en la pila en vez de apilar otra
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        // al girar la pantalla la Activity muere con el dialogo todavia abierto
        // y el sistema lo reporta como una ventana filtrada
        if (activeDialog != null) {
            activeDialog.dismiss();
        }
        dbHelper.close();
        super.onDestroy();
    }
}
