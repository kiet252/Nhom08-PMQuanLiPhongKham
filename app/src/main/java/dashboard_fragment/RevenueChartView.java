package dashboard_fragment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

public class RevenueChartView extends View {
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String[] labels = new String[0];
    private float[] values = new float[0];

    public RevenueChartView(Context context) {
        super(context);
        init();
    }

    public RevenueChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RevenueChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gridPaint.setColor(Color.parseColor("#DDEAF7"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dp(1));
        gridPaint.setPathEffect(new DashPathEffect(new float[]{dp(4), dp(8)}, 0));

        barPaint.setColor(Color.parseColor("#0D5FA8"));
        barPaint.setStyle(Paint.Style.FILL);

        labelPaint.setColor(Color.parseColor("#64748B"));
        labelPaint.setTextSize(sp(13));
    }

    public void setData(String[] labels, float[] values) {
        this.labels = labels != null ? labels : new String[0];
        this.values = values != null ? values : new float[0];
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int count = Math.min(labels.length, values.length);
        if (count == 0) return;

        float left = dp(46);
        float top = dp(18);
        float right = getWidth() - dp(10);
        float bottom = getHeight() - dp(32);
        float chartHeight = bottom - top;
        float maxValue = getRoundedMaxValue();

        labelPaint.setTextAlign(Paint.Align.RIGHT);
        for (int i = 0; i <= 4; i++) {
            float value = maxValue * (4 - i) / 4f;
            float y = top + chartHeight * i / 4f;
            canvas.drawLine(left, y, right, y, gridPaint);
            canvas.drawText(formatAxisValue(value), left - dp(10), y + dp(5), labelPaint);
        }

        float slotWidth = (right - left) / count;
        float barWidth = Math.min(dp(34), slotWidth * 0.56f);
        float radius = dp(8);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < count; i++) {
            float centerX = left + slotWidth * i + slotWidth / 2f;
            float normalized = Math.max(0f, values[i]) / maxValue;
            float barTop = bottom - chartHeight * normalized;
            RectF rect = new RectF(centerX - barWidth / 2f, barTop, centerX + barWidth / 2f, bottom);
            canvas.drawRoundRect(rect, radius, radius, barPaint);
            canvas.drawText(labels[i], centerX, getHeight() - dp(8), labelPaint);
        }
    }

    private float getRoundedMaxValue() {
        float max = 0f;
        for (float value : values) max = Math.max(max, value);
        if (max <= 0f) return 60f;
        float step = 15f;
        return Math.max(step, (float) Math.ceil(max / step) * step);
    }

    private String formatAxisValue(float value) {
        if (Math.abs(value - Math.round(value)) < 0.01f) {
            return String.valueOf(Math.round(value));
        }
        return String.format(Locale.getDefault(), "%.1f", value);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
