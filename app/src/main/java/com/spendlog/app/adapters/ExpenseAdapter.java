package com.spendlog.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.spendlog.app.R;
import com.spendlog.app.models.Expense;
import com.spendlog.app.utils.CategoryIcon;
import com.spendlog.app.utils.CurrencyFormatter;
import com.spendlog.app.utils.DateFormatter;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    public interface OnExpenseActionListener {
        void onEditExpense(Expense expense);

        void onDeleteExpense(Expense expense);
    }

    private final List<Expense> expenses;
    private final OnExpenseActionListener actionListener;

    public ExpenseAdapter(List<Expense> expenses, OnExpenseActionListener actionListener) {
        this.expenses = expenses;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gasto, parent, false);
        return new ExpenseViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        View row = holder.itemView;

        holder.iconoCategoria.setImageResource(CategoryIcon.forCategory(expense.getCategoryName()));
        holder.textoTitulo.setText(buildTitle(expense));
        holder.textoDetalle.setText(row.getContext().getString(R.string.detalle_gasto,
                DateFormatter.shortFormat(expense.getDate()), expense.getCategoryName()));
        holder.textoMonto.setText(row.getContext().getString(R.string.monto_negativo,
                CurrencyFormatter.format(expense.getAmount())));

        // el pin marca los gastos que se guardaron con ubicacion adjunta
        holder.iconoUbicacion.setVisibility(expense.hasLocation() ? View.VISIBLE : View.GONE);

        // tocar la fila abre el gasto para editarlo
        row.setOnClickListener(v -> actionListener.onEditExpense(expense));
        holder.botonBorrar.setOnClickListener(v -> actionListener.onDeleteExpense(expense));

        // se conserva el mantener pulsado porque ya funcionaba asi
        row.setOnLongClickListener(v -> {
            actionListener.onDeleteExpense(expense);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    // un gasto sin descripcion se titula con el nombre de su categoria
    private String buildTitle(Expense expense) {
        String description = expense.getDescription();
        if (description == null || description.trim().isEmpty()) {
            return expense.getCategoryName();
        }
        return description;
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {

        final ImageView iconoCategoria;
        final ImageView iconoUbicacion;
        final ImageView botonBorrar;
        final TextView textoTitulo;
        final TextView textoDetalle;
        final TextView textoMonto;

        ExpenseViewHolder(View row) {
            super(row);
            iconoCategoria = row.findViewById(R.id.icono_categoria);
            iconoUbicacion = row.findViewById(R.id.icono_ubicacion);
            botonBorrar = row.findViewById(R.id.boton_borrar);
            textoTitulo = row.findViewById(R.id.texto_titulo);
            textoDetalle = row.findViewById(R.id.texto_detalle);
            textoMonto = row.findViewById(R.id.texto_monto);
        }
    }
}
