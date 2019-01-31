package com.kondee.testwattpadview

import android.os.Build
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main_view_drag_helper.*

class MainVDHActivity : AppCompatActivity() {

    var page = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_view_drag_helper)

        initInstance()
    }

    private fun initInstance() {

        layout_container.setFragment(supportFragmentManager, TestSlidingViewHorizontalFragment.newInstance(page))

        layout_container.setPageSize(4)

        layout_container.setOnPageChangeListener(object : SlidingPageLayout.OnPageChangeListener {

            override fun currentPage(page: Int, isPrevious: Boolean) {
                layout_container.setFragment(supportFragmentManager, TestSlidingViewHorizontalFragment.newInstance(page + 1, isPrevious))
            }

            override fun prevPage() {
//                page--
//                layout_container.setFragment(supportFragmentManager, TestSlidingViewVerticalFragment.newInstance(page))
            }

            override fun nextPage() {
//                page++
//                layout_container.setFragment(supportFragmentManager, TestSlidingViewVerticalFragment.newInstance(page))
            }
        })
    }

    private fun dp2px(dp: Number): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
    }
}
