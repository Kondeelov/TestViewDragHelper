package com.kondee.testwattpadview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.annotation.IntRange
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class SlidingPageLayoutV3 @JvmOverloads constructor(context: Context, attrs: AttributeSet) :
    FrameLayout(context, attrs) {

    private var mPage: Int = 0

    private val stateSubject = PublishSubject.create<STATE>()

    private var shouldClearView: Boolean = false

    private var mAdapter: SlidingPageAdapter? = null

    private inner class SlidingPageDragHelper : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return true
        }

        //        Vertical Orientation
        override fun getViewVerticalDragRange(child: View): Int {
            val dragRange = if (mOrientation == ORIENTATION.VERTICAL) measuredHeight else 0
            return dragRange
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            if (mOrientation == ORIENTATION.HORIZONTAL) {
                return 0
            }

            if (mPage == 0 && top + dy > 0) {
                return 0
            }

            if (mPage == mSize - 1 && top + dy < 0 && (mLastState == STATE.EXPANDED && dy < 0)) {
                return 0
            }

            if (isAnimate || mLock) {
                return 0
            }

            slidingView?.let {
                if (mLastState == STATE.COLLAPSED) {
                    if (top + dy > it.height) {
                        it.offsetTopAndBottom(-it.top)
                        it.offsetTopAndBottom(-it.height)

                        if (!isNextView) {
                            shouldClearView = true
                        }

                        mAdapter?.detachFragment(mPage)

                        mAdapter?.setCurrentPosition(slidingViewId, mPage - 1, true)

//                        view?.visibility = View.VISIBLE
                    } else if (top + dy < -it.height) {
                        it.offsetTopAndBottom(-it.top)
                        it.offsetTopAndBottom(it.height)
                    }
                }
//                else if (mLastState == STATE.EXPANDED) {
//                    if (dy < 0 && slidingView?.findViewById<View>(mScrollChildId)?.canScrollVertically(1) == true) {
//                        return Math.max(top, 0)
//                    } else if (dy > 0 && slidingView?.findViewById<View>(mScrollChildId)?.canScrollVertically(-1) == true) {
//                        return Math.max(top, 0)
//                    }
//                }

                if (it.top in 1..height && shouldClearView) {
//                    view?.visibility = View.GONE
                }
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

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)

            if (isAnimate || mState == STATE.NEXT_PAGE || mState == STATE.PREV_PAGE || mLock) {
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
            }

            ViewCompat.postInvalidateOnAnimation(this@SlidingPageLayoutV3)
        }
    }

    private fun smoothScrollVertical(v: View) {
        if (v.top < -height + (height / 2)) {
            nextView(v)
        } else if (v.top < 0) {
            if (mLastState == STATE.EXPANDED) {
                expandView(v)
            } else {
                previousView(v)
            }
        } else if (v.top < v.height / 2) {
            expandView(v)
        } else if (v.top > v.height / 2) {
            collapseView(v)
        }
    }

    private fun smoothScrollHorizontal(v: View) {
//        TODO("Implement")
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
                mPageListener?.onStateChange(it)

//                if (it == STATE.EXPANDED) {
//                    mFragmentManager?.let { fm ->
//                        fm.beginTransaction()
//                            .replace(slidingViewId, currentFragment!!, "position_" + mPage)
//                            .runOnCommit {
//                                view?.visibility = View.GONE
//                            }
//                            .commitAllowingStateLose()
//
//                        lastFragment = currentFragment
//                    }
//                }
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

//    private var view: View? = null

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

//        if (slidingView is ViewGroup) {
//            view = View(context)
//            view?.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
//            view?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
//
//            view?.bringToFront()
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                view?.elevation = 8f
//            }
//            view?.visibility = View.GONE
//
//            (slidingView as FrameLayout).addView(view, (slidingView as ViewGroup).childCount)
//        }
    }

    private var isNextView: Boolean = false

    override fun computeScroll() {
        super.computeScroll()

        if (viewDragHelper?.continueSettling(true) == true) {

            isAnimate = true
            ViewCompat.postInvalidateOnAnimation(this)
        } else {
            when (mState) {
                STATE.NEXT_PAGE -> {

                    isNextView = false

                    mAdapter?.detachFragment(mPage)

                    if (mLastState == STATE.EXPANDED) {
                        mPage += 1

                        isNextView = true
                    }

//                    view?.visibility = View.VISIBLE

                    mPageListener?.currentPage(mPage)

                    slidingView?.let {
                        collapseView(it, false)
                    }

                    setAnimate(false)

                    mLastState = mState
                }
                STATE.PREV_PAGE -> {

                    mAdapter?.detachFragment(mPage)

                    if (mLastState == STATE.COLLAPSED) {
                        mPage -= 1
                    }

//                    view?.visibility = View.VISIBLE

                    mPageListener?.currentPage(mPage, true)

                    slidingView?.let {
                        expandView(it, false)
                    }

                    setAnimate(false)

                    mLastState = mState
                }
                STATE.EXPANDED, STATE.COLLAPSED -> {
                    mLastState = mState
                    setAnimate(false)
                    mLastOffsetTop = slidingView?.top ?: 0
                    mLastOffsetLeft = slidingView?.left ?: 0
                }
            }

            stateSubject.onNext(mState)
        }
    }

    private fun setAnimate(animating: Boolean) {
        synchronized(Any()) {
            Handler().postDelayed({
                isAnimate = animating
            }, 150)
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

                shouldClearView = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (mState == STATE.COLLAPSED) {
                    if (mOrientation == ORIENTATION.VERTICAL) {
                        if (Math.abs(ev.y - lastY) < viewConfiguration.scaledTouchSlop) {
                            return false
                        }
                    } else {
                        if (Math.abs(ev.x - lastX) < viewConfiguration.scaledTouchSlop) {
                            return false
                        }
                    }

                    viewDragHelper?.shouldInterceptTouchEvent(ev)
                    return true
                }

                val scrollChild = slidingView?.findViewById(mScrollChildId) as View?

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
            ViewCompat.postInvalidateOnAnimation(this@SlidingPageLayoutV3)
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

        var leftV = 0
        var topV = 0

        if (mOrientation == ORIENTATION.VERTICAL) {
            leftV = v.left
            topV = 0
        } else {
            leftV = 0
            topV = v.top
        }

        viewDragHelper?.smoothSlideViewTo(v, leftV, topV)

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
            v.offsetTopAndBottom(-topV)
            v.offsetLeftAndRight(-leftV)

            mLastOffsetTop = slidingView?.top ?: 0
            mLastOffsetLeft = slidingView?.left ?: 0
        }

        mState = STATE.EXPANDED
    }

    private fun collapseView(v: View, isAnimate: Boolean = true) {

        if (mPage == 0) {
            return
        }

        var leftV = 0
        var topV = 0
        if (mOrientation == ORIENTATION.VERTICAL) {
            leftV = v.left
            topV = height
        } else {
            leftV = width
            topV = v.top
        }

        if (isAnimate) {
            viewDragHelper?.smoothSlideViewTo(v, leftV, topV)
        } else {
            v.offsetTopAndBottom(-(v.top))
            v.offsetTopAndBottom(topV)
            v.offsetLeftAndRight(-(v.left))
            v.offsetLeftAndRight(leftV)

            mLastOffsetTop = slidingView?.top ?: 0
            mLastOffsetLeft = slidingView?.left ?: 0
        }

        mState = STATE.COLLAPSED
    }

//    private var currentFragment: Fragment? = null

//    private var mFragmentManager: FragmentManager? = null

//    fun setFragment(fm: FragmentManager, fragment: Fragment) {
//        currentFragment = fragment
//
//        mFragmentManager = fm
//    }

    private var mSize: Int = 0

    fun setPageSize(size: Int) {
        mSize = size
    }

    fun setCurrentPage(@IntRange(from = 0, to = Int.MAX_VALUE.toLong()) page: Int) {
        if (page > mSize - 1) {
            mPage = mSize - 1
        }

        mPage = page

        mAdapter?.setCurrentPosition(slidingViewId, mPage)
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

    fun setAdapter(adapter: SlidingPageAdapter) {
        mAdapter = adapter

        mSize = mAdapter?.getItemCount() ?: 0

        mPageListener?.currentPage(mPage)
    }

    interface OnPageChangeListener {
        fun currentPage(page: Int, isPrevious: Boolean = false)
        fun onStateChange(state: STATE)
    }

    enum class STATE {
        PREV_PAGE, NEXT_PAGE, COLLAPSED, EXPANDED, DRAGGING
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

    private var mPageListener: SlidingPageLayoutV3.OnPageChangeListener? = object : OnPageChangeListener {
        override fun currentPage(page: Int, isPrevious: Boolean) {
            mAdapter?.setCurrentPosition(slidingViewId, page, isPrevious)
        }

        override fun onStateChange(state: STATE) {

        }
    }
}

interface isPrevious {
    var isPrevious: Boolean
}

abstract class SlidingPageAdapter(private val fragmentManager: FragmentManager) {

    private var fragmentTransaction: FragmentTransaction? = null

    init {
        createFragmentTransaction()
    }

    abstract fun getItem(position: Int, isPrevious: Boolean = false): Fragment

    abstract fun getItemCount(): Int

    private var currentFragment: Fragment? = null

    internal fun adTachFragment(containerId: Int, position: Int, isPrevious: Boolean) {

        val fragmentTag = getFragmentTag(position)
        val fragment = fragmentManager.findFragmentByTag(fragmentTag)
        if (fragment == null) {
            val item = getItem(position, isPrevious)
            item as isPrevious
            item.isPrevious = isPrevious
            fragmentTransaction?.add(containerId, item, fragmentTag)
        } else {
            if (fragment.isDetached) {
                fragment as isPrevious
                fragment.isPrevious = isPrevious
                fragmentTransaction?.attach(fragment)
            }
        }

        currentFragment = fragment
    }

    internal fun commit() {
        if (fragmentTransaction != null) {
            fragmentTransaction?.commit()
            fragmentTransaction = null
            fragmentManager.executePendingTransactions()
        }
    }

    private var mPosition: Int = 0

    fun setCurrentPosition(containerId: Int, position: Int, isPrevious: Boolean = false) {
        if (mPosition == position) {
            return
        }
        mPosition = position

        createFragmentTransaction()

        if (position - 1 >= 0) {
            prepareFragment(position - 1)
        }

        if (position + 1 < getItemCount()) {
            prepareFragment(position + 1)
        }

        adTachFragment(containerId, position, isPrevious)

        commit()
    }

    private fun prepareFragment(position: Int) {

        val fragmentTag = getFragmentTag(position)
        val fragment = fragmentManager.findFragmentByTag(fragmentTag)

        if (fragment == null) {
            fragmentTransaction?.attach(getItem(position))
        } else {
            if (!fragment.isDetached) {
                fragmentTransaction?.detach(fragment)
            }
        }
    }

    fun detachFragment(position: Int) {
        createFragmentTransaction()

        val fragment = fragmentManager.findFragmentByTag(getFragmentTag(position))
        fragment?.let {
            fragmentTransaction?.detach(it)
        }

        commit()
    }

    private fun createFragmentTransaction() {
        if (fragmentTransaction == null) {
            fragmentTransaction = fragmentManager.beginTransaction()
            (fragmentTransaction as FragmentTransaction).setReorderingAllowed(true)
        }
    }

    private var offScreenLimit = 1

    fun setOffScreenLimit(@IntRange(from = 1, to = Long.MAX_VALUE) page: Int) {
        offScreenLimit = page
    }

    fun getCurrentPosition(): Int {
        return mPosition
    }

    private fun getFragmentTag(position: Int): String {
        val tag = "sliding_view:$position"
        Log.d("Kondee", "getFragmentTag : $tag")
        return tag
    }
}