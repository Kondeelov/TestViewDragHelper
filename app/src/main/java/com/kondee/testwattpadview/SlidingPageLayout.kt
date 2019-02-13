package com.kondee.testwattpadview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.annotation.IntRange
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class SlidingPageLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet) :
    FrameLayout(context, attrs) {

    private val sizeScale = 0.93f
    private var mPage: Int = 0

    private val stateSubject = PublishSubject.create<STATE>()

    private inner class SlidingPageDragHelper : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return true
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

            if (mLock) {
                return 0
            }

            if (mState != STATE.DRAGGING) {
                mLastState = mState
            }
            mState = STATE.DRAGGING

            stateSubject.onNext(mState)

            mLastOffsetTop = top

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

            if (mLock) {
                return 0
            }

            if (mState != STATE.DRAGGING) {
                mLastState = mState
            }
            mState = STATE.DRAGGING

            stateSubject.onNext(mState)

            mLastOffsetLeft = left

            return left
        }

        override fun onViewDragStateChanged(state: Int) {
            super.onViewDragStateChanged(state)
            if (state == 1) {
                mRealState = STATE.DRAGGING
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)

            if (isAnimate || mState == STATE.PREV_PAGE || mState == STATE.NEXT_PAGE || mLock) {
                return
            }

            slidingView?.let {

                if (mOrientation == ORIENTATION.VERTICAL) {
                    if (Math.abs(yvel) >= VELOCITY_THRESHOLD) {
                        if (mLastState == STATE.COLLAPSED) {
                            when {
                                yvel > 0 -> {
                                    previousView(it)
                                }
                                yvel < 0 -> {
                                    expandView(it)
                                }
                                else -> {
                                    smoothScrollVertical(it)
                                }
                            }
                        } else if (mLastState == STATE.EXPANDED) {
                            when {
                                yvel > 0 -> {
                                    collapseView(it)
                                }
                                yvel < 0 -> {
                                    nextView(it)
                                }
                                else -> {
                                    smoothScrollVertical(it)
                                }
                            }
                        }
                    } else {
                        smoothScrollVertical(it)
                    }
                } else {
                    if (Math.abs(xvel) >= VELOCITY_THRESHOLD) {
                        if (mLastState == STATE.COLLAPSED) {
                            when {
                                xvel > 0 -> {
                                    previousView(it)
                                }
                                xvel < 0 -> {
                                    expandView(it)
                                }
                                else -> {
                                    smoothScrollHorizontal(it)
                                }
                            }
                        } else if (mLastState == STATE.EXPANDED) {
                            when {
                                xvel > 0 -> {
                                    collapseView(it)
                                }
                                xvel < 0 -> {
                                    nextView(it)
                                }
                                else -> {
                                    smoothScrollHorizontal(it)
                                }
                            }
                        }
                    } else {
                        smoothScrollHorizontal(it)
                    }
                }

                scaleView(it)
            }

            ViewCompat.postInvalidateOnAnimation(this@SlidingPageLayout)
        }
    }

    private fun smoothScrollVertical(v: View) {
        if (v.y > height + (height / 4)) {
            previousView(v)
        } else if (v.y > (height * sizeScale) / 2) {
            collapseView(v)
        } else if (v.y > -(height / 4)) {
            expandView(v)
        } else {
            nextView(v)
        }
    }

    private fun smoothScrollHorizontal(v: View) {
        if (v.x > width + (width / 4)) {
            previousView(v)
        } else if (v.x > (width * sizeScale) / 2) {
            collapseView(v)
        } else if (v.x > -(width / 4)) {
            expandView(v)
        } else {
            nextView(v)
        }
    }

    var mOrientation: ORIENTATION = ORIENTATION.VERTICAL
    private var slidingViewId: Int = -1

    init {

        val a = context.obtainStyledAttributes(attrs, R.styleable.SlidingPageLayout)

        try {
            mOrientation = ORIENTATION.getOrientation(a.getInt(R.styleable.SlidingPageLayout_slide_orientation, 0))
            slidingViewId = a.getResourceId(R.styleable.SlidingPageLayout_sliding_view_id, ViewCompat.generateViewId())
        } finally {
            a.recycle()
        }

        stateSubject.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .subscribe {
                mPageListener?.onStageChange(it)

                if (it == STATE.EXPANDED) {

                    if (currentFragment != lastFragment) {
                        mFragmentManager?.let { fm ->
                            fm.beginTransaction()
                                .replace(slidingViewId, currentFragment!!)
                                .commit()

                            lastFragment = currentFragment
                        }
                    }
                }
            }
    }

    private var slidingView: View? = null

    private var mState = STATE.EXPANDED

    private var mLastState = STATE.COLLAPSED

    private var viewDragHelper: ViewDragHelper? = null

    private var mLastOffsetTop = 0
    private var mLastOffsetLeft = 0

    private var isAnimate: Boolean = false

    private var lastY: Float = 0f
    private var lastX: Float = 0f

    private var mPageListener: SlidingPageLayout.OnPageChangeListener? = null

    private var mRealState = STATE.EXPANDED

    override fun onFinishInflate() {
        super.onFinishInflate()

        viewDragHelper = ViewDragHelper.create(this, 1.0f, SlidingPageDragHelper())

        if (childCount == 1) {
            slidingView = FrameLayout(context)
            slidingView?.apply {
                layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                id = slidingViewId
                contentDescription = "SlidingView"
                isClickable = true
            }

            addView(slidingView, 1)
        } else {
            slidingView = findViewById(slidingViewId)
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

            stateSubject.onNext(mState)

            when (mState) {
                STATE.NEXT_PAGE -> {

                    mPage += 1

//                    (currentFragment as NovelReaderFragment?)?.resetView()

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

//                    (currentFragment as NovelReaderFragment?)?.resetView()

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
                STATE.EXPANDED, STATE.COLLAPSED -> {
                    isAnimate = false
                    mLastOffsetTop = slidingView?.top ?: 0
                    mLastOffsetLeft = slidingView?.left ?: 0
                }
            }
        }
    }

    private val viewConfiguration: ViewConfiguration
        get() {
            return ViewConfiguration.get(context)
        }

    private val scaledTouchSlop = viewConfiguration.scaledTouchSlop

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
                    if (mOrientation == ORIENTATION.VERTICAL) {
                        if (Math.abs(ev.y - lastY) < 1) {
                            return false
                        }
                    } else {
                        if (Math.abs(ev.x - lastX) < 1) {
                            return false
                        }
                    }

                    viewDragHelper?.shouldInterceptTouchEvent(ev)
                    return true
                }

                val rootFragment = currentFragment?.view as ViewGroup?
                val scrollChild = rootFragment?.findViewById(mScrollChildId) as View?

                if (mOrientation == ORIENTATION.VERTICAL) {

                    if (lastY < ev.y) {
                        if (scrollChild?.canScrollVertically(-1) == true) {
                            return false
                        }
                    } else if (lastY > ev.y) {
                        if (scrollChild?.canScrollVertically(1) == true) {
                            return false
                        }
                    }
                } else {

                    if (lastX < ev.x) {
                        if (scrollChild?.canScrollHorizontally(-1) == true) {
                            return false
                        }
                    } else if (lastX > ev.x) {
                        if (scrollChild?.canScrollHorizontally(1) == true) {
                            return false
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (Math.abs(ev.x - lastX) < scaledTouchSlop && Math.abs(ev.y - lastY) < scaledTouchSlop /*&& currentTime - lastTime <= ViewConfiguration.getJumpTapTimeout()*/) {
                    return false
                }
            }
        }

        if (viewDragHelper?.shouldInterceptTouchEvent(ev) == true) {
            return true
        }

        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isAnimate) {
            return false
        }

        if (event.pointerCount > 1) {
            if (mOrientation == ORIENTATION.VERTICAL) {
                slidingView?.let {
                    smoothScrollVertical(it)
                }
            } else {
                slidingView?.let {
                    smoothScrollHorizontal(it)
                }
            }
            ViewCompat.postInvalidateOnAnimation(this@SlidingPageLayout)
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

        if (slidingView != null) {
            slidingView?.offsetTopAndBottom(mLastOffsetTop)
            slidingView?.offsetLeftAndRight(mLastOffsetLeft)

            scaleView(slidingView!!)
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

    private fun expandView(v: View, isAnimate: Boolean = true) {
        var leftV = 0
        var topV = 0

        if (mOrientation == ORIENTATION.VERTICAL) {
            leftV = v.left
            topV = 0
        } else {
            leftV = 0
            topV = v.top
        }

        if (isAnimate) {
            viewDragHelper?.smoothSlideViewTo(v, leftV, topV)
        } else {
            v.left = 0
            v.top = 0
            v.offsetTopAndBottom(leftV)
            v.offsetLeftAndRight(topV)
        }

        mState = STATE.EXPANDED
    }

    private fun collapseView(v: View, isAnimate: Boolean = true) {
        var leftV = 0
        var topV = 0
        if (mOrientation == ORIENTATION.VERTICAL) {
            leftV = v.left
            topV = (height * sizeScale).toInt()
        } else {
            leftV = (width * sizeScale).toInt()
            topV = v.top
        }

        if (isAnimate) {
            viewDragHelper?.smoothSlideViewTo(v, leftV, topV)
        } else {
            v.offsetTopAndBottom(-(v.left))
            v.offsetTopAndBottom(leftV)
            v.offsetLeftAndRight(-(v.top))
            v.offsetLeftAndRight(topV)
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

    private var currentFragment: Fragment? = null

    private var lastFragment: Fragment? = null

    private var mFragmentManager: FragmentManager? = null

    fun setFragment(fm: FragmentManager, fragment: Fragment) {

        currentFragment = fragment

        mFragmentManager = fm
//        val subscribe = stateSubject
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe {
//
//                }

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

    private var mScrollChildId: Int = -1

    fun setScrollableChildId(@IdRes id: Int) {
        mScrollChildId = id
    }

    fun setOnPageChangeListener(listener: OnPageChangeListener) {
        mPageListener = listener
    }

    private var mLock: Boolean = false

    fun setLockScroll(lock: Boolean) {
        mLock = lock
    }

    interface OnPageChangeListener {
        fun currentPage(page: Int, isPrevious: Boolean)
        fun onStageChange(state: STATE)
    }

    enum class STATE {
        PREV_PAGE, NEXT_PAGE, COLLAPSED, EXPANDED, DRAGGING, ANIMATING
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