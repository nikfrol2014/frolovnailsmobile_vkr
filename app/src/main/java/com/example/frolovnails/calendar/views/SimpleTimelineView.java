package com.example.frolovnails.calendar.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.frolovnails.R;
import com.example.frolovnails.network.models.response.Appointment;
import com.example.frolovnails.network.models.response.ScheduleBlock;

import java.math.BigDecimal;
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
    private List<ScheduleBlock> blocks = new ArrayList<>();
    private float hourHeight;
    private float leftMargin;
    private int viewWidth;
    private float currentTimeY = -1;

    private Paint gridPaint;
    private Paint textPaint;
    private Paint eventPaint;
    private Paint currentTimePaint;
    private Paint blockPaint;
    private Paint blockTextPaint;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    private Handler handler;
    private Runnable timeUpdater;
    private OnNotesClickListener notesClickListener;
    private OnEventClickListener eventClickListener;

    public interface OnNotesClickListener {
        void onNotesClick(Appointment appointment);
    }

    public interface OnEventClickListener {
        void onEventClick(Appointment appointment);
    }

    public void setOnNotesClickListener(OnNotesClickListener listener) {
        this.notesClickListener = listener;
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.eventClickListener = listener;
    }

    public SimpleTimelineView(Context context) {
        super(context);
        init();
    }

    public SimpleTimelineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // Вместо статических цветов, используем getResources().getColor()

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        hourHeight = HOUR_HEIGHT_DP * density;
        leftMargin = LEFT_MARGIN_DP * density;

        Context context = getContext();

        gridPaint = new Paint();
        gridPaint.setColor(getColor(context, R.color.divider));
        gridPaint.setStrokeWidth(1f);

        textPaint = new Paint();
        textPaint.setColor(getColor(context, R.color.text_secondary));
        textPaint.setTextSize(12 * density);
        textPaint.setTextAlign(Paint.Align.CENTER);

        eventPaint = new Paint();
        eventPaint.setStyle(Paint.Style.FILL);

        currentTimePaint = new Paint();
        currentTimePaint.setColor(getColor(context, R.color.status_cancelled));
        currentTimePaint.setStrokeWidth(2f);

        blockPaint = new Paint();
        blockPaint.setColor(ContextCompat.getColor(context, R.color.status_cancelled));
        blockPaint.setAlpha(100);
        blockPaint.setStyle(Paint.Style.FILL);

        blockTextPaint = new Paint();
        blockTextPaint.setColor(getColor(context, R.color.white));
        blockTextPaint.setTextSize(12 * density);

        startCurrentTimeUpdater();
    }

    private int getColor(Context context, int colorRes) {
        return ContextCompat.getColor(context, colorRes);
    }

    private void startCurrentTimeUpdater() {
        handler = new Handler();
        timeUpdater = () -> {
            updateCurrentTimeLine();
            invalidate();
            handler.postDelayed(timeUpdater, 60000);
        };
        handler.post(timeUpdater);
    }

    private void updateCurrentTimeLine() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        if (hour >= START_HOUR && hour <= END_HOUR) {
            float y = (hour - START_HOUR) * hourHeight;
            y += (minute / 60f) * hourHeight;
            currentTimeY = y;
        } else {
            currentTimeY = -1;
        }
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments != null ? appointments : new ArrayList<>();
        invalidate();
    }

    public void setBlocks(List<ScheduleBlock> blocks) {
        this.blocks = blocks != null ? blocks : new ArrayList<>();
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

        // Блокировки
        for (ScheduleBlock block : blocks) {
            drawBlock(canvas, block);
        }

        // Записи
        for (Appointment apt : appointments) {
            drawAppointment(canvas, apt);
        }

        // Линия текущего времени
        if (currentTimeY >= 0 && currentTimeY < getHeight()) {
            canvas.drawLine(0, currentTimeY, viewWidth, currentTimeY, currentTimePaint);
            Paint timeTextPaint = new Paint();
            timeTextPaint.setColor(Color.RED);
            timeTextPaint.setTextSize(12 * getResources().getDisplayMetrics().density);
            canvas.drawText("● Сейчас", leftMargin + 8, currentTimeY - 8, timeTextPaint);
        }
    }

    private void drawBlock(Canvas canvas, ScheduleBlock block) {
        try {
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(dateFormat.parse(block.getStartTime()));
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(dateFormat.parse(block.getEndTime()));

            int startHour = startCal.get(Calendar.HOUR_OF_DAY);
            int startMinute = startCal.get(Calendar.MINUTE);
            int endHour = endCal.get(Calendar.HOUR_OF_DAY);
            int endMinute = endCal.get(Calendar.MINUTE);

            float top = (startHour - START_HOUR) * hourHeight;
            top += (startMinute / 60f) * hourHeight;

            float bottom = (endHour - START_HOUR) * hourHeight;
            bottom += (endMinute / 60f) * hourHeight;

            RectF rect = new RectF(leftMargin + 4, top + 2, viewWidth - 8, bottom - 2);
            canvas.drawRoundRect(rect, 8, 8, blockPaint);

            String blockText = "🚫 " + (block.getReason() != null ? block.getReason() : "Заблокировано");
            canvas.drawText(blockText, rect.left + 8, rect.top + 24, blockTextPaint);

        } catch (ParseException e) {
            e.printStackTrace();
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

            if (bottom - top < 95) bottom = top + 95;

            RectF rect = new RectF(leftMargin + 4, top + 2, viewWidth - 8, bottom - 2);

            eventPaint.setColor(getBackgroundColorForStatus(apt.getStatus().toString()));
            canvas.drawRoundRect(rect, 8, 8, eventPaint);

            eventPaint.setColor(getBorderColorForStatus(apt.getStatus().toString()));
            eventPaint.setStyle(Paint.Style.STROKE);
            eventPaint.setStrokeWidth(2f);
            canvas.drawRoundRect(rect, 8, 8, eventPaint);
            eventPaint.setStyle(Paint.Style.FILL);

            Paint textPaint = new Paint();
            textPaint.setColor(Color.WHITE);

            float density = getResources().getDisplayMetrics().density;
            float lineHeight = 20 * density;
            float x = rect.left + 8;
            float y = rect.top + lineHeight;

            textPaint.setTextSize(14 * density);
            textPaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
            String statusText = getStatusText(apt.getStatus().toString());
            String name = apt.getClient().getFirstName() + " " + apt.getClient().getLastName() + " [" + statusText + "]";
            canvas.drawText(name, x, y, textPaint);

            textPaint.setTextSize(12 * density);
            textPaint.setTypeface(android.graphics.Typeface.DEFAULT);
            String time = String.format(Locale.getDefault(), "%02d:%02d - %02d:%02d", startHour, startMinute, endHour, endMinute);
            y += lineHeight;
            canvas.drawText(time, x, y, textPaint);

            y += lineHeight;
            String serviceText = apt.getService().getName() + " (" + apt.getService().getCategory() + ")";
            canvas.drawText(serviceText, x, y, textPaint);

            y += lineHeight;
            textPaint.setColor(Color.parseColor("#FFD700"));
            // Отображаем фактическую цену, если есть
            BigDecimal actualPrice = apt.getActualPrice();
            BigDecimal displayPrice = actualPrice != null ? actualPrice : apt.getService().getPrice();
            String priceText = displayPrice + " ₽ • " + apt.getService().getDurationMinutes() + " мин";
            if (actualPrice != null) {
                priceText += " (было " + apt.getService().getPrice() + " ₽)";
            }
            canvas.drawText(priceText, x, y, textPaint);

            // Кнопка "📝" справа, по центру по вертикали
            float buttonSize = 32 * density;
            float rectCenterY = rect.centerY();
            float buttonX = rect.right - buttonSize - 8;
            float buttonY = rectCenterY - buttonSize / 2;
            RectF buttonRect = new RectF(buttonX, buttonY, buttonX + buttonSize, buttonY + buttonSize);

            // Фон кнопки
            Paint buttonBgPaint = new Paint();
            buttonBgPaint.setColor(Color.parseColor("#444444"));
            buttonBgPaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(buttonRect, 8, 8, buttonBgPaint);

            // Рамка кнопки
            Paint buttonBorderPaint = new Paint();
            buttonBorderPaint.setColor(Color.parseColor("#FFD700"));
            buttonBorderPaint.setStyle(Paint.Style.STROKE);
            buttonBorderPaint.setStrokeWidth(1.5f);
            canvas.drawRoundRect(buttonRect, 8, 8, buttonBorderPaint);

            // Текст кнопки
            Paint buttonTextPaint = new Paint();
            buttonTextPaint.setColor(Color.parseColor("#FFD700"));
            buttonTextPaint.setTextSize(18 * density);
            buttonTextPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("📝", buttonX + buttonSize / 2, buttonY + buttonSize - 10, buttonTextPaint);

            // Заметки мастера
            if (apt.getMasterNotes() != null && !apt.getMasterNotes().isEmpty()) {
                y += lineHeight;
                textPaint.setColor(Color.parseColor("#CCCCCC"));
                textPaint.setTextSize(11 * density);
                String notesPreview = apt.getMasterNotes().length() > 30
                        ? apt.getMasterNotes().substring(0, 30) + "..."
                        : apt.getMasterNotes();
                canvas.drawText("📝 " + notesPreview, x, y, textPaint);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();

            for (Appointment apt : appointments) {
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

                    if (bottom - top < 95) bottom = top + 95;

                    float density = getResources().getDisplayMetrics().density;
                    float buttonSize = 32 * density;
                    float rectCenterY = top + (bottom - top) / 2;
                    float buttonX = (viewWidth - 8) - buttonSize - 8;
                    float buttonY = rectCenterY - buttonSize / 2;

                    // Проверяем клик по кнопке заметок
                    if (x >= buttonX && x <= buttonX + buttonSize &&
                            y >= buttonY && y <= buttonY + buttonSize) {
                        if (notesClickListener != null) {
                            notesClickListener.onNotesClick(apt);
                        }
                        return true;
                    }

                    // Проверяем клик по самой записи
                    if (x >= leftMargin && x <= viewWidth - 8 &&
                            y >= top && y <= bottom) {
                        if (eventClickListener != null) {
                            eventClickListener.onEventClick(apt);
                        }
                        return true;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private int getBackgroundColorForStatus(String status) {
        Context context = getContext();
        switch (status) {
            case "CONFIRMED": return ContextCompat.getColor(context, R.color.status_confirmed);
            case "PENDING": return ContextCompat.getColor(context, R.color.status_pending);
            case "CREATED": return ContextCompat.getColor(context, R.color.status_created);
            case "CANCELLED": return ContextCompat.getColor(context, R.color.status_cancelled);
            case "COMPLETED": return ContextCompat.getColor(context, R.color.status_completed);
            default: return ContextCompat.getColor(context, R.color.text_disabled);
        }
    }

    private int getBorderColorForStatus(String status) {
        // Более темный оттенок для рамки
        Context context = getContext();
        switch (status) {
            case "CONFIRMED": return ContextCompat.getColor(context, R.color.primary_dark);
            case "PENDING": return ContextCompat.getColor(context, R.color.secondary_dark);
            case "CREATED": return ContextCompat.getColor(context, R.color.secondary_dark);
            case "CANCELLED": return ContextCompat.getColor(context, R.color.status_cancelled);
            case "COMPLETED": return ContextCompat.getColor(context, R.color.accent);
            default: return ContextCompat.getColor(context, R.color.text_disabled);
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "CONFIRMED": return "Подтверждено";
            case "PENDING": return "Ожидание";
            case "CREATED": return "Создано";
            case "CANCELLED": return "Отменено";
            case "COMPLETED": return "Выполнено";
            default: return status;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int totalHours = END_HOUR - START_HOUR + 1;
        int totalHeight = (int) (totalHours * hourHeight);
        setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (handler != null) {
            handler.removeCallbacks(timeUpdater);
        }
    }
}