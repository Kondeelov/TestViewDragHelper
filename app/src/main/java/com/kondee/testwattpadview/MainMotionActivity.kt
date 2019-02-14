package com.kondee.testwattpadview

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import kotlinx.android.synthetic.main.activity_main_motion_layout.*

class MainMotionActivity : AppCompatActivity() {

    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_motion_layout)

        gestureDetector = GestureDetector(this@MainMotionActivity, object : GestureDetector.SimpleOnGestureListener() {

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                return true
            }

            override fun onDown(e: MotionEvent?): Boolean {
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {

                if (nested_scroll_view.height + nested_scroll_view.scrollY >= text_view_lorem.height + text_view_lorem.marginTop + text_view_lorem.marginBottom) {

                    val du = Math.max(250, Math.abs((velocityY / nested_scroll_view.height).toLong()))

                    val objectAnimator = if (velocityY < 0) {
                        ObjectAnimator.ofFloat(
                            nested_scroll_view,
                            "y",
                            nested_scroll_view.y,
                            -nested_scroll_view.height.toFloat()
                        )
                    } else {
                        ObjectAnimator.ofFloat(
                            nested_scroll_view,
                            "y",
                            nested_scroll_view.y,
                            0f
                        )
                    }.apply {
                        duration = du
                        interpolator = DecelerateInterpolator(0.2f)
                    }

                    objectAnimator.start()

                    objectAnimator.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {

                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            if (nested_scroll_view.y == -nested_scroll_view.height.toFloat()) {
                                layout_motion_container.progress = 0f
                            }
                        }

                        override fun onAnimationCancel(animation: Animator?) {

                        }

                        override fun onAnimationStart(animation: Animator?) {

                        }
                    })

                    return true
                }
                return false
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                var scrollY = distanceY
                if (nested_scroll_view.height + nested_scroll_view.scrollY >= text_view_lorem.height + text_view_lorem.marginTop + text_view_lorem.marginBottom) {
                    if (nested_scroll_view.y + scrollY <= 0f) {
                        scrollY = 0f
                    }
                    nested_scroll_view.translationY -= scrollY
                    return true
                } else if (layout_motion_container.progress == 0f) {

//                    if (nested_scroll_view.y + scrollY >= layout_motion_container.height - dp2px(32)) {
//                        scrollY = 0f
//                    }
                    nested_scroll_view.translationY -= scrollY
                }
                return false
            }
        })

        initInstance()
    }

    private fun dp2px(dp: Number): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
    }

    private fun initInstance() {
        layout_motion_container.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {

            }

            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {

            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
//                nested_scroll_view.isNestedScrollingEnabled = false
//                isEnd = p3 >= 95f
            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
//                if (isEnd) {
//                    nested_scroll_view.isNestedScrollingEnabled = true
//                }
            }
        })

        nested_scroll_view.viewTreeObserver.addOnScrollChangedListener {

        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }
}
