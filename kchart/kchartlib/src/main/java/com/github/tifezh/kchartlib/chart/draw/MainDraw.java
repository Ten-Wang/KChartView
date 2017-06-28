package com.github.tifezh.kchartlib.chart.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.github.tifezh.kchartlib.R;
import com.github.tifezh.kchartlib.chart.EntityImpl.CandleImpl;
import com.github.tifezh.kchartlib.chart.EntityImpl.KLineImpl;
import com.github.tifezh.kchartlib.chart.impl.IChartDraw;
import com.github.tifezh.kchartlib.chart.impl.IKChartView;
import com.github.tifezh.kchartlib.utils.ViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 主图的实现类
 * Created by tifezh on 2016/6/14.
 */

public class MainDraw implements IChartDraw<CandleImpl>{

    private float mCandleWidth = 0;
    private float mCandleLineWidth = 0;
    private Paint mRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mGreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma5Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma10Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma20Paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Context mContext;
    private Paint mSelectorTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mSelectorBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 构造方法
     * @param context
     */
    public MainDraw(Context context) {
        this.mContext=context;
        mSelectorTextPaint.setColor(ContextCompat.getColor(context,R.color.chart_text));
        mSelectorTextPaint.setTextSize(context.getResources().getDimension(R.dimen.chart_selector_text_size));
        mSelectorBackgroundPaint.setColor(ContextCompat.getColor(context,R.color.chart_selector));
        mSelectorBackgroundPaint.setAlpha(200);

        mCandleWidth = context.getResources().getDimension(R.dimen.chart_candle_width);
        mCandleLineWidth = context.getResources().getDimension(R.dimen.chart_candle_line_width);
        float lineWidth = context.getResources().getDimension(R.dimen.chart_line_width);
        float textSize = context.getResources().getDimension(R.dimen.chart_text_size);
        mRedPaint.setColor(ContextCompat.getColor(context,R.color.chart_red));
        mGreenPaint.setColor(ContextCompat.getColor(context,R.color.chart_green));

        ma5Paint.setColor(ContextCompat.getColor(context,R.color.chart_ma5));
        ma5Paint.setStrokeWidth(lineWidth);
        ma5Paint.setTextSize(textSize);

        ma10Paint.setColor(ContextCompat.getColor(context,R.color.chart_ma10));
        ma10Paint.setStrokeWidth(lineWidth);
        ma10Paint.setTextSize(textSize);

        ma20Paint.setColor(ContextCompat.getColor(context,R.color.chart_ma20));
        ma20Paint.setStrokeWidth(lineWidth);
        ma20Paint.setTextSize(textSize);
    }

    @Override
    public void drawTranslated(@Nullable CandleImpl lastPoint, @NonNull CandleImpl curPoint, float lastX, float curX, @NonNull Canvas canvas, @NonNull IKChartView view, int position) {
        drawCandle(view, canvas, curX, curPoint.getHighPrice(), curPoint.getLowPrice(), curPoint.getOpenPrice(), curPoint.getClosePrice());
        //画ma5
        if (lastPoint.getMA5Price() != 0) {
            view.drawMainLine(canvas, ma5Paint, lastX, lastPoint.getMA5Price(), curX, curPoint.getMA5Price());
        }
        //画ma10
        if (lastPoint.getMA10Price() != 0) {
            view.drawMainLine(canvas, ma10Paint, lastX, lastPoint.getMA10Price(), curX, curPoint.getMA10Price());
        }
        //画ma20
        if (lastPoint.getMA20Price() != 0) {
            view.drawMainLine(canvas, ma20Paint, lastX, lastPoint.getMA20Price(), curX, curPoint.getMA20Price());
        }
    }

    @Override
    public void drawText(@NonNull Canvas canvas, @NonNull IKChartView view, int position, float x, float y) {
        CandleImpl point = (KLineImpl) view.getItem(position);
        String text = "MA5:" + view.formatValue(point.getMA5Price()) + " ";
        canvas.drawText(text, x, y, ma5Paint);
        x += ma5Paint.measureText(text);
        text = "MA10:" + view.formatValue(point.getMA10Price()) + " ";
        canvas.drawText(text, x, y, ma10Paint);
        x += ma10Paint.measureText(text);
        text = "MA20:" + view.formatValue(point.getMA20Price()) + " ";
        canvas.drawText(text, x, y, ma20Paint);
        if (view.isLongPress()) {
            drawSelector(view, canvas);
        }
    }

    @Override
    public float getMaxValue(CandleImpl point) {
        return Math.max(point.getHighPrice(), point.getMA20Price());
    }

    @Override
    public float getMinValue(CandleImpl point) {
        return Math.min(point.getMA20Price(), point.getLowPrice());
    }

    /**
     * 画Candle
     * @param canvas
     * @param x      x轴坐标
     * @param high   最高价
     * @param low    最低价
     * @param open   开盘价
     * @param close  收盘价
     */
    private void drawCandle(IKChartView view, Canvas canvas, float x, float high, float low, float open, float close) {
        high = view.getMainY(high);
        low = view.getMainY(low);
        open = view.getMainY(open);
        close = view.getMainY(close);
        float r = mCandleWidth / 2;
        float lineR = mCandleLineWidth / 2;
        if (open > close) {
            //实心
            canvas.drawRect(x - r, close, x + r, open, mRedPaint);
            canvas.drawRect(x - lineR, high, x + lineR, low, mRedPaint);
        } else if (open < close) {
            canvas.drawRect(x - r, open, x + r, close, mGreenPaint);
            canvas.drawRect(x - lineR, high, x + lineR, low, mGreenPaint);
        } else {
            canvas.drawRect(x - r, open, x + r, close + 1, mRedPaint);
            canvas.drawRect(x - lineR, high, x + lineR, low, mRedPaint);
        }
    }

    /**
     * draw选择器
     * @param view
     * @param canvas
     */
    private void drawSelector(IKChartView view, Canvas canvas) {
        Paint.FontMetrics metrics = mSelectorTextPaint.getFontMetrics();
        float textHeight = metrics.descent - metrics.ascent;

        int index = view.getSelectedIndex();
        float padding = ViewUtil.Dp2Px(mContext, 5);
        float margin = ViewUtil.Dp2Px(mContext, 5);
        float width = 0;
        float left;
        float top = margin+view.getTopPadding();
        float height = padding * 8 + textHeight * 5;

        CandleImpl point = (CandleImpl) view.getItem(index);
        List<String> strings = new ArrayList<>();
        strings.add(view.formatDateTime(view.getAdapter().getDate(index)));
        strings.add("高:" + point.getHighPrice());
        strings.add("低:" + point.getLowPrice());
        strings.add("开:" + point.getOpenPrice());
        strings.add("收:" + point.getClosePrice());

        for (String s : strings) {
            width = Math.max(width, mSelectorTextPaint.measureText(s));
        }
        width += padding * 2;

        float x = view.translateXtoX(view.getX(index));
        if (x > view.getChartWidth() / 2) {
            left = margin;
        } else {
            left = view.getChartWidth() - width - margin;
        }

        RectF r = new RectF(left, top, left + width, top + height);
        canvas.drawRoundRect(r, padding, padding, mSelectorBackgroundPaint);
        float y = top + padding * 2 + (textHeight - metrics.bottom - metrics.top) / 2;

        for (String s : strings) {
            canvas.drawText(s, left + padding, y, mSelectorTextPaint);
            y += textHeight + padding;
        }

    }

    /**
     * 设置蜡烛宽度
     * @param candleWidth
     */
    public void setCandleWidth(float candleWidth) {
        mCandleWidth = candleWidth;
    }

    /**
     * 设置蜡烛线宽度
     * @param candleLineWidth
     */
    public void setCandleLineWidth(float candleLineWidth) {
        mCandleLineWidth = candleLineWidth;
    }

    /**
     * 设置ma5颜色
     * @param color
     */
    public void setMa5Color(int color) {
        this.ma5Paint.setColor(color);
    }

    /**
     * 设置ma10颜色
     * @param color
     */
    public void setMa10Color(int color) {
        this.ma10Paint.setColor(color);
    }

    /**
     * 设置ma20颜色
     * @param color
     */
    public void setMa20Color(int color) {
        this.ma20Paint.setColor(color);
    }

    /**
     * 设置选择器文字颜色
     * @param color
     */
    public void setSelectorTextColor(int color) {
        mSelectorTextPaint.setColor(color);
    }

    /**
     * 设置选择器背景
     * @param color
     * @param alpha 透明度
     */
    public void setSelectorBackgroundColor(int color,int alpha) {
        mSelectorBackgroundPaint.setColor(color);
        mSelectorBackgroundPaint.setAlpha(alpha);
    }
}
