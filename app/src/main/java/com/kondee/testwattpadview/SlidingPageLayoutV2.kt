package com.kondee.testwattpadview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper

class SlidingPageLayoutV2 @JvmOverloads constructor(context: Context, attrs: AttributeSet) :
        FrameLayout(context, attrs) {

    inner class SlidingPageDragHelper : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child != mainView
        }

        private var isScroll: Boolean = false

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            primaryView?.let {
                scaleView(it)
            }

            secondaryView?.let {
                isScroll = true
                it.offsetTopAndBottom(dy)
            }

            return top
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return measuredHeight
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)

            if (changedView == primaryView && !isScroll) {
                secondaryView?.offsetTopAndBottom(dy)
            }

            Log.d("Kondee", "onViewPositionChanged : ${changedView.contentDescription} $top $dy")
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)

            isScroll = false

            primaryView?.let {

                if (Math.abs(yvel) >= VELOCITY_THRESHOLD) {
                    if (mState == STATE.COLLAPSED) {
                        when {
                            yvel > 0 -> {
                                previousView(it)
                            }
                            yvel < 0 -> {
                                expandView(it)
                            }
                        }
                    } else if (mState == STATE.EXPANDED) {
                        when {
                            yvel > 0 -> {
                                collapseView(it)
                            }
                            yvel < 0 -> {
                                nextView(it)
                            }
                        }
                    }
                } else {

                    if (it.y > height) {
                        previousView(it)
                    } else if (it.y > (height * 0.85) / 2) {
                        collapseView(it)
                    } else if (it.y > 0) {
                        expandView(it)
                    } else {
                        nextView(it)
                    }
                }
            }

            ViewCompat.postInvalidateOnAnimation(this@SlidingPageLayoutV2)
        }
    }

    init {

    }

    private var mainView: View? = null
    private var primaryView: View? = null
    private var secondaryView: View? = null

    private var mState = STATE.COLLAPSED

    private var viewDragHelper: ViewDragHelper? = null

    private var mPrimaryOffsetTop = 0
    private var mSecondaryOffsetTop: Int = 0

    private var isAnimate: Boolean = false

    private var lastY: Float = 0f

    private var mPageListener: SlidingPageLayoutV2.OnPageChangeListener? = null


    override fun onFinishInflate() {
        super.onFinishInflate()

        viewDragHelper = ViewDragHelper.create(this, 1.0f, SlidingPageDragHelper())

        if (childCount == 3) {
            mainView = getChildAt(0)
            primaryView = getChildAt(2)
            secondaryView = getChildAt(1)

            post {

                mSecondaryOffsetTop = -(secondaryView?.height ?: 0) -
                        (height - (primaryView?.top ?: 0))
                secondaryView?.offsetTopAndBottom(mSecondaryOffsetTop)

                primaryView?.let {
                    collapseView(it)
                }

                ViewCompat.postInvalidateOnAnimation(this@SlidingPageLayoutV2)

                mState = STATE.COLLAPSED
            }
        }
    }

    override fun computeScroll() {
        super.computeScroll()

        if (viewDragHelper?.continueSettling(true) == true) {

            isAnimate = true
            primaryView?.let {
                scaleView(it)
            }

//            secondaryView?.let {
//                if (mState == STATE.COLLAPSED) {
//                    it.offsetTopAndBottom()
//                }
//            }

            ViewCompat.postInvalidateOnAnimation(this)
        } else {

            isAnimate = false

            if (mState == STATE.NEXT_PAGE) {

                mPageListener?.nextPage()

                primaryView?.let {

                    it.offsetTopAndBottom(height * 2)

                    viewDragHelper?.smoothSlideViewTo(it, it.left, (height * 0.85f).toInt())

                    ViewCompat.postInvalidateOnAnimation(this@SlidingPageLayoutV2)

                    mState = STATE.COLLAPSED
                }
            } else if (mState == STATE.PREV_PAGE) {

                mPageListener?.prevPage()

                primaryView?.let {

                    it.offsetTopAndBottom(-(height * 2))

                    viewDragHelper?.smoothSlideViewTo(it, it.left, 0)

                    ViewCompat.postInvalidateOnAnimation(this@SlidingPageLayoutV2)

                    mState = STATE.EXPANDED
                }
            }

//            val layoutParams: SlidingPageLayout.LayoutParams = primaryView?.layoutParams as LayoutParams
//            layoutParams.offsetTop = primaryView?.top ?: 0
//            primaryView?.layoutParams = layoutParams
            mPrimaryOffsetTop = primaryView?.top ?: 0
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isAnimate) {
            return false
        }

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                lastY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (mState == STATE.COLLAPSED) {
                    viewDragHelper?.shouldInterceptTouchEvent(ev)
                    return true
                }

                if (lastY < ev.y) {
                    if (primaryView?.canScrollVertically(-1) == true) {
                        return false
                    }
                } else if (lastY > ev.y) {
                    if (primaryView?.canScrollVertically(1) == true) {
                        return false
                    }
                }
            }
        }

        if (viewDragHelper?.shouldInterceptTouchEvent(ev) == true) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        primaryView?.let {
            viewDragHelper?.captureChildView(it, event.getPointerId(0))
        }

        viewDragHelper?.processTouchEvent(event)
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        Log.d("Kondee", "onLayout : ")

//        primaryView?.let {
//            it.offsetTopAndBottom((it.layoutParams as LayoutParams).offsetTop)
//        }

        primaryView?.let {
            it.offsetTopAndBottom(mPrimaryOffsetTop)
        }

        secondaryView?.let {
            it.offsetTopAndBottom(mSecondaryOffsetTop)
        }
    }

    private fun nextView(it: View) {
        viewDragHelper?.smoothSlideViewTo(it, it.left, -height)

        mState = STATE.NEXT_PAGE
    }

    private fun previousView(it: View) {
        viewDragHelper?.smoothSlideViewTo(it, it.left, height)

        mState = STATE.PREV_PAGE
    }

    private fun expandView(it: View) {
        viewDragHelper?.smoothSlideViewTo(it, it.left, 0)

        mState = STATE.EXPANDED
    }

    private fun collapseView(it: View) {
        viewDragHelper?.smoothSlideViewTo(it, it.left, (height * 0.85).toInt())

        mState = STATE.COLLAPSED
    }

    private fun scaleView(view: View) {
        val top = view.top.toFloat()
        val ratio = Math.max(Math.min(1f, 1 - (top / height)), 0f)

        val scale = 0.8f + ratio * 0.2f
        view.scaleX = scale
        view.scaleY = scale
    }

    fun setOnPageChangeListener(listener: OnPageChangeListener) {
        mPageListener = listener
    }

    interface OnPageChangeListener {
        fun prevPage()

        fun nextPage()
    }

    enum class STATE {
        PREV_PAGE, NEXT_PAGE, COLLAPSED, EXPANDED
    }

    companion object {
        const val VELOCITY_THRESHOLD = 3000
    }

//    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
//        return p is LayoutParams
//    }
//
//    override fun generateDefaultLayoutParams(): FrameLayout.LayoutParams {
//        return LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//    }
//
//    override fun generateLayoutParams(attrs: AttributeSet): FrameLayout.LayoutParams {
//        return LayoutParams(context, attrs)
//    }
//
//    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
//        return LayoutParams(lp)
//    }
//
//    inner class LayoutParams : FrameLayout.LayoutParams {
//
//        var offsetTop: Int = 0
//
//        constructor(lp: ViewGroup.LayoutParams) : super(lp)
//
//        constructor(width: Int, height: Int) : super(width, height)
//
//        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
//    }
}