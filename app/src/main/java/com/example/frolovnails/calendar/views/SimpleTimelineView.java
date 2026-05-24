package com.example.frolovnails.calendar.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.frolovnails.network.models.response.Appointment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SimpleTimelineView extends View {

    private static final int START_HOUR = 8;
    private static final int END_HOUR = 22;
    private static final int HOUR_HEIGHT_DP = 80;
    private static final int LEFT_MARGIN_DP = 50;

    private List<Appointment> appointments = new ArrayList<>();
    private float hourHeight;
    private float leftMargin;
    private int viewWidth;

    private Paint gridPaint;
    private Paint textPaint;
    private Paint eventPaint;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public SimpleTimelineView(Context context) {
        super(context);
        init();
    }

    public SimpleTimelineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        hourHeight = HOUR_HEIGHT_DP * density;
        leftMargin = LEFT_MARGIN_DP * density;

        gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#E0E0E0"));
        gridPaint.setStrokeWidth(1f);

        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#757575"));
        textPaint.setTextSize(12 * density);
        textPaint.setTextAlign(Paint.Align.CENTER);

        eventPaint = new Paint();
        eventPaint.setStyle(Paint.Style.FILL);
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments != null ? appointments : new ArrayList<>();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (viewWidth == 0) return;

        float y = 0;
        for (int hour = START_HOUR; hour <= END_HOUR; hour++) {
            canvas.drawLine(0, y, viewWidth, y, gridPaint);
            String hourText = String.format(Locale.getDefault(), "%02d:00", hour);
            canvas.drawText(hourText, leftMargin / 2, y + hourHeight / 2 + 5, textPaint);
            y += hourHeight;
        }

        for (Appointment apt : appointments) {
            drawAppointment(canvas, apt);
        }
    }

    private void drawAppointment(Canvas canvas, Appointment apt) {
        try {
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(dateFormat.parse(apt.getStartTime()));
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(dateFormat.parse(apt.getEndTime()));

            int startHour = startCal.get(Calendar.HOUR_OF_DAY);
            int startMinute = startCal.get(Calendar.MINUTE);
            int endHour = endCal.get(Calendar.HOUR_OF_DAY);
            int endMinute = endCal.get(Calendar.MINUTE);

            float top = (startHour - START_HOUR) * hourHeight;
            top += (startMinute / 60f) * hourHeight;

            float bottom = (endHour - START_HOUR) * hourHeight;
            bottom += (endMinute / 60f) * hourHeight;

            if (bottom - top < 30) bottom = top + 30;

            RectF rect = new RectF(leftMargin + 4, top + 2, viewWidth - 8, bottom - 2);
            eventPaint.setColor(getColorForStatus(apt.getStatus()));
            canvas.drawRoundRect(rect, 8, 8, eventPaint);

            Paint text = new Paint();
            text.setColor(Color.WHITE);
            text.setTextSize(12 * getResources().getDisplayMetrics().density);
            String title = apt.getClient().getFirstName() + " " + apt.getClient().getLastName();
            canvas.drawText(title, rect.left + 8, rect.top + 24, text);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private int getColorForStatus(Appointment.AppointmentStatus status) {
        switch (status) {
            case CONFIRMED: return Color.parseColor("#4CAF50");
            case PENDING: return Color.parseColor("#FF9800");
            case CREATED: return Color.parseColor("#FF9800");
            case CANCELLED: return Color.parseColor("#F44336");
            case COMPLETED: return Color.parseColor("#2196F3");
            default: return Color.parseColor("#9E9E9E");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int totalHours = END_HOUR - START_HOUR + 1;
        int totalHeight = (int) (totalHours * hourHeight);
        setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY));
    }
}