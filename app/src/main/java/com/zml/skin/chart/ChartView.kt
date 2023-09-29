package com.zml.skin.chart


import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.*
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator

import com.zml.skin.R
import kotlin.math.ceil

//zml


class ChartView  : View {

    fun getPixels(dipValue: Int, context: Context): Int {
        val r: Resources = context.resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dipValue.toFloat(),
            r.displayMetrics
        ).toInt()
    }

    enum class CHART_GRAVITY(val value:Int){
        LEFT(0),
        RIGHT(1)
    }
    private val defaultWidth:Int = getPixels(400,context)


    private val mPaint:Paint = Paint()
    private var mChartHeight:Float = 50f
    private var chartCount:Int = 10

    private var textSize:Float = 12f

    private var animUpdate:Boolean = false
    private var leftColor:Int = Color.RED
    private var rightColor:Int = Color.GREEN
    private var normalColor:Int = Color.BLACK
    
    private var selectedColor:Int = normalColor

    private var multiSelected:Boolean = false
    
    
    private var chartGravity:CHART_GRAVITY = CHART_GRAVITY.RIGHT

    private val mTextRect:Rect = Rect()

    private var chartModels:MutableList<ChartModel> = arrayListOf()

    private var volume:Float = 10f


    private var animator:ValueAnimator? = null

    private var itemEffectAnimator:ValueAnimator? = null

    private var diff: DiffEval? = DiffEval(mutableListOf())


    private var clickEffect = false

    private var clickedIndex :Int = -1


    init {
        mPaint.color = Color.RED
        mPaint.isAntiAlias = true
    }

    constructor(context: Context, attributeSet: AttributeSet? = null,defStyleAttr: Int = 0):super(context,attributeSet,defStyleAttr){
        this.initStyle(context, attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet? = null):this(context,attributeSet,0)

    constructor(context: Context):this(context,null,0)

    private fun initStyle(context: Context, attributeSet: AttributeSet? = null){
        val typedArray: TypedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ChartViewLayout)
        animUpdate = typedArray.getBoolean(R.styleable.ChartViewLayout_anim_update,false)
        leftColor = typedArray.getColor(R.styleable.ChartViewLayout_left_text_color,context.getColor(R.color.purple_200))
        rightColor = typedArray.getColor(R.styleable.ChartViewLayout_right_text_color,context.getColor(R.color.purple_500))
        mChartHeight = typedArray.getDimension(R.styleable.ChartViewLayout_chart_height,30f)

        multiSelected = typedArray.getBoolean(R.styleable.ChartViewLayout_multi_select,false)

        mChartHeight = ceil(mChartHeight)
        textSize = typedArray.getDimension(R.styleable.ChartViewLayout_text_size,15f)
        selectedColor =typedArray.getColor(R.styleable.ChartViewLayout_selected_color,context.getColor(R.color.purple_500))
        normalColor = typedArray.getColor(R.styleable.ChartViewLayout_normal_color,context.getColor(R.color.purple_700))
        val gravity =typedArray.getInteger(R.styleable.ChartViewLayout_char_gravity,CHART_GRAVITY.RIGHT.value)
        if (gravity == 0){
            chartGravity = CHART_GRAVITY.LEFT
        }
        typedArray.recycle()
    }



    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // 获取view宽的SpecSize和SpecMode
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)

        // 获取view高的SpecSize和SpecMode
        var heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val totalH:Int = (mChartHeight*chartCount).toInt()
        heightSpecSize = if (heightSpecSize<totalH){totalH}else{heightSpecSize}
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST){
           // Log.i("zml","1")
            // 当view的宽和高都设置为wrap_content时，调用setMeasuredDimension(measuredWidth,measureHeight)方法设置view的宽/高为400px
            setMeasuredDimension(defaultWidth, totalH)
        }else if (widthSpecMode == MeasureSpec.AT_MOST){
           // Log.i("zml","2")
            // 当view的宽设置为wrap_content时，设置View的宽为你想要设置的大小（这里我设置400px）,高就采用系统获取的heightSpecSize
            setMeasuredDimension(defaultWidth, heightSpecSize)
        }else if (heightSpecMode == MeasureSpec.AT_MOST){
           // Log.i("zml","3")
            // 当view的高设置为wrap_content时，设置View的高为你想要设置的大小（这里我设置400px）,宽就采用系统获取的widthSpecSize
            setMeasuredDimension(widthSpecSize,  totalH)
        }else{
            setMeasuredDimension(widthSpecSize,  heightSpecSize)
        }



        //mChartHeight = (measuredHeight/chartCount).toFloat()
        //Log.i("zml","measuredHeight=$measuredHeight, chartHeight=$mChartHeight , totalH=$totalH  ,  heightSize=$heightSpecSize")
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val tmp = diff?.diffs ?: arrayListOf()
        //Log.i("zml","onDraw=${this.measuredHeight}")
        for (i in 0 until chartCount){
            mPaint.color =normalColor
            if (clickEffect){
                mPaint.color = if (i == clickedIndex) selectedColor else normalColor
            }

            var percent = if (tmp.size<=i){0.5f}else{tmp[i]}
            val mRectF = RectF((measuredWidth* percent),
                (mChartHeight*i),measuredWidth.toFloat(),mChartHeight*(i+1))

            canvas?.drawRect(mRectF,mPaint)
            mPaint.color = Color.GREEN
            mPaint.strokeWidth = getPixels(1,context).toFloat()
            if (i != 0){
                canvas?.drawLine(mRectF.left,mRectF.top,
                    mRectF.right,mRectF.top,mPaint)
            }

            val text = if(i>=chartModels.size) "- -" else chartModels[i].leftTxt
            //val fontMetrics = mPaint.fontMetricsInt
            mPaint.getTextBounds(text, 0, text.length, mTextRect)


            mPaint.color = leftColor
            mPaint.textSize = textSize//left
            canvas?.drawText(text,0f,
                mRectF.top+(mChartHeight-mTextRect.height())/2+mTextRect.height(),mPaint)


            val rightTxt = if(i>=chartModels.size) "- -" else chartModels[i].rightTxt
            mPaint.getTextBounds(rightTxt, 0, rightTxt.length, mTextRect)
            mPaint.color = rightColor
            mPaint.textSize = textSize//right
            canvas?.drawText(rightTxt,(measuredWidth-mTextRect.width()).toFloat(),
                mRectF.top+(mChartHeight-mTextRect.height())/2+mTextRect.height(),mPaint)
        }


    }

    fun setChartModel(volume: Float,models:MutableList<ChartModel>){
        this.volume = volume
        chartModels = models
        val count = models.size
        if (count!=chartCount){
            chartCount = count
            requestLayout()
        }

        val d = DiffEval(arrayListOf())
        for (i in 0 until models.size) {
            d.diffs.add((models[i].volume/volume))
        }

        if (animUpdate) {
            animateUpdate(d)
        }else {
            diff = d
            invalidate()
        }

    }

    private fun animateUpdate(targetDif:DiffEval){
        if (animator!=null && animator?.isRunning!!) {
            animator?.end()
        }

        post {

            animator = ValueAnimator.ofObject(ChartDifEvaluator(),diff,targetDif)
                .setDuration(200).apply {
                    interpolator = LinearInterpolator()
                    addUpdateListener { animation ->
                        diff = animation.animatedValue as DiffEval

                        invalidate()
                    }
                    start()
                }
        }
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (animator?.isRunning!!) {
            animator?.end()
        }
    }

    fun onItemEffect(){
        if (itemEffectAnimator!=null && itemEffectAnimator?.isRunning!!) {
            itemEffectAnimator?.end()
        }

        post {

        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event?.action == MotionEvent.ACTION_DOWN){
            val y = event.y
            clickedIndex = (ceil(y/mChartHeight)-1).toInt()

            clickEffect = true
            invalidate()
            return true
        }else if (event?.action == MotionEvent.ACTION_UP){
            clickEffect = false
            //Log.i("zml","点击=$clickedIndex")
        }
        invalidate()
        return super.onTouchEvent(event)
    }


    interface OnItemClickListener{
        fun onItemClicked()
    }


    data class ChartModel(
        var volume:Float = 0.5f,
        var leftTxt:String = "5.98",
        var rightTxt:String = "150K"
    )


    data class DiffEval(
    var diffs:MutableList<Float>
    )

}


