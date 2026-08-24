package com.spendlog.app;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.spendlog.app.database.DatabaseHelper;
import com.spendlog.app.models.Category;
import com.spendlog.app.models.Expense;
import com.spendlog.app.utils.DateFormatter;
import com.spendlog.app.utils.SystemBars;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegistroActivity extends AppCompatActivity {

    // el historial manda aqui el id del gasto que se quiere editar; si no viene
    // ninguno, la pantalla trabaja como siempre y crea un gasto nuevo
    public static final String EXTRA_EXPENSE_ID = "expense_id";

    private static final String STATE_SELECTED_DATE = "selected_date";
    private static final String STATE_LATITUDE = "latitude";
    private static final String STATE_LONGITUDE = "longitude";

    // codigo propio para reconocer la respuesta de este permiso y no la de otro
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    // si en este tiempo no llega ninguna posicion dejamos de esperar, para que
    // el mensaje no se quede en "buscando" para siempre
    private static final long LOCATION_TIMEOUT_MS = 15000;

    // La ultima posicion conocida que guarda el sistema puede ser de hace horas,
    // de otro sitio. Un gasto se registra donde ocurre, asi que una lectura vieja
    // ya no responde a "donde se hizo este gasto". Cinco minutos es corto para que
    // la posicion siga siendo del mismo lugar y largo para que un telefono que
    // acaba de ubicarse no tenga que esperar otra lectura.
    private static final long MAX_LOCATION_AGE_MS = 5 * 60 * 1000;

    private TextView textoVolver;
    private TextView textoTitulo;
    private EditText campoMonto;
    private Spinner selectorCategoria;
    private EditText campoDescripcion;
    private TextView textoFecha;
    private TextView textoUbicacion;
    private Button botonUbicacion;
    private Button botonGuardar;

    private DatabaseHelper dbHelper;
    private Calendar selectedDate;
    private DatePickerDialog datePicker;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private final Handler waitHandler = new Handler(Looper.getMainLooper());

    // en null significa que el gasto se va a guardar sin ubicacion
    private Double latitude;
    private Double longitude;

    // cero significa que se esta creando un gasto nuevo
    private int editingExpenseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        dbHelper = new DatabaseHelper(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        selectedDate = Calendar.getInstance();

        // al girar la pantalla la Activity se recrea: sin recuperar la fecha aqui,
        // el formulario conserva el monto pero vuelve a la de hoy sin avisar
        if (savedInstanceState != null) {
            selectedDate.setTimeInMillis(savedInstanceState.getLong(STATE_SELECTED_DATE));

            // lo mismo con la ubicacion ya adjuntada: si no se restaura, girar el
            // telefono la borra y el gasto termina guardandose sin ella
            if (savedInstanceState.containsKey(STATE_LATITUDE)) {
                latitude = savedInstanceState.getDouble(STATE_LATITUDE);
                longitude = savedInstanceState.getDouble(STATE_LONGITUDE);
            }
        }

        editingExpenseId = getIntent().getIntExtra(EXTRA_EXPENSE_ID, 0);

        bindViews();
        SystemBars.apply(findViewById(R.id.encabezado), findViewById(R.id.contenido));
        setupCategorySpinner();

        // los campos se llenan solo la primera vez: al girar la pantalla el
        // sistema ya restaura lo que el usuario tuviera escrito, y volver a
        // cargarlo de la base le borraria los cambios
        if (editingExpenseId != 0) {
            showEditTitles();
            if (savedInstanceState == null) {
                loadExpenseToEdit();
            }
        }

        setupDateField();
        setupButtons();
        showLocationLabel();
    }

    private void showEditTitles() {
        textoTitulo.setText(R.string.titulo_editar_gasto);
        textoVolver.setText(R.string.volver_editar_gasto);
        botonGuardar.setText(R.string.boton_guardar_cambios);
    }

    private void loadExpenseToEdit() {
        Expense expense = dbHelper.getExpenseById(editingExpenseId);

        // si el gasto ya no esta, por ejemplo porque se borro desde el historial,
        // la pantalla vuelve atras en vez de mostrar un formulario vacio que al
        // guardar no actualizaria nada
        if (expense == null) {
            Toast.makeText(this, R.string.error_gasto_no_encontrado, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        campoMonto.setText(String.valueOf(Math.round(expense.getAmount())));
        campoDescripcion.setText(expense.getDescription());
        Date storedDate = DateFormatter.parseStored(expense.getDate());
        if (storedDate != null) {
            selectedDate.setTime(storedDate);
        }
        selectCategory(expense.getCategoryId());

        if (expense.hasLocation()) {
            latitude = expense.getLatitude();
            longitude = expense.getLongitude();
        }
    }

    // el Spinner se posiciona buscando la categoria del gasto entre las que carga
    private void selectCategory(int categoryId) {
        for (int i = 0; i < selectorCategoria.getCount(); i++) {
            Category category = (Category) selectorCategoria.getItemAtPosition(i);
            if (category.getId() == categoryId) {
                selectorCategoria.setSelection(i);
                return;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_SELECTED_DATE, selectedDate.getTimeInMillis());

        if (latitude != null && longitude != null) {
            outState.putDouble(STATE_LATITUDE, latitude);
            outState.putDouble(STATE_LONGITUDE, longitude);
        }
    }

    private void bindViews() {
        textoVolver = findViewById(R.id.texto_volver);
        textoTitulo = findViewById(R.id.texto_titulo);
        campoMonto = findViewById(R.id.campo_monto);
        selectorCategoria = findViewById(R.id.selector_categoria);
        campoDescripcion = findViewById(R.id.campo_descripcion);
        textoFecha = findViewById(R.id.texto_fecha);
        textoUbicacion = findViewById(R.id.texto_ubicacion);
        botonUbicacion = findViewById(R.id.boton_ubicacion);
        botonGuardar = findViewById(R.id.boton_guardar);
    }

    private void setupCategorySpinner() {
        List<Category> categories = dbHelper.getAllCategories();
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectorCategoria.setAdapter(adapter);
    }

    private void setupDateField() {
        showDate();
        textoFecha.setOnClickListener(v -> showDatePicker());
    }

    private void setupButtons() {
        botonGuardar.setOnClickListener(v -> saveExpense());
        botonUbicacion.setOnClickListener(v -> attachLocation());
        textoVolver.setOnClickListener(v -> finish());
    }

    private void showDate() {
        textoFecha.setText(DateFormatter.longFormat(selectedDate.getTime()));
    }

    private void showDatePicker() {
        // dos toques seguidos abrian dos selectores y el campo solo guarda el
        // ultimo: el primero se quedaba sin cerrar al destruirse la pantalla
        if (datePicker != null && datePicker.isShowing()) {
            return;
        }

        datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            showDate();
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));
        // el historial no avanza mas alla del mes en curso, asi que un gasto
        // con fecha futura no se veria en ninguna pantalla
        datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePicker.show();
    }

    // ------------------------------------------------------------------
    // ubicacion: es opcional, asi que ninguna rama de aqui puede impedir
    // que el gasto se guarde
    // ------------------------------------------------------------------

    private void attachLocation() {
        // el permiso se pide aqui, cuando el usuario toca el boton, y no al abrir
        // la pantalla: asi el sistema pregunta cuando se entiende para que
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        readLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != LOCATION_PERMISSION_REQUEST) {
            return;
        }

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            readLocation();
        } else if (hasLocation()) {
            // el gasto que se esta editando ya traia coordenadas: negar el permiso
            // no las borra, asi que la etiqueta tiene que seguir mostrandolas
            Toast.makeText(this, R.string.ubicacion_conservada, Toast.LENGTH_SHORT).show();
            showLocationLabel();
        } else {
            // negarlo no rompe nada: el formulario sigue igual y el gasto se
            // guarda sin coordenadas
            textoUbicacion.setText(R.string.ubicacion_sin_permiso);
        }
    }

    private void readLocation() {
        // se corta aqui y no dentro de startLocationUpdates: si esta busqueda se
        // resuelve por la ultima posicion conocida, la anterior se quedaba viva y
        // su plazo de 15 s acababa pisando la etiqueta con un error que ya no era cierto
        stopLocationUpdates();

        // las coordenadas que ya hubiera no se tocan aqui: editando un gasto son
        // las que trae guardadas, y borrarlas al empezar una busqueda que puede
        // fallar destruiria un dato que ya existia. Solo las reemplaza una
        // lectura nueva de verdad, en showLocation.
        textoUbicacion.setText(R.string.ubicacion_buscando);

        // la ultima posicion conocida contesta de inmediato; solo si el sistema
        // no tiene ninguna guardada hay que quedarse esperando una nueva
        Location known = lastKnownLocation();
        if (known != null) {
            showLocation(known);
            return;
        }

        startLocationUpdates();
    }

    private Location lastKnownLocation() {
        // LocationManager de Android, sin servicios de Google Play de por medio.
        // Se consultan los dos proveedores porque el dispositivo puede tener
        // apagado cualquiera de ellos.
        String[] providers = {LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER};

        for (String provider : providers) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    Location location = locationManager.getLastKnownLocation(provider);
                    // una posicion vieja se descarta y se sigue con el otro
                    // proveedor; si ninguno tiene una reciente, el metodo devuelve
                    // null y readLocation se queda esperando una lectura nueva
                    if (location != null && isFresh(location)) {
                        return location;
                    }
                }
            } catch (SecurityException e) {
                // sin permiso el sistema no entrega nada
            } catch (IllegalArgumentException e) {
                // este dispositivo no tiene ese proveedor
            }
        }

        return null;
    }

    private boolean isFresh(Location location) {
        return System.currentTimeMillis() - location.getTime() <= MAX_LOCATION_AGE_MS;
    }

    private void startLocationUpdates() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // el proveedor puede empezar entregando la misma posicion vieja
                // que ya se habia descartado: si llega asi, se sigue esperando
                if (!isFresh(location)) {
                    return;
                }

                showLocation(location);
                stopLocationUpdates();
            }

            // los tres siguientes no hacen nada, pero hay que escribirlos:
            // en Android 11 pasaron a tener implementacion por defecto y el
            // compilador ya no los exige, pero en Android 8 y 9, que esta app
            // todavia soporta, siguen siendo obligatorios y sin ellos la
            // pantalla revienta cuando el sistema los llama
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }
        };

        String[] providers = {LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER};
        boolean asked = false;

        for (String provider : providers) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
                    asked = true;
                }
            } catch (SecurityException e) {
                // sin permiso no se puede pedir nada
            } catch (IllegalArgumentException e) {
                // este dispositivo no tiene ese proveedor
            }
        }

        if (!asked) {
            showSearchFailed();
            return;
        }

        // la condicion mira si la busqueda sigue abierta y no si ya hay coordenadas:
        // con una ubicacion adjuntada antes, latitude no es null y el plazo no
        // cortaria nada, dejando el escucha registrado y el mensaje congelado
        waitHandler.postDelayed(() -> {
            if (locationListener != null) {
                stopLocationUpdates();
                showSearchFailed();
            }
        }, LOCATION_TIMEOUT_MS);
    }

    private void stopLocationUpdates() {
        waitHandler.removeCallbacksAndMessages(null);

        if (locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException e) {
                // nada que hacer: el sistema ya dejo de mandar posiciones
            }
            locationListener = null;
        }
    }

    // Una busqueda fallida no puede borrar lo que ya habia: si el gasto conserva
    // coordenadas, se avisa y la etiqueta vuelve a mostrarlas, de modo que lo que
    // dice la pantalla y lo que se va a guardar siguen coincidiendo.
    private void showSearchFailed() {
        if (hasLocation()) {
            Toast.makeText(this, R.string.ubicacion_conservada, Toast.LENGTH_SHORT).show();
            showLocationLabel();
            return;
        }

        textoUbicacion.setText(R.string.ubicacion_no_disponible);
    }

    private boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    private void showLocation(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        showLocationLabel();
    }

    private void showLocationLabel() {
        if (!hasLocation()) {
            textoUbicacion.setText(R.string.ubicacion_sin_adjuntar);
            return;
        }

        // Locale.US para que el separador decimal sea el punto y coincida con
        // el formato con el que las coordenadas quedan guardadas
        textoUbicacion.setText(getString(R.string.ubicacion_adjunta,
                String.format(Locale.US, "%.4f", latitude),
                String.format(Locale.US, "%.4f", longitude)));
    }

    // ------------------------------------------------------------------

    private void saveExpense() {
        String amountText = campoMonto.getText().toString().trim();

        if (amountText.isEmpty()) {
            Toast.makeText(this, R.string.error_monto_vacio, Toast.LENGTH_SHORT).show();
            return;
        }

        // el campo es de solo digitos, asi que aqui ya no puede llegar nada que
        // no sea un numero entero. Se dejo asi porque la aplicacion muestra los
        // montos sin decimales y porque en Colombia el punto separa los miles:
        // con el teclado decimal, escribir 50.000 se guardaba como cincuenta pesos
        double amount = Double.parseDouble(amountText);

        if (amount <= 0) {
            Toast.makeText(this, R.string.error_monto_cero, Toast.LENGTH_SHORT).show();
            return;
        }

        // finish() no descarta los toques que ya estaban en cola: sin desactivar el
        // boton, un doble toque rapido guarda el mismo gasto dos o tres veces
        botonGuardar.setEnabled(false);

        Category category = (Category) selectorCategoria.getSelectedItem();
        String description = campoDescripcion.getText().toString().trim();
        String date = DateFormatter.forDatabase(selectedDate.getTime());

        // insert devuelve -1 y update devuelve false si la fila no se pudo
        // escribir; sin mirarlo, la pantalla se cerraria anunciando un gasto
        // que no quedo en ninguna parte
        boolean saved;
        if (editingExpenseId != 0) {
            saved = dbHelper.updateExpense(editingExpenseId, amount, category.getId(),
                    description, date, latitude, longitude);
        } else {
            saved = dbHelper.saveExpense(amount, category.getId(), description, date,
                    latitude, longitude) != -1;
        }

        if (!saved) {
            botonGuardar.setEnabled(true);
            Toast.makeText(this, R.string.error_al_guardar, Toast.LENGTH_SHORT).show();
            return;
        }

        // el sonido va aqui y no antes de las validaciones ni antes del insert:
        // solo suena cuando el gasto quedo realmente guardado
        playSavedSound();

        Toast.makeText(this, editingExpenseId != 0
                ? R.string.gasto_actualizado : R.string.gasto_guardado, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void playSavedSound() {
        // el contexto de la aplicacion y no el de la Activity, porque la pantalla
        // se cierra enseguida y el sonido tiene que terminar de sonar igual
        MediaPlayer player = MediaPlayer.create(getApplicationContext(), R.raw.sonido_guardado);

        if (player == null) {
            // si el sistema no pudo preparar el sonido no pasa nada mas:
            // el gasto ya quedo guardado
            return;
        }

        // liberarlo al terminar evita que cada guardado deje un reproductor
        // ocupando memoria
        player.setOnCompletionListener(mediaPlayer -> mediaPlayer.release());
        player.start();
    }

    @Override
    protected void onDestroy() {
        // al girar la pantalla la Activity muere con el selector todavia abierto
        // y el sistema lo reporta como una ventana filtrada
        if (datePicker != null) {
            datePicker.dismiss();
        }
        stopLocationUpdates();
        dbHelper.close();
        super.onDestroy();
    }
}
