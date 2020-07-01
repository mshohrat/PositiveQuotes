package com.ms.quokkaism.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout


class MainContentView : ConstraintLayout {


    constructor(context: Context?) : super(context) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context,attrs) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context,attrs,defStyleAttr) {
    }

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context,attrs, defStyleAttr, defStyleRes) {
//    }


    lateinit var paint: Paint
    @ColorInt
    var bgColor : Int = Color.WHITE
    set(value) {
        field = value
        invalidate()
    }
    var radius = 0f
    set(value) {
        field = value
        invalidate()
    }
    var w : Int = 0
    var h : Int = 0

    init {
        initPaintTools()
    }

    private fun initPaintTools() {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.FILL_AND_STROKE
    }

//    override fun onDraw(canvas: Canvas?) {
//        drawBg(canvas)
//        super.onDraw(canvas)
//    }

    override fun dispatchDraw(canvas: Canvas?) {
        drawBg(canvas)
        super.dispatchDraw(canvas)
    }

//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        if (context == null) {
//            return
//        }
//        val minimumWidth = convertDpToPixel(context, 200f).toInt()
//        val minimumHeight = convertDpToPixel(context, 300f).toInt()
//
//        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
//        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
//        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
//        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
//
//        //Measure Width
//        //Measure Width
//        w = if (widthMode == MeasureSpec.EXACTLY) {
//            widthSize
//        } else if (widthMode == MeasureSpec.AT_MOST) {
//            Math.min(minimumWidth, widthSize)
//        } else {
//            minimumWidth
//        }
//
//        //Measure Height
//        //Measure Height
//        h = if (heightMode == MeasureSpec.EXACTLY) {
//            heightSize
//        } else if (heightMode == MeasureSpec.AT_MOST) {
//            Math.min(minimumHeight, heightSize)
//        } else {
//            minimumHeight
//        }
//        setMeasuredDimension(w, h);
//    }

    private fun drawBg(canvas: Canvas?)
    {
        paint.color = bgColor
        val rect = RectF()
        rect.left = 0f
        rect.top = 0f
        rect.right = measuredWidth.toFloat()
        rect.bottom = measuredHeight.toFloat()
        canvas?.drawRoundRect(rect,radius,radius,paint)
    }

}