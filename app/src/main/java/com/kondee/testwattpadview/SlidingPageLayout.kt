package com.kondee.testwattpadview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IntRange
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class SlidingPageLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet) :
        FrameLayout(context, attrs) {

    private val heightScale = 0.93f
    private var mPage: Int = 0

    private inner class SlidingPageDragHelper : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child != mainView
        }

        //      Vertical Orientation
        override fun getViewVerticalDragRange(child: View): Int {
            val dragRange = if (mOrientation == ORIENTATION.VERTICAL) measuredHeight else 0
            return dragRange
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            slidingView?.let {
                scaleView(it)
            }

            if (mOrientation == ORIENTATION.HORIZONTAL) {
                return 0
            }

            if (mPage == 0 && top + dy > 0) {
                return 0
            }

            if (mPage == mSize - 1 && top + dy < 0) {
                return 0
            }

            if (isAnimate) {
                return 0
            }

            return top
        }

        //      Horizontal Orientation
        override fun getViewHorizontalDragRange(child: View): Int {
            var dragRange = if (mOrientation == ORIENTATION.HORIZONTAL) measuredWidth else 0
            return dragRange
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {

            slidingView?.let {
                scaleView(it)
            }

            if (mOrientation == ORIENTATION.VERTICAL) {
                return 0
            }

            if (mPage == 0 && left + dx > 0) {
                return 0
            }

            if (mPage == mSize - 1 && left + dx < 0) {
                return 0
            }

            if (isAnimate) {
                return 0
            }

            return left
        }


        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)

            slidingView?.let {

                if (mOrientation == ORIENTATION.VERTICAL) {
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

                        if (it.y > height + (height / 4)) {
                            previousView(it)
                        } else if (it.y > (height * heightScale) / 2) {
                            collapseView(it)
                        } else if (it.y > -(height / 4)) {
                            expandView(it)
                        } else {
                            nextView(it)
                        }
                    }
                } else {
                    if (Math.abs(xvel) >= VELOCITY_THRESHOLD) {
                        if (mState == STATE.COLLAPSED) {
                            when {
                                xvel > 0 -> {
                                    previousView(it)
                                }
                                xvel < 0 -> {
                                    expandView(it)
                                }
                            }
                        } else if (mState == STATE.EXPANDED) {
                            when {
                                xvel > 0 -> {
                                    collapseView(it)
                                }
                                xvel < 0 -> {
                                    nextView(it)
                                }
                            }
                        }
                    } else {

                        if (it.x > width + (width / 4)) {
                            previousView(it)
                        } else if (it.x > (width * heightScale) / 2) {
                            collapseView(it)
                        } else if (it.x > -(width / 4)) {
                            expandView(it)
                        } else {
                            nextView(it)
                        }
                    }
                }
            }

            ViewCompat.postInvalidateOnAnimation(this@SlidingPageLayout)
        }
    }

    private var mOrientation: ORIENTATION = ORIENTATION.VERTICAL

    init {

        context.withStyledAttributes(attrs, R.styleable.SlidingPageLayout) {
            mOrientation = ORIENTATION.getOrientation(getInt(R.styleable.SlidingPageLayout_slide_orientation, 0))
        }
    }

    private var slidingView: View? = null
    private var mainView: View? = null

    private var mState = STATE.EXPANDED

    private var viewDragHelper: ViewDragHelper? = null

    private var mLastOffsetTop = 0
    private var mLastOffsetLeft = 0

    private var isAnimate: Boolean = false

    private var lastY: Float = 0f
    private var lastX: Float = 0f

    private var mPageListener: SlidingPageLayout.OnPageChangeListener? = null

    private var slidingViewId: Int = -1

    override fun onFinishInflate() {
        super.onFinishInflate()

        viewDragHelper = ViewDragHelper.create(this, 1.0f, SlidingPageDragHelper())

//        if (childCount == 2) {
//            mainView = getChildAt(0)
//            slidingView = getChildAt(1)
//
//            post {
//                slidingView?.let {
//                    collapseView(it)
//                }
//
//                ViewCompat.postInvalidateOnAnimation(this@SlidingPageLayout)
//            }
//        }

        if (childCount == 1) {
            mainView = getChildAt(0)
            slidingView = FrameLayout(context)
            slidingView?.apply {
                layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                slidingViewId = ViewCompat.generateViewId()
                id = slidingViewId
                isClickable = true
            }

            addView(slidingView, 1)
        }
    }

    override fun computeScroll() {
        super.computeScroll()

        if (viewDragHelper?.continueSettling(true) == true) {

            isAnimate = true
            slidingView?.let {
                scaleView(it)
            }
            ViewCompat.postInvalidateOnAnimation(this)

        } else {

            when (mState) {
                STATE.NEXT_PAGE -> {

                    mPage += 1

                    mPageListener?.currentPage(mPage, false)

                    slidingView?.let {

                        if (mOrientation == ORIENTATION.VERTICAL) {
                            it.offsetTopAndBottom((height * 2))
                        } else {
                            it.offsetLeftAndRight((width * 2))
                        }
                        collapseView(it)

                        ViewCompat.postInvalidateOnAnimation(this@SlidingPageLayout)
                    }
                }
                STATE.PREV_PAGE -> {

                    mPage -= 1

                    mPageListener?.currentPage(mPage, true)

                    slidingView?.let {

                        if (mOrientation == ORIENTATION.VERTICAL) {
                            it.offsetTopAndBottom(-(height * 2))
                        } else {
                            it.offsetLeftAndRight(-(width * 2))
                        }
                        expandView(it)

                        ViewCompat.postInvalidateOnAnimation(this@SlidingPageLayout)
                    }
                }
                else -> {
                    isAnimate = false
                    mLastOffsetTop = slidingView?.top ?: 0
                    mLastOffsetLeft = slidingView?.left ?: 0
                }
            }

        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isAnimate) {
            return false
        }

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                lastY = ev.y
                lastX = ev.x
            }
            MotionEvent.ACTION_MOVE -> {
                if (mState == STATE.COLLAPSED) {
                    viewDragHelper?.shouldInterceptTouchEvent(ev)
                    return true
                }

                if (mOrientation == ORIENTATION.VERTICAL) {

                    if (lastY < ev.y) {
                        if ((currentFragment?.view as ViewGroup?)?.getChildAt(0)?.canScrollVertically(-1) == true) {
                            return false
                        }
                    } else if (lastY > ev.y) {
                        if ((currentFragment?.view as ViewGroup?)?.getChildAt(0)?.canScrollVertically(1) == true) {
                            return false
                        }
                    }
                } else {

                    if (lastX < ev.x) {
                        if ((currentFragment?.view as ViewGroup?)?.getChildAt(0)?.canScrollHorizontally(-1) == true) {
                            return false
                        }
                    } else if (lastX > ev.x) {
                        if ((currentFragment?.view as ViewGroup?)?.getChildAt(0)?.canScrollHorizontally(1) == true) {
                            return false
                        }
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
        if (isAnimate) {
            return false
        }

        slidingView?.let {
            viewDragHelper?.captureChildView(it, event.getPointerId(0))
        }

        viewDragHelper?.processTouchEvent(event)
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        slidingView?.let {
            it.offsetTopAndBottom(mLastOffsetTop)
            it.offsetLeftAndRight(mLastOffsetLeft)
        }
    }

    private fun nextView(v: View) {

        if (mPage == mSize - 1) {
            return
        }

        if (mOrientation == ORIENTATION.VERTICAL) {
            viewDragHelper?.smoothSlideViewTo(v, v.left, -height)
        } else {
            viewDragHelper?.smoothSlideViewTo(v, -width, v.top)
        }

        mState = STATE.NEXT_PAGE
    }

    private fun previousView(v: View) {

        if (mPage == 0) {
            return
        }

        if (mOrientation == ORIENTATION.VERTICAL) {
            viewDragHelper?.smoothSlideViewTo(v, v.left, height)
        } else {
            viewDragHelper?.smoothSlideViewTo(v, width, v.top)
        }

        mState = STATE.PREV_PAGE
    }

    private fun expandView(v: View) {
        if (mOrientation == ORIENTATION.VERTICAL) {
            viewDragHelper?.smoothSlideViewTo(v, v.left, 0)
        } else {
            viewDragHelper?.smoothSlideViewTo(v, 0, v.top)
        }

        mState = STATE.EXPANDED
    }

    private fun collapseView(v: View) {
        if (mOrientation == ORIENTATION.VERTICAL) {
            viewDragHelper?.smoothSlideViewTo(v, v.left, (height * heightScale).toInt())
        } else {
            viewDragHelper?.smoothSlideViewTo(v, (width * heightScale).toInt(), v.top)
        }

        mState = STATE.COLLAPSED
    }

    private fun scaleView(view: View) {

        val ratio: Float
        val scale: Float
        val pivotX: Float
        val pivotY: Float

        if (mOrientation == ORIENTATION.VERTICAL) {
            ratio = Math.max(Math.min(1f, 1 - (view.top.toFloat() / height)), 0f)
            scale = 0.8f + ratio * 0.2f
            val diff = (height.toFloat() * scale) / 2f
            pivotX = width.toFloat() / 2f
            pivotY = height.toFloat() / 2f - diff
        } else {
            ratio = Math.max(Math.min(1f, 1 - (view.left.toFloat() / width)), 0f)
            scale = 0.8f + ratio * 0.2f
            val diff = (width.toFloat() * scale) / 2f
            pivotX = width.toFloat() / 2f - diff
            pivotY = height.toFloat() / 2f
        }

        view.pivotX = pivotX
        view.pivotY = pivotY

        view.scaleX = scale
        view.scaleY = scale
    }

    fun setOnPageChangeListener(listener: OnPageChangeListener) {
        mPageListener = listener
    }

    private var currentFragment: Fragment? = null

    fun setFragment(fm: FragmentManager, fragment: Fragment) {

        currentFragment = fragment

        fm.beginTransaction()
                .replace(slidingViewId, fragment)
                .commit()
    }

    private var mSize: Int = 0

    fun setPageSize(size: Int) {
        mSize = size
    }

    fun setCurrentPage(@IntRange(from = 0, to = Int.MAX_VALUE.toLong()) page: Int) {
        if (page > mSize - 1) {
            mPage = mSize - 1
        }

        mPage = page
    }

    interface OnPageChangeListener {
        fun prevPage()

        fun nextPage()

        fun currentPage(page: Int, isPrevious: Boolean)
    }

    enum class STATE {
        PREV_PAGE, NEXT_PAGE, COLLAPSED, EXPANDED
    }

    enum class ORIENTATION(val value: Int) {
        VERTICAL(0), HORIZONTAL(1);

        companion object {
            fun getValue(orientation: ORIENTATION): Int {
                return orientation.value
            }

            fun getOrientation(value: Int): ORIENTATION {
                return ORIENTATION.values().find { it.value == value } ?: VERTICAL
            }
        }
    }

    companion object {
        const val VELOCITY_THRESHOLD = 3000
    }
}