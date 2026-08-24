package com.spendlog.app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.spendlog.app.database.DatabaseHelper;
import com.spendlog.app.models.Category;
import com.spendlog.app.utils.CategoryIcon;
import com.spendlog.app.utils.CurrencyFormatter;
import com.spendlog.app.utils.DateFormatter;
import com.spendlog.app.utils.SystemBars;
import com.spendlog.app.views.GraficoCategoriasView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    // los tres estados del banner, ordenados de menos a mas grave: se comparan
    // con > para quedarse con el peor cuando hay varias categorias en juego
    private static final int ALERT_NONE = 0;
    private static final int ALERT_NEAR = 1;
    private static final int ALERT_EXCEEDED = 2;

    // la pantalla de Configuracion promete avisar al superar el 80%
    private static final double WARNING_RATIO = 0.8;

    private TextView textoTotalMes;
    private TextView textoMesActual;
    private TextView textoPresupuesto;
    private TextView textoDisponible;
    private View bannerAlerta;
    private ImageView iconoAlerta;
    private TextView textoAlerta;
    private GraficoCategoriasView graficoCategorias;
    private LinearLayout contenedorCategorias;
    private TextView textoSinGastos;
    private FloatingActionButton botonAgregar;
    private BottomNavigationView barraNavegacion;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);

        bindViews();
        SystemBars.apply(findViewById(R.id.encabezado), barraNavegacion);
        setupBottomNavigation();
        setupFab();
    }

    // recargamos aqui y no en onCreate porque al volver de registrar un gasto
    // la Activity no se recrea, solo se reanuda
    @Override
    protected void onResume() {
        super.onResume();
        barraNavegacion.setSelectedItemId(R.id.nav_inicio);
        loadDashboardData();
    }

    private void bindViews() {
        textoTotalMes = findViewById(R.id.texto_total_mes);
        textoMesActual = findViewById(R.id.texto_mes_actual);
        textoPresupuesto = findViewById(R.id.texto_presupuesto);
        textoDisponible = findViewById(R.id.texto_disponible);
        bannerAlerta = findViewById(R.id.banner_alerta);
        iconoAlerta = findViewById(R.id.icono_alerta);
        textoAlerta = findViewById(R.id.texto_alerta);
        graficoCategorias = findViewById(R.id.grafico_categorias);
        contenedorCategorias = findViewById(R.id.contenedor_categorias);
        textoSinGastos = findViewById(R.id.texto_sin_gastos);
        botonAgregar = findViewById(R.id.boton_agregar);
        barraNavegacion = findViewById(R.id.barra_navegacion);
    }

    private void setupBottomNavigation() {
        barraNavegacion.setSelectedItemId(R.id.nav_inicio);
        barraNavegacion.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_historial) {
                openScreen(HistorialActivity.class);
            } else if (itemId == R.id.nav_config) {
                openScreen(ConfiguracionActivity.class);
            }
            return true;
        });
    }

    private void setupFab() {
        botonAgregar.setOnClickListener(v -> startActivity(new Intent(this, RegistroActivity.class)));
    }

    private void loadDashboardData() {
        Date today = new Date();
        String currentMonth = DateFormatter.monthForDatabase(today);

        double monthTotal = dbHelper.getTotalByMonth(currentMonth);
        textoTotalMes.setText(CurrencyFormatter.format(monthTotal));
        textoMesActual.setText(getString(R.string.subtitulo_dashboard, DateFormatter.monthAndYear(today)));

        textoPresupuesto.setText(CurrencyFormatter.format(dbHelper.getTotalLimits()));

        // el disponible lo calcula loadCategoryRows, que es donde se sabe que
        // gasto pertenece a una categoria con limite y cual no
        loadCategoryRows(currentMonth);
    }

    private void loadCategoryRows(String currentMonth) {
        contenedorCategorias.removeAllViews();

        List<Category> categories = dbHelper.getAllCategories();
        // pedimos los totales del mes de una sola vez: con una consulta por categoria
        // el numero de viajes a la base crecia con cada categoria que el usuario creara
        Map<Integer, Double> spentByCategory = dbHelper.getTotalsByCategoryForMonth(currentMonth);

        // el interruptor de Configuracion apaga las alertas completas y no solo
        // el banner: si el usuario las apago, la lista y el grafico tampoco pueden
        // seguir pintando de amarillo y rojo algo de lo que ya no se avisa
        boolean alertsEnabled = alertsEnabled();

        LayoutInflater inflater = LayoutInflater.from(this);
        int worstState = ALERT_NONE;
        String alertCategory = null;
        boolean hasExpenses = false;

        // vamos juntando lo que se dibuja en la lista para pasarle al grafico
        // exactamente las mismas categorias, sin volver a consultar la base
        List<String> chartNames = new ArrayList<>();
        List<Double> chartAmounts = new ArrayList<>();
        List<Integer> chartStates = new ArrayList<>();

        // solo el gasto de las categorias con limite se descuenta del presupuesto:
        // el presupuesto es la suma de los limites, asi que restarle tambien lo
        // gastado en categorias sin limite dejaba el disponible en negativo sin
        // que ningun limite se hubiera tocado
        double budgetedSpent = 0;

        for (Category category : categories) {
            Double spent = spentByCategory.get(category.getId());
            // una categoria sin gastos este mes no aparece en el resultado agrupado
            if (spent == null || spent <= 0) {
                continue;
            }

            hasExpenses = true;
            chartNames.add(category.getName());
            chartAmounts.add(spent);

            if (category.getMonthlyLimit() > 0) {
                budgetedSpent = budgetedSpent + spent;
            }

            // con las alertas apagadas todas las categorias quedan en neutro,
            // igual que una categoria sin limite definido
            int state = alertsEnabled ? alertStateFor(category, spent) : ALERT_NONE;
            chartStates.add(state);
            // el banner es uno solo, asi que cuando varias categorias disparan
            // alerta a la vez se muestra la mas grave: haber superado el limite
            // pesa mas que estar acercandose. Entre dos categorias del mismo
            // estado gana la primera, que por el orden de la consulta es la mas
            // antigua, y asi el banner no salta de una a otra al recargar.
            if (state > worstState) {
                worstState = state;
                alertCategory = category.getName();
            }

            View row = inflater.inflate(R.layout.item_categoria_dashboard, contenedorCategorias, false);
            fillCategoryRow(row, category, spent, state);
            contenedorCategorias.addView(row);
        }

        // el grafico recibe tres arreglos en paralelo: nombre, monto y color de
        // cada barra, para que una categoria no salga verde en el grafico y roja
        // en la lista de justo debajo
        String[] names = new String[chartNames.size()];
        double[] amounts = new double[chartAmounts.size()];
        int[] colors = new int[chartStates.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = chartNames.get(i);
            amounts[i] = chartAmounts.get(i);
            colors[i] = ContextCompat.getColor(this, alertColor(chartStates.get(i)));
        }
        graficoCategorias.setData(names, amounts, colors);

        double budget = dbHelper.getTotalLimits();
        double available = budget > 0 ? budget - budgetedSpent : 0;
        textoDisponible.setText(CurrencyFormatter.format(available));

        textoSinGastos.setVisibility(hasExpenses ? View.GONE : View.VISIBLE);
        showAlert(worstState, alertCategory);
    }

    // una categoria sin limite definido nunca dispara alerta, porque no hay
    // proporcion que calcular contra nada
    private int alertStateFor(Category category, double spent) {
        if (category.getMonthlyLimit() <= 0) {
            return ALERT_NONE;
        }

        // se divide y se compara contra 0.8 en vez de comparar contra
        // limite * 0.8: multiplicar mueve el resultado unos decimales y un gasto
        // de exactamente el 80% se quedaba fuera del aviso por ese redondeo
        double ratio = spent / category.getMonthlyLimit();

        if (ratio > 1) {
            return ALERT_EXCEEDED;
        }
        if (ratio >= WARNING_RATIO) {
            return ALERT_NEAR;
        }
        return ALERT_NONE;
    }

    private void fillCategoryRow(View row, Category category, double spent, int state) {
        ImageView iconoCategoria = row.findViewById(R.id.icono_categoria);
        TextView textoNombre = row.findViewById(R.id.texto_nombre);
        TextView textoMonto = row.findViewById(R.id.texto_monto);
        ProgressBar barraProgreso = row.findViewById(R.id.barra_progreso);

        iconoCategoria.setImageResource(CategoryIcon.forCategory(category.getName()));
        textoNombre.setText(category.getName());
        textoMonto.setText(CurrencyFormatter.format(spent));

        // sin limite definido no hay proporcion que mostrar, la barra queda en cero
        int progress = 0;
        if (category.getMonthlyLimit() > 0) {
            progress = (int) Math.min(100, spent / category.getMonthlyLimit() * 100);
        }
        barraProgreso.setProgress(progress);

        // la fila usa el mismo color que le corresponderia al banner: si el
        // banner avisa en amarillo, la categoria que lo provoca no puede
        // seguir pintada de verde en la lista de abajo
        int color = ContextCompat.getColor(this, alertColor(state));
        barraProgreso.setProgressTintList(ColorStateList.valueOf(color));
        textoMonto.setTextColor(color);
    }

    private boolean alertsEnabled() {
        return getSharedPreferences(ConfiguracionActivity.PREFS_NAME, MODE_PRIVATE)
                .getBoolean(ConfiguracionActivity.KEY_ALERTS_ENABLED, true);
    }

    private int alertColor(int state) {
        if (state == ALERT_EXCEEDED) {
            return R.color.texto_rojo;
        }
        if (state == ALERT_NEAR) {
            return R.color.alerta_amarillo;
        }
        return R.color.verde_principal;
    }

    // con el interruptor apagado el estado ya llega en ALERT_NONE desde
    // loadCategoryRows, asi que aqui no hay que volver a consultar la preferencia
    private void showAlert(int state, String alertCategory) {
        if (state == ALERT_NONE) {
            bannerAlerta.setVisibility(View.GONE);
            return;
        }

        boolean exceeded = state == ALERT_EXCEEDED;

        // setBackgroundResource reemplaza el relleno del layout por el del
        // drawable nuevo, asi que hay que guardarlo antes y devolverlo despues
        int left = bannerAlerta.getPaddingLeft();
        int top = bannerAlerta.getPaddingTop();
        int right = bannerAlerta.getPaddingRight();
        int bottom = bannerAlerta.getPaddingBottom();

        bannerAlerta.setBackgroundResource(exceeded
                ? R.drawable.fondo_banner_alerta_rojo
                : R.drawable.fondo_banner_alerta);
        bannerAlerta.setPadding(left, top, right, bottom);

        iconoAlerta.setColorFilter(ContextCompat.getColor(this, alertColor(state)));
        textoAlerta.setText(getString(exceeded
                        ? R.string.alerta_limite_superado
                        : R.string.alerta_limite_cerca,
                alertCategory));
        bannerAlerta.setVisibility(View.VISIBLE);
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
        dbHelper.close();
        super.onDestroy();
    }
}
