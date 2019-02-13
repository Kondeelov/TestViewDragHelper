package com.kondee.testwattpadview

import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.activity_main_view_drag_helper_V3.*

class MainVDHActivityV3 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_view_drag_helper)

        initInstance()
    }

    private fun initInstance() {
        layout_container.setAdapter(MySlidingPageAdapter(supportFragmentManager))
    }

    private fun dp2px(dp: Number): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
    }

    class MySlidingPageAdapter(fm: FragmentManager) : SlidingPageAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return TestSlidingViewVerticalFragment.newInstance(position)
        }

        override fun getItemCount(): Int {
            return 4
        }

    }

}
