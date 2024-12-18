package com.proove.smart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.proove.smart.R;

public class BatteryProgressView extends View {
    private Paint paint;
    private RectF rectF;
    private float progress = 70f;
    private float strokeWidth = 40; // 改小了默认笔画宽度，从20dp改为8dp
    // 进度条颜色
// 获取背景颜色资源
    private int progressColor = ContextCompat.getColor(getContext(), R.color.but_battery_status_color);

    // 获取轨道颜色资源
    private int trackColor = ContextCompat.getColor(getContext(), R.color.pro_bg_color);

    public BatteryProgressView(Context context) {
        super(context);
        init();
    }

    public BatteryProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BatteryProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectF = new RectF();
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        // 设置画笔样式
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);

        // 计算圆弧的矩形范围
        float padding = strokeWidth / 2;
        rectF.set(padding, padding, width - padding, height - padding);

        // 绘制背景轨道
        paint.setColor(trackColor);
        canvas.drawArc(rectF, 190f, 160f, false, paint);

        // 绘制进度
        paint.setColor(progressColor);
        float sweepAngle = progress * 160f / 100f;
        canvas.drawArc(rectF, 190, sweepAngle, false, paint);
    }
}