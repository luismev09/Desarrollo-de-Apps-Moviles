package com.spendlog.app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.spendlog.app.adapters.ExpenseAdapter;
import com.spendlog.app.database.DatabaseHelper;
import com.spendlog.app.models.Category;
import com.spendlog.app.models.Expense;
import com.spendlog.app.utils.CurrencyFormatter;
import com.spendlog.app.utils.DateFormatter;
import com.spendlog.app.utils.SystemBars;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    private static final String STATE_SELECTED_CATEGORY = "selected_category";
    private static final String STATE_VISIBLE_MONTH = "visible_month";
    private static final String STATE_RANGE_MODE = "range_mode";
    private static final String STATE_RANGE_FROM = "range_from";
    private static final String STATE_RANGE_TO = "range_to";

    private TextView textoSubtitulo;
    private TextView botonMesAnterior;
    private TextView botonMesSiguiente;
    private LinearLayout grupoChips;
    private RecyclerView listaGastos;
    private TextView textoSinGastos;
    private TextView textoTotal;
    private Button botonExportar;
    private BottomNavigationView barraNavegacion;

    private final List<Expense> expenses = new ArrayList<>();
    private ExpenseAdapter expenseAdapter;
    private DatabaseHelper dbHelper;
    private AlertDialog activeDialog;
    // cero significa que el filtro activo es "Todos"
    private int selectedCategoryId;
    private Calendar visibleMonth;

    // el historial muestra o un mes completo o un rango de fechas elegido a mano;
    // en los dos casos la consulta es la misma, lo unico que cambia son los
    // extremos del periodo
    private boolean rangeMode;
    private Calendar rangeFrom;
    private Calendar rangeTo;
    private DatePickerDialog periodPicker;
    private Toast periodHint;

    // fecha inicial a medio elegir: mientras no se confirme la final, el periodo
    // que se esta viendo no se toca, para que cancelar el segundo selector deje
    // todo como estaba
    private Calendar pendingFrom;

    // evita que un doble toque sobre la misma fila abra dos veces el editor
    private boolean openingEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        dbHelper = new DatabaseHelper(this);

        // nos paramos en el dia 1 para que sumar y restar meses no se salte ninguno
        // cuando el mes de partida tiene mas dias que el de destino
        visibleMonth = Calendar.getInstance();
        visibleMonth.set(Calendar.DAY_OF_MONTH, 1);
        rangeFrom = Calendar.getInstance();
        rangeTo = Calendar.getInstance();

        // sin esto, girar la pantalla devuelve el filtro a "Todos" y el mes visible
        // al actual, aunque el chip siga pareciendo el elegido
        if (savedInstanceState != null) {
            selectedCategoryId = savedInstanceState.getInt(STATE_SELECTED_CATEGORY);
            visibleMonth.setTimeInMillis(savedInstanceState.getLong(STATE_VISIBLE_MONTH));
            rangeMode = savedInstanceState.getBoolean(STATE_RANGE_MODE);
            rangeFrom.setTimeInMillis(savedInstanceState.getLong(STATE_RANGE_FROM));
            rangeTo.setTimeInMillis(savedInstanceState.getLong(STATE_RANGE_TO));
        }

        bindViews();
        SystemBars.apply(findViewById(R.id.encabezado), barraNavegacion);
        setupBottomNavigation();
        setupMonthNavigation();
        setupList();
        setupExportButton();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_CATEGORY, selectedCategoryId);
        outState.putLong(STATE_VISIBLE_MONTH, visibleMonth.getTimeInMillis());
        outState.putBoolean(STATE_RANGE_MODE, rangeMode);
        outState.putLong(STATE_RANGE_FROM, rangeFrom.getTimeInMillis());
        outState.putLong(STATE_RANGE_TO, rangeTo.getTimeInMillis());
    }

    // los chips se rearman aqui porque el usuario pudo crear una categoria
    // nueva en Configuracion mientras esta pantalla seguia viva
    @Override
    protected void onResume() {
        super.onResume();
        barraNavegacion.setSelectedItemId(R.id.nav_historial);
        openingEditor = false;
        setupChips();
        loadExpenses();
    }

    private void bindViews() {
        textoSubtitulo = findViewById(R.id.texto_subtitulo);
        botonMesAnterior = findViewById(R.id.boton_mes_anterior);
        botonMesSiguiente = findViewById(R.id.boton_mes_siguiente);
        grupoChips = findViewById(R.id.grupo_chips);
        listaGastos = findViewById(R.id.lista_gastos);
        textoSinGastos = findViewById(R.id.texto_sin_gastos);
        textoTotal = findViewById(R.id.texto_total);
        botonExportar = findViewById(R.id.boton_exportar);
        barraNavegacion = findViewById(R.id.barra_navegacion);
    }

    private void setupBottomNavigation() {
        barraNavegacion.setSelectedItemId(R.id.nav_historial);
        barraNavegacion.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_inicio) {
                openScreen(DashboardActivity.class);
            } else if (itemId == R.id.nav_config) {
                openScreen(ConfiguracionActivity.class);
            }
            return true;
        });
    }

    private void setupMonthNavigation() {
        botonMesAnterior.setOnClickListener(v -> changeMonth(-1));
        botonMesSiguiente.setOnClickListener(v -> changeMonth(1));
        textoSubtitulo.setOnClickListener(v -> showPeriodOptions());
    }

    // tocar el periodo deja elegir un mes cualquiera, con su ano, o un rango de
    // fechas a mano. Las flechas siguen sirviendo para moverse de mes en mes.
    private void showPeriodOptions() {
        if (activeDialog != null && activeDialog.isShowing()) {
            return;
        }

        String[] options = {
                getString(R.string.periodo_elegir_mes),
                getString(R.string.periodo_elegir_rango)
        };

        activeDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.periodo_titulo)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        askMonth();
                    } else {
                        askRangeStart();
                    }
                })
                .show();
    }

    private void askMonth() {
        // se reutiliza el selector de fecha del sistema y se ignora el dia:
        // asi se elige mes y ano sin escribir un selector propio
        showPicker(visibleMonth, R.string.periodo_elige_mes, (view, year, month, dayOfMonth) -> {
            rangeMode = false;
            visibleMonth.set(year, month, 1);
            loadExpenses();
        });
    }

    private void askRangeStart() {
        showPicker(rangeFrom, R.string.periodo_elige_desde, (view, year, month, dayOfMonth) -> {
            pendingFrom = Calendar.getInstance();
            pendingFrom.set(year, month, dayOfMonth);
            askRangeEnd();
        });
    }

    private void askRangeEnd() {
        // el segundo selector arranca en la fecha final anterior, que la primera
        // vez es hoy: arrancarlo en la fecha inicial hacia que aceptar sin tocar
        // nada diera un rango de un solo dia, que casi nunca es lo que se quiere
        showPicker(rangeTo, R.string.periodo_elige_hasta, (view, year, month, dayOfMonth) -> {
            Calendar pendingTo = Calendar.getInstance();
            pendingTo.set(year, month, dayOfMonth);

            // si el usuario elige el final antes que el principio se intercambian,
            // que es lo que queria decir, en vez de mostrarle una lista vacia
            if (pendingTo.before(pendingFrom)) {
                Calendar swap = pendingFrom;
                pendingFrom = pendingTo;
                pendingTo = swap;
            }

            // el periodo solo se cambia aqui, con las dos fechas ya confirmadas
            rangeFrom = pendingFrom;
            rangeTo = pendingTo;
            rangeMode = true;
            loadExpenses();
        });
    }

    // el aviso dice cual de las dos fechas se esta eligiendo: encadenados, los dos
    // selectores son identicos y sin el no hay forma de saber si toca la inicial
    // o la final
    private void showPicker(Calendar start, int hintRes,
                            DatePickerDialog.OnDateSetListener listener) {
        // se cierra el anterior en vez de no abrir el nuevo: al encadenar el
        // "desde" con el "hasta", el primero todavia no ha terminado de cerrarse
        // y una guarda de "ya hay uno abierto" se comia el segundo
        if (periodPicker != null) {
            periodPicker.dismiss();
        }

        // se cancela el aviso anterior: encadenados, el "1 de 2" seguia en
        // pantalla encima del selector de la fecha final
        if (periodHint != null) {
            periodHint.cancel();
        }
        periodHint = Toast.makeText(this, hintRes, Toast.LENGTH_SHORT);
        periodHint.show();

        periodPicker = new DatePickerDialog(this, listener, start.get(Calendar.YEAR),
                start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH));
        // no se registran gastos en el futuro, asi que tampoco se consultan
        periodPicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        periodPicker.show();
    }

    private void setupList() {
        expenseAdapter = new ExpenseAdapter(expenses, new ExpenseAdapter.OnExpenseActionListener() {
            @Override
            public void onEditExpense(Expense expense) {
                editExpense(expense);
            }

            @Override
            public void onDeleteExpense(Expense expense) {
                confirmDelete(expense);
            }
        });
        listaGastos.setLayoutManager(new LinearLayoutManager(this));
        listaGastos.setAdapter(expenseAdapter);
    }

    private void setupChips() {
        grupoChips.removeAllViews();
        grupoChips.addView(createChip(getString(R.string.chip_todos), 0));

        for (Category category : dbHelper.getAllCategories()) {
            grupoChips.addView(createChip(category.getName(), category.getId()));
        }

        paintChips();
    }

    private void setupExportButton() {
        botonExportar.setOnClickListener(v -> exportSummary());
    }

    private Chip createChip(String label, int categoryId) {
        Chip chip = new Chip(this);
        chip.setText(label);
        chip.setTag(categoryId);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.separacion_chips));
        chip.setLayoutParams(params);

        chip.setOnClickListener(v -> {
            selectedCategoryId = categoryId;
            paintChips();
            loadExpenses();
        });
        return chip;
    }

    private void paintChips() {
        for (int i = 0; i < grupoChips.getChildCount(); i++) {
            Chip chip = (Chip) grupoChips.getChildAt(i);
            boolean isActive = (int) chip.getTag() == selectedCategoryId;

            // el chip inactivo va en verde oscuro y no en un blanco translucido:
            // Material pinta el color sobre la superficie blanca del propio chip,
            // asi que un blanco con alfa terminaba en blanco y el texto desaparecia
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this,
                    isActive ? R.color.blanco : R.color.verde_oscuro)));
            chip.setTextColor(ContextCompat.getColor(this,
                    isActive ? R.color.verde_principal : R.color.blanco));
        }
    }

    private void changeMonth(int months) {
        visibleMonth.add(Calendar.MONTH, months);
        loadExpenses();
    }

    private void loadExpenses() {
        String from = periodFrom();
        String to = periodTo();

        expenses.clear();
        if (selectedCategoryId == 0) {
            expenses.addAll(dbHelper.getExpensesBetween(from, to));
        } else {
            expenses.addAll(dbHelper.getExpensesByCategoryBetween(selectedCategoryId, from, to));
        }
        expenseAdapter.notifyDataSetChanged();

        textoSinGastos.setVisibility(expenses.isEmpty() ? View.VISIBLE : View.GONE);
        textoTotal.setText(CurrencyFormatter.format(totalVisible()));

        // contamos lo que la lista esta mostrando, no todo el mes: si hay un chip
        // activo, el contador y el total del pie tienen que hablar del mismo conjunto
        int recordCount = expenses.size();
        textoSubtitulo.setText(getString(R.string.subtitulo_historial, periodLabel(),
                getResources().getQuantityString(R.plurals.cantidad_registros, recordCount, recordCount)));

        // las flechas solo tienen sentido moviendose de mes en mes; con un rango
        // a mano no hay un "mes siguiente" al que ir
        botonMesAnterior.setVisibility(rangeMode ? View.INVISIBLE : View.VISIBLE);
        botonMesSiguiente.setVisibility(rangeMode || isCurrentMonth()
                ? View.INVISIBLE : View.VISIBLE);
    }

    // el primer y el ultimo dia del periodo que se esta viendo
    private String periodFrom() {
        if (rangeMode) {
            return DateFormatter.forDatabase(rangeFrom.getTime());
        }
        return DateFormatter.forDatabase(visibleMonth.getTime());
    }

    private String periodTo() {
        if (rangeMode) {
            return DateFormatter.forDatabase(rangeTo.getTime());
        }

        Calendar lastDay = (Calendar) visibleMonth.clone();
        lastDay.set(Calendar.DAY_OF_MONTH, lastDay.getActualMaximum(Calendar.DAY_OF_MONTH));
        return DateFormatter.forDatabase(lastDay.getTime());
    }

    private String periodLabel() {
        if (rangeMode) {
            return getString(R.string.periodo_rango,
                    DateFormatter.dayMonthAndYear(rangeFrom.getTime()),
                    DateFormatter.dayMonthAndYear(rangeTo.getTime()));
        }
        return DateFormatter.monthAndYear(visibleMonth.getTime());
    }

    private void editExpense(Expense expense) {
        // dos toques rapidos apilaban dos editores del mismo gasto y el segundo
        // en guardarse pisaba lo que hubiera hecho el primero
        if (openingEditor) {
            return;
        }
        openingEditor = true;

        Intent intent = new Intent(this, RegistroActivity.class);
        intent.putExtra(RegistroActivity.EXTRA_EXPENSE_ID, expense.getId());
        startActivity(intent);
    }

    private boolean isCurrentMonth() {
        Calendar today = Calendar.getInstance();
        return visibleMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && visibleMonth.get(Calendar.MONTH) == today.get(Calendar.MONTH);
    }

    private double totalVisible() {
        double total = 0;
        for (Expense expense : expenses) {
            total = total + expense.getAmount();
        }
        return total;
    }

    private void exportSummary() {
        // sin gastos el resumen saldria con el encabezado y un total en cero,
        // que no le sirve a nadie
        if (expenses.isEmpty()) {
            Toast.makeText(this, R.string.sin_gastos_para_exportar, Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder summary = new StringBuilder();
        summary.append(getString(R.string.asunto_resumen)).append("\n");
        summary.append(periodLabel()).append("\n");

        // con un chip activo el resumen no es el del mes completo, y sin decirlo
        // el que lo recibe lo lee como si lo fuera
        String categoryName = selectedCategoryName();
        if (categoryName != null) {
            summary.append(getString(R.string.resumen_filtrado, categoryName)).append("\n");
        }

        summary.append("\n");

        for (Expense expense : expenses) {
            summary.append(getString(R.string.linea_resumen,
                            DateFormatter.shortFormat(expense.getDate()),
                            buildTitle(expense),
                            CurrencyFormatter.format(expense.getAmount())))
                    .append("\n");
        }

        summary.append("\n").append(getString(R.string.resumen_total,
                CurrencyFormatter.format(totalVisible())));

        // ACTION_SEND deja que el usuario elija SMS, correo o mensajeria,
        // asi la app no necesita el permiso SEND_SMS
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.asunto_resumen));
        intent.putExtra(Intent.EXTRA_TEXT, summary.toString());
        startActivity(Intent.createChooser(intent, getString(R.string.titulo_selector_compartir)));
    }

    // el nombre del filtro se lee del chip activo, que ya lo tiene: asi no hay
    // que preguntarle otra vez a la base ni guardar el nombre por separado
    private String selectedCategoryName() {
        if (selectedCategoryId == 0) {
            return null;
        }

        for (int i = 0; i < grupoChips.getChildCount(); i++) {
            Chip chip = (Chip) grupoChips.getChildAt(i);
            if ((int) chip.getTag() == selectedCategoryId) {
                return chip.getText().toString();
            }
        }

        return null;
    }

    private String buildTitle(Expense expense) {
        String description = expense.getDescription();
        if (description == null || description.trim().isEmpty()) {
            return expense.getCategoryName();
        }
        return description;
    }

    private void confirmDelete(Expense expense) {
        // dos pulsaciones largas seguidas abrian dos dialogos y el campo solo
        // guarda el ultimo: el primero se quedaba sin cerrar al destruirse la pantalla
        if (activeDialog != null && activeDialog.isShowing()) {
            return;
        }

        activeDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_borrar_gasto)
                .setMessage(R.string.mensaje_borrar_gasto)
                .setPositiveButton(R.string.boton_borrar, (dialog, which) -> {
                    dbHelper.deleteExpense(expense.getId());
                    Toast.makeText(this, R.string.gasto_borrado, Toast.LENGTH_SHORT).show();
                    loadExpenses();
                })
                .setNegativeButton(R.string.boton_cancelar, null)
                .show();
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
        if (periodPicker != null) {
            periodPicker.dismiss();
        }
        dbHelper.close();
        super.onDestroy();
    }
}
