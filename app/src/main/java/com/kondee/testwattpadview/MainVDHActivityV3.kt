package com.kondee.testwattpadview

import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.activity_main_view_drag_helper_v3.*

class MainVDHActivityV3 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_view_drag_helper_v3)

        initInstance()
    }

    private fun initInstance() {

        val adapter = MySlidingPageAdapter(supportFragmentManager)
        layout_container.setScrollableChildId(R.id.recycler_view)
        layout_container.setAdapter(adapter)

        layout_container.setCurrentPage(15)
    }

    private fun dp2px(dp: Number): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
    }

    class MySlidingPageAdapter(fm: FragmentManager) : SlidingPageAdapter(fm) {

        override fun getItem(position: Int, isPrevious: Boolean): Fragment {
            val page = position % 4
            val fragment = TestSlidingViewVerticalFragment.newInstance(page + 1)
            fragment as hasPrevious
            fragment.isPrevious = isPrevious
            return fragment
        }

        override fun getItemCount(): Int {
            return 4
        }
    }
}