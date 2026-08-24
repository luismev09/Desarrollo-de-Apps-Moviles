package com.spendlog.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.spendlog.app.R;
import com.spendlog.app.utils.CurrencyFormatter;

// Vista dibujada a mano sobre el Canvas: una barra horizontal por categoria,
// tan larga como su gasto comparado con el de la categoria que mas gasto.
// No usa ninguna libreria de graficos, solo drawRect y drawText.
public class GraficoCategoriasView extends View {

    // los dos arreglos van en paralelo: la posicion i de names corresponde
    // a la posicion i de amounts
    private String[] names = new String[0];
    private double[] amounts = new double[0];
    // color ya resuelto de cada barra, que el dashboard calcula segun lo cerca
    // que este la categoria de su limite
    private int[] colors = new int[0];

    private final TextPaint textPaint = new TextPaint();
    private final Paint barPaint = new Paint();

    private int nameColor;
    private int trackColor;
    private int messageColor;

    // la vista solo se crea desde activity_dashboard.xml, asi que este es el
    // unico constructor que Android necesita: el que recibe los atributos del XML
    public GraficoCategoriasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        prepare();
    }

    private void prepare() {
        textPaint.setAntiAlias(true);
        barPaint.setAntiAlias(true);

        nameColor = ContextCompat.getColor(getContext(), android.R.color.black);
        trackColor = ContextCompat.getColor(getContext(), R.color.gris_claro);
        messageColor = ContextCompat.getColor(getContext(), R.color.gris_texto);
    }

    // recibe lo que el Dashboard ya calculo, para no volver a consultar la base
    public void setData(String[] names, double[] amounts, int[] colors) {
        this.names = names;
        this.amounts = amounts;
        this.colors = colors;
        // el alto depende de cuantas barras hay, asi que hay que volver a medir
        requestLayout();
        invalidate();
    }

    // el ancho lo decide el padre; el alto lo calculamos nosotros segun
    // cuantas categorias haya que dibujar
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);

        int height;
        if (names.length == 0) {
            height = (int) dp(80);
        } else {
            // la ultima barra no lleva la separacion que va entre filas, si no
            // el grafico deja un hueco vacio al final
            height = (int) (dp(6) + (names.length - 1) * dp(46) + rowContentHeight());
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (names.length == 0) {
            drawEmptyMessage(canvas);
            return;
        }

        // la barra mas larga es la de la categoria que mas gasto, y el resto
        // se dibuja en proporcion a ella
        double biggest = 0;
        for (double amount : amounts) {
            if (amount > biggest) {
                biggest = amount;
            }
        }
        if (biggest <= 0) {
            drawEmptyMessage(canvas);
            return;
        }

        float width = getWidth();
        float top = dp(6);

        for (int i = 0; i < names.length; i++) {
            textPaint.setTextSize(sp(13));

            String amountLabel = CurrencyFormatter.format(amounts[i]);
            float amountWidth = textPaint.measureText(amountLabel);

            // el nombre solo puede ocupar lo que sobra despues del monto; si no
            // cabe, se corta con puntos suspensivos en vez de montarse encima
            float nameSpace = width - amountWidth - dp(10);
            if (nameSpace < dp(30)) {
                nameSpace = dp(30);
            }
            String name = TextUtils.ellipsize(names[i], textPaint, nameSpace,
                    TextUtils.TruncateAt.END).toString();

            float baseline = top - textPaint.ascent();

            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setColor(nameColor);
            canvas.drawText(name, 0, baseline, textPaint);

            textPaint.setTextAlign(Paint.Align.RIGHT);
            textPaint.setColor(colors[i]);
            canvas.drawText(amountLabel, width, baseline, textPaint);

            // primero la pista gris de ancho completo y encima el relleno verde
            float barTop = top + sp(13) + dp(7);
            float barBottom = barTop + dp(10);

            barPaint.setColor(trackColor);
            canvas.drawRect(0, barTop, width, barBottom, barPaint);

            float filled = (float) (amounts[i] / biggest) * width;
            // una categoria con muy poco gasto igual tiene que verse
            if (filled < dp(3)) {
                filled = dp(3);
            }
            barPaint.setColor(colors[i]);
            canvas.drawRect(0, barTop, filled, barBottom, barPaint);

            top = top + dp(46);
        }
    }

    private void drawEmptyMessage(Canvas canvas) {
        textPaint.setTextSize(sp(13));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(messageColor);

        // se recorta igual que los nombres de categoria: con el tamano de letra
        // grande del sistema el mensaje no cabe en las pantallas angostas y,
        // al estar centrado, se saldria por los dos lados a la vez
        String message = TextUtils.ellipsize(getContext().getString(R.string.grafico_sin_datos),
                textPaint, getWidth(), TextUtils.TruncateAt.END).toString();
        // centrado en los dos ejes: en x por el Align.CENTER y en y corriendo
        // la mitad del alto del texto para compensar la linea base
        float middle = getHeight() / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(message, getWidth() / 2f, middle, textPaint);
    }

    // lo que ocupa una fila: la linea de texto, la separacion y la barra
    private float rowContentHeight() {
        return sp(13) + dp(7) + dp(10);
    }

    private float dp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }

    private float sp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value,
                getResources().getDisplayMetrics());
    }
}
