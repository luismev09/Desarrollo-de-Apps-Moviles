package com.spendlog.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.spendlog.app.R;
import com.spendlog.app.models.Category;
import com.spendlog.app.models.Expense;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SpendLog.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_CATEGORIES = "categories";
    private static final String TABLE_EXPENSES = "expenses";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_MONTHLY_LIMIT = "monthly_limit";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_CATEGORY_ID = "category_id";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";

    // traemos el nombre de la categoria en la misma consulta para no lanzar
    // una consulta extra por cada fila del RecyclerView
    private static final String SELECT_EXPENSES =
            "SELECT e._id, e.amount, e.category_id, e.description, e.date, c.name, " +
                    "e.latitude, e.longitude " +
                    "FROM expenses e INNER JOIN categories c ON e.category_id = c._id ";

    // el segundo criterio desempata los gastos del mismo dia
    private static final String ORDER_EXPENSES = "ORDER BY e.date DESC, e._id DESC";

    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // categories va primero porque expenses la referencia
        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_MONTHLY_LIMIT + " REAL DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_EXPENSES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_AMOUNT + " REAL NOT NULL, " +
                COLUMN_CATEGORY_ID + " INTEGER NOT NULL, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_DATE + " TEXT NOT NULL, " +
                // van sin NOT NULL porque adjuntar la ubicacion es opcional
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL)");

        insertSeedCategories(db);
    }

    // ALTER TABLE y no DROP TABLE: borrar las tablas para recrearlas se lleva
    // por delante los gastos que el usuario ya tenia registrados
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_EXPENSES + " ADD COLUMN " + COLUMN_LATITUDE + " REAL");
            db.execSQL("ALTER TABLE " + TABLE_EXPENSES + " ADD COLUMN " + COLUMN_LONGITUDE + " REAL");
        }
    }

    private void insertSeedCategories(SQLiteDatabase db) {
        int[] seedNames = {
                R.string.categoria_transporte,
                R.string.categoria_insumos,
                R.string.categoria_arriendo,
                R.string.categoria_servicios,
                R.string.categoria_otros
        };

        for (int seedName : seedNames) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, context.getString(seedName));
            values.put(COLUMN_MONTHLY_LIMIT, 0);
            db.insert(TABLE_CATEGORIES, null, values);
        }
    }

    // latitude y longitude llegan en null cuando el gasto se guarda sin ubicacion,
    // que es lo que pasa si el usuario niega el permiso o no toca el boton
    public long saveExpense(double amount, int categoryId, String description, String date,
                            Double latitude, Double longitude) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_CATEGORY_ID, categoryId);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);

        return db.insert(TABLE_EXPENSES, null, values);
    }

    // Las dos consultas del historial van por rango de fechas. Como la fecha se
    // guarda en formato ano-mes-dia, BETWEEN sobre el texto compara igual que
    // sobre fechas, asi que un mes es simplemente el rango de su dia 1 a su
    // ultimo dia y no hacen falta dos caminos distintos.
    public List<Expense> getExpensesBetween(String fromDate, String toDate) {
        return readExpenses(SELECT_EXPENSES + "WHERE e.date BETWEEN ? AND ? " + ORDER_EXPENSES,
                new String[]{fromDate, toDate});
    }

    public List<Expense> getExpensesByCategoryBetween(int categoryId, String fromDate, String toDate) {
        return readExpenses(
                SELECT_EXPENSES + "WHERE e.category_id = ? AND e.date BETWEEN ? AND ? "
                        + ORDER_EXPENSES,
                new String[]{String.valueOf(categoryId), fromDate, toDate});
    }

    // la pantalla de registro la usa para cargar el gasto que se va a editar
    public Expense getExpenseById(int expenseId) {
        List<Expense> found = readExpenses(SELECT_EXPENSES + "WHERE e._id = ?",
                new String[]{String.valueOf(expenseId)});
        return found.isEmpty() ? null : found.get(0);
    }

    // misma forma que saveExpense, pero sobre una fila que ya existe
    public boolean updateExpense(int expenseId, double amount, int categoryId, String description,
                                 String date, Double latitude, Double longitude) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_CATEGORY_ID, categoryId);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);

        int updated = db.update(TABLE_EXPENSES, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(expenseId)});
        return updated > 0;
    }

    public boolean deleteExpense(int expenseId) {
        SQLiteDatabase db = getWritableDatabase();
        int deleted = db.delete(TABLE_EXPENSES, COLUMN_ID + " = ?",
                new String[]{String.valueOf(expenseId)});
        return deleted > 0;
    }

    public double getTotalByMonth(String month) {
        return readTotal("SELECT SUM(amount) FROM expenses WHERE date LIKE ?",
                new String[]{month + "%"});
    }

    // el dashboard necesita el gasto de todas las categorias a la vez, asi que lo
    // resolvemos con un GROUP BY en una sola consulta en vez de preguntar de a una
    public Map<Integer, Double> getTotalsByCategoryForMonth(String month) {
        Map<Integer, Double> totals = new HashMap<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT category_id, SUM(amount) FROM expenses WHERE date LIKE ? GROUP BY category_id",
                new String[]{month + "%"});

        while (cursor.moveToNext()) {
            totals.put(cursor.getInt(0), cursor.getDouble(1));
        }

        cursor.close();
        return totals;
    }

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_MONTHLY_LIMIT},
                null, null, null, null, COLUMN_ID + " ASC");

        while (cursor.moveToNext()) {
            categories.add(new Category(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2)));
        }

        cursor.close();
        return categories;
    }

    public long saveCategory(String name, double monthlyLimit) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_MONTHLY_LIMIT, monthlyLimit);

        return db.insert(TABLE_CATEGORIES, null, values);
    }

    public boolean updateCategoryLimit(int categoryId, double newLimit) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_MONTHLY_LIMIT, newLimit);

        int updated = db.update(TABLE_CATEGORIES, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(categoryId)});
        return updated > 0;
    }

    public double getTotalLimits() {
        return readTotal("SELECT SUM(monthly_limit) FROM categories", null);
    }

    private List<Expense> readExpenses(String sql, String[] selectionArgs) {
        List<Expense> expenses = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);

        while (cursor.moveToNext()) {
            Expense expense = new Expense(
                    cursor.getInt(0),
                    cursor.getDouble(1),
                    cursor.getInt(2),
                    cursor.getString(5),
                    cursor.getString(3),
                    cursor.getString(4));

            // los gastos guardados antes de la version 2 del esquema tienen
            // las dos columnas en null y se quedan sin ubicacion
            if (!cursor.isNull(6) && !cursor.isNull(7)) {
                expense.setLatitude(cursor.getDouble(6));
                expense.setLongitude(cursor.getDouble(7));
            }

            expenses.add(expense);
        }

        cursor.close();
        return expenses;
    }

    private double readTotal(String sql, String[] selectionArgs) {
        double total = 0;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);

        // SUM devuelve null si no hay filas, no cero
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        return total;
    }
}
