package com.ms.quokkaism.view

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

class RoundedDrawable: Drawable() {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    @ColorInt
    var bgColor : Int = Color.WHITE
        set(value) {
            field = value
            invalidateSelf()
        }

    var radius = 0f
        set(value) {
            field = value
            invalidateSelf()
        }

    init {
        paint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        paint.color = bgColor
        canvas.drawRoundRect(RectF(bounds),radius,radius,paint)
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(filter: ColorFilter?) {
        paint.colorFilter = filter
    }

    override fun onLevelChange(level: Int): Boolean {
        invalidateSelf()
        return true
    }

//    fun animateAndSetRadius(newRadius: Float) {
//        val animator = ValueAnimator.ofFloat(radius,newRadius)
//        animator.addUpdateListener {
//            val value = it.animatedValue as Float
//            radius = value
//        }
//        animator.duration = 400
//
//    }
}