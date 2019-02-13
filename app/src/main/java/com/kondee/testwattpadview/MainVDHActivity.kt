package com.kondee.testwattpadview

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
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

        layout_container.setFragment(supportFragmentManager, TestSlidingViewVerticalFragment.newInstance(page), 0)

        layout_container.setPageSize(4)

        layout_container.setOnPageChangeListener(object : SlidingPageLayoutV2.OnPageChangeListener {

            override fun onStageChange(state: SlidingPageLayoutV2.STATE) {

            }

            override fun currentPage(page: Int, isPrevious: Boolean) {
                Log.d("Kondee", "currentPage : $page")
                layout_container.setFragment(
                    supportFragmentManager,
                    TestSlidingViewVerticalFragment.newInstance(page + 1, isPrevious),
                    page
                )
            }
        })
    }

    private fun dp2px(dp: Number): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
    }
}
