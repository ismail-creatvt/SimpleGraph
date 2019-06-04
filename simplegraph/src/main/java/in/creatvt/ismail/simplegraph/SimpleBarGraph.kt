package `in`.creatvt.ismail.simplegraph

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.res.ResourcesCompat


class SimpleBarGraph @JvmOverloads constructor(context: Context, attrs: AttributeSet?=null, defStyle: Int=0)
    : View(context, attrs, defStyle) {

    companion object{
        private const val DEFAULT_Y_LABEL_COUNT = 8
    }

    private var currentFraction: Float = 0f

    private var hasBarWidth: Boolean = false

    private var mPixelPerValue: Float = 1f
    //the value of y label step e.g 2 for steps 2,4,6,8
    private var mYLabelStepValue: Float = 1f
    //max y label e.g 8 for 2,4,6,8
    private var mMaxYLabel: Float = 8f
    //The distance between each y labels in pixel
    private var mYLabelStepHeight:Int = 0
    //Attribute value holders
    private val mColorLine:Int

    private val mBubbleFillColor:Int
    private val mTextColorLabel:Int
    private var mStrokeColorAxis: Int
    private var mBarFillColor: Int
    private var mBarStrokeColor: Int
    private var mBubbleStrokeColor: Int
    private val mTextColorBarValue:Int
    private val mBubbleRadius:Float
    private var mBarWidth:Float
    private val mTextSizeXLabel:Float
    private val mTextSizeYLabel:Float
    private val mTextSizeBarValue:Float
    private var mStrokeWidthAxis: Float
    private var mBarStrokeWidth: Float
    private var mBubbleStrokeWidth: Float
    private var mLineStrokeWidth: Float
    private var mBarSpacing: Float
    private var mMaxBarWidth: Float

    //Initialize Paint values
    private val mAxisPaint = Paint(ANTI_ALIAS_FLAG)
    private val mBarPaint = Paint(ANTI_ALIAS_FLAG)
    private val mBarStrokePaint = Paint(ANTI_ALIAS_FLAG)
    private val mLinePaint = Paint(ANTI_ALIAS_FLAG)
    private val mBubbleStrokePaint = Paint(ANTI_ALIAS_FLAG)
    private val mBubbleFillPaint = Paint(ANTI_ALIAS_FLAG)
    private val mXLabelPaint = Paint(ANTI_ALIAS_FLAG)
    private val mYLabelPaint = Paint(ANTI_ALIAS_FLAG)
    private val mBarValuePaint = Paint(ANTI_ALIAS_FLAG)

    private var mYLabelCount = DEFAULT_Y_LABEL_COUNT

    private val mGraphRect = Rect()

    private val mTextBound = Rect()

    private val mBarRects = arrayListOf<Rect>()

    //Data to be displayes
    private var mData = arrayListOf<BarData>()

    data class BarData(val value:Float,val label:String)

    init{
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.SimpleBarGraph, defStyle, 0
        )

        //First initialize all the values from attributes
        mColorLine = getAttrColor(a,R.styleable.SimpleBarGraph_sbg_colorLine,R.color.sbg_default_colorLine)

        mBubbleFillColor = getAttrColor(a,R.styleable.SimpleBarGraph_sbg_bubbleFillColor,R.color.sbg_default_bubbleFillColor)

        mBubbleStrokeColor = getAttrColor(a,R.styleable.SimpleBarGraph_sbg_bubbleStrokeColor,R.color.sbg_default_bubbleStrokeColor)

        mTextColorLabel = getAttrColor(a,R.styleable.SimpleBarGraph_sbg_textColorLabel,R.color.sbg_default_textColorLabel)

        mBarStrokeColor = getAttrColor(a,R.styleable.SimpleBarGraph_sbg_barStrokeColor,R.color.sbg_default_barStrokeColor)

        mBarFillColor = getAttrColor(a,R.styleable.SimpleBarGraph_sbg_barFillColor,R.color.sbg_default_barFillColor)

        mStrokeColorAxis = getAttrColor(a,R.styleable.SimpleBarGraph_sbg_strokeColorAxis,R.color.sbg_default_strokeColorAxis)

        mTextColorBarValue = getAttrColor(a,R.styleable.SimpleBarGraph_sbg_textColorBarValue,R.color.sbg_default_textColorBarValue)

        mBubbleRadius = a.getDimension(R.styleable.SimpleBarGraph_sbg_bubbleRadius,resources.getDimension(R.dimen.sbg_default_bubbleRadius))

        mBubbleStrokeWidth = a.getDimension(R.styleable.SimpleBarGraph_sbg_bubbleStrokeWidth,resources.getDimension(R.dimen.sbg_default_bubbleStrokeWidth))

        mBarWidth = a.getDimension(R.styleable.SimpleBarGraph_sbg_barWidth,-1f)

        hasBarWidth = mBarWidth != -1f

        mBarSpacing = a.getDimension(R.styleable.SimpleBarGraph_sbg_barSpacing,resources.getDimension(R.dimen.sbg_default_barSpacing))

        mBarStrokeWidth = a.getDimension(R.styleable.SimpleBarGraph_sbg_barStrokeWidth,resources.getDimension(R.dimen.sbg_default_barStrokeWidth))

        mTextSizeXLabel = a.getDimension(R.styleable.SimpleBarGraph_sbg_textSizeXLabel,resources.getDimension(R.dimen.sbg_default_textSizeXLabel))

        mTextSizeYLabel = a.getDimension(R.styleable.SimpleBarGraph_sbg_textSizeYLabel,resources.getDimension(R.dimen.sbg_default_textSizeYLabel))

        mStrokeWidthAxis = a.getDimension(R.styleable.SimpleBarGraph_sbg_strokeWidthAxis,resources.getDimension(R.dimen.sbg_default_strokeWidthAxis))

        mTextSizeBarValue = a.getDimension(R.styleable.SimpleBarGraph_sbg_textSizeBarValue,resources.getDimension(R.dimen.sbg_default_textSizeBarValue))

        mLineStrokeWidth = a.getDimension(R.styleable.SimpleBarGraph_sbg_lineStrokeWidth,resources.getDimension(R.dimen.sbg_default_lineStrokeWidth))

        mMaxBarWidth = a.getDimension(R.styleable.SimpleBarGraph_sbg_maxBarWidth,0f)

        initializePaints()

        a.recycle()
    }

    override fun getGlobalVisibleRect(r: Rect?, globalOffset: Point?): Boolean {
        val visible = super.getGlobalVisibleRect(r, globalOffset)
        if(visible){
            animateBars()
        }
        return visible
    }

    fun animateBars(){
        ValueAnimator.ofFloat(0f,1f).apply {
            addUpdateListener {
                currentFraction= it.animatedValue as Float
                invalidate()
            }
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        animateBars()
    }

    private fun initializePaints() {
        mXLabelPaint.textSize = mTextSizeXLabel
        mXLabelPaint.color = mTextColorLabel
        mXLabelPaint.textAlign = Paint.Align.CENTER

        mYLabelPaint.textSize = mTextSizeYLabel
        mYLabelPaint.color = mTextColorLabel

        mBarPaint.color = mBarFillColor

        mBarStrokePaint.color = mBarStrokeColor
        mBarStrokePaint.strokeWidth = mBarStrokeWidth
        mBarStrokePaint.style = Paint.Style.STROKE

        mLinePaint.color = mColorLine
        mLinePaint.strokeWidth = mLineStrokeWidth
        mLinePaint.style = Paint.Style.STROKE

        mBubbleStrokePaint.color = mBubbleStrokeColor
        mBubbleStrokePaint.strokeWidth = mBubbleStrokeWidth
        mBubbleStrokePaint.style = Paint.Style.STROKE

        mBubbleFillPaint.color = mBubbleFillColor

        mBarValuePaint.color = mTextColorBarValue
        mBarValuePaint.textSize = mTextSizeBarValue
        mBarValuePaint.textAlign = Paint.Align.CENTER

        mAxisPaint.color = mStrokeColorAxis
        mAxisPaint.strokeWidth = mStrokeWidthAxis
        mAxisPaint.style = Paint.Style.STROKE
    }

    private fun getAttrColor(a:TypedArray,attr:Int,default:Int): Int {
       return a.getColor(attr,ResourcesCompat.getColor(resources,default,context.theme))
    }

    fun setData(barData:ArrayList<BarData>){
        mData = barData
        repeat(mData.size) { mBarRects.add(Rect()) }
        calculateValues()
        animateBars()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        mXLabelPaint.getTextBounds("My Label",0,8,mTextBound)

        mGraphRect.left = mYLabelPaint.measureText("00000").toInt()
        mGraphRect.bottom = height - mTextBound.height().times(2)
        mGraphRect.right = width
        mGraphRect.top = paddingTop

        calculateBarWidth()
        calculateYLabelDistance()
        calculateBarRects()

        drawXAxis(canvas)
        drawYAxis(canvas)
        drawBars(canvas)
        drawLines(canvas)
        drawBubbles(canvas)
    }

    private fun calculateYLabelDistance() {
        mYLabelStepHeight = mGraphRect.height().minus(mTextSizeYLabel).div(mYLabelCount).toInt()
        mPixelPerValue = mYLabelStepHeight/mYLabelStepValue
    }

    private fun calculateBarWidth() {
        if(!hasBarWidth){
            mBarWidth = (mGraphRect.width() - (mBarSpacing * (mData.size + 1))).div(mData.size)
            if(mMaxBarWidth!=0f && mBarWidth > mMaxBarWidth){
                mBarWidth = mMaxBarWidth
            }
        }else{
            mBarSpacing = (mGraphRect.width() - (mBarWidth * mData.size)).div(mData.size + 1)
        }
    }

    private fun calculateValues(){
        if(mData.isEmpty()) return
        val max = mData.maxBy { it.value }
        calculateStepAndMaxYLabel(max!!)
        if(max.value < mMaxYLabel.div(2)){
            mYLabelCount = mYLabelCount.div(2)
            calculateStepAndMaxYLabel(max)
        }else{
            mYLabelCount = DEFAULT_Y_LABEL_COUNT
            calculateStepAndMaxYLabel(max)
        }
    }

    private fun calculateStepAndMaxYLabel(max:BarData){
        mYLabelStepValue = Math.ceil(max.value.div(mYLabelCount).toDouble()).toFloat()
        mMaxYLabel =  mYLabelStepValue * mYLabelCount
    }

    private fun drawXAxis(canvas: Canvas) {
        canvas.drawLine(mGraphRect.left.toFloat(),mGraphRect.bottom.toFloat(),mGraphRect.right.toFloat(),mGraphRect.bottom.toFloat(),mAxisPaint)
    }

    private fun drawYAxis(canvas: Canvas) {
        canvas.drawLine(mGraphRect.left.toFloat(),mGraphRect.top.toFloat(),mGraphRect.left.toFloat(),mGraphRect.bottom.toFloat(),mAxisPaint)
        for(i in 1..mYLabelCount){
            val label = mYLabelStepValue.times(i).toString()
            mYLabelPaint.getTextBounds(label,0,label.length,mTextBound)
            canvas.drawText(label,0f,(mGraphRect.bottom - (mYLabelStepHeight * i) + mTextBound.height().div(2)).toFloat(),mYLabelPaint)
        }
    }

    private fun calculateBarRects(){
        mData.forEachIndexed { i, barData ->
            mBarRects[i].left = (mGraphRect.left + (i * (mBarSpacing + mBarWidth)) + mBarSpacing).toInt()
            mBarRects[i].right = mBarRects[i].left + mBarWidth.toInt()
            mBarRects[i].bottom = mGraphRect.bottom
            mBarRects[i].top = mGraphRect.bottom - (Math.ceil(mPixelPerValue.times(barData.value).toDouble()).toInt() * currentFraction).toInt()
        }
    }

    private fun drawBars(canvas: Canvas) {

        mBarRects.forEachIndexed{ index,it ->
            canvas.drawRect(it,mBarPaint)
            canvas.drawRect(it,mBarStrokePaint)

            val value = mData[index].value.toString()
            val label = mData[index].label
            mXLabelPaint.getTextBounds(label,0,label.length,mTextBound)
            //Draw bar value
            canvas.drawText(value,0,value.length,it.exactCenterX(),it.top - mBubbleRadius * 2,mBarValuePaint)

            //Draw X-Axis bar label
            canvas.drawText(label,0,label.length,it.exactCenterX(),it.bottom + mTextBound.height().times(1.5f),mXLabelPaint)
        }

    }

    private fun Rect.prettyPrint():String{
        return " left : $left" +
               " right : $right" +
               " top : $top" +
               " bottom : $bottom"
    }

    private fun drawLines(canvas: Canvas) {
        for(i in 0 until mBarRects.size-1){
            canvas.drawLine(mBarRects[i].exactCenterX(),mBarRects[i].top.toFloat(),
                mBarRects[i+1].exactCenterX(),mBarRects[i+1].top.toFloat(),mLinePaint)
        }
    }

    private fun drawBubbles(canvas: Canvas){
        mBarRects.forEach {
            canvas.drawCircle(it.left + it.width()/2f,it.top.toFloat(),mBubbleRadius,mBubbleFillPaint)
            canvas.drawCircle(it.left + it.width()/2f,it.top.toFloat(),mBubbleRadius,mBubbleStrokePaint)
        }
    }

}
