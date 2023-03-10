package com.testntrack.opencvscanner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import org.opencv.core.Point

/**
 * TODO: document your custom view class.
 */
class DocumentDetectorView : View {




    private val points = ArrayList<Point>();

    /**
     * The text to draw
     */


    /**
     * In the example view, this drawable is drawn above the text.
     */
    var exampleDrawable: Drawable? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.DocumentDetectorView, defStyle, 0
        )


        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.


        if (a.hasValue(R.styleable.DocumentDetectorView_exampleDrawable)) {
            exampleDrawable = a.getDrawable(
                R.styleable.DocumentDetectorView_exampleDrawable
            )
            exampleDrawable?.callback = this
        }

        a.recycle()

        // Set up a default TextPaint object


        // Update TextPaint and text measurements from attributes

    }




    fun setPoints(point: List<Point>)
    {
        points.clear();
        points.addAll(point)
        invalidate()


    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paint = Paint()
        paint.strokeWidth = 5f
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND;
        paint.color = resources.getColor(R.color.teal_200)




        if(points.size==4)
        {
            canvas.drawLine(points[0].x.toFloat(),points[0].y.toFloat(),points[1].x.toFloat(),points[1].y.toFloat(),paint);
            canvas.drawLine(points[0].x.toFloat(),points[0].y.toFloat(),points[2].x.toFloat(),points[2].y.toFloat(),paint);
            canvas.drawLine(points[1].x.toFloat(),points[1].y.toFloat(),points[3].x.toFloat(),points[3].y.toFloat(),paint);
            canvas.drawLine(points[2].x.toFloat(),points[2].y.toFloat(),points[3].x.toFloat(),points[3].y.toFloat(),paint);

        }


//        // TODO: consider storing these as member variables to reduce
//        // allocations per draw cycle.
//        val paddingLeft = paddingLeft
//        val paddingTop = paddingTop
//        val paddingRight = paddingRight
//        val paddingBottom = paddingBottom
//
//        val contentWidth = width - paddingLeft - paddingRight
//        val contentHeight = height - paddingTop - paddingBottom
//
//        exampleString?.let {
//            // Draw the text.
//            canvas.drawText(
//                it,
//                paddingLeft + (contentWidth - textWidth) / 2,
//                paddingTop + (contentHeight + textHeight) / 2,
//                textPaint
//            )
//        }
//
//        // Draw the example drawable on top of the text.
//        exampleDrawable?.let {
//            it.setBounds(
//                paddingLeft, paddingTop,
//                paddingLeft + contentWidth, paddingTop + contentHeight
//            )
//            it.draw(canvas)
//        }
    }
}