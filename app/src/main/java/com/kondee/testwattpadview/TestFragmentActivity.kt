package com.kondee.testwattpadview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_test_fragment.*

class TestFragmentActivity : AppCompatActivity() {

    var page = 1

    var fragmentList = mutableMapOf<Int, Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_fragment)

        supportFragmentManager.beginTransaction()
            .add(R.id.contentContainer, TestSlidingViewVerticalFragment.newInstance(page), "test:$page")
            .commit()

        button_toggle.setOnClickListener {
            page = if (page == 1) 2 else 1
            toggleFragment(page)
        }
    }

    private fun toggleFragment(page: Int) {

        val nPage = if (page == 1) {
            2
        } else {
            1
        }

        val fragment2 = supportFragmentManager.findFragmentByTag("test:$nPage")

        if (fragment2 != null) {
            if (fragment2.isDetached.not()) {
                supportFragmentManager.beginTransaction().detach(fragment2).commit()
            }
        }


        val fragment = supportFragmentManager.findFragmentByTag("test:$page")

        if (fragment == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.contentContainer, TestSlidingViewVerticalFragment.newInstance(page), "test:$page")
                .commit()
        } else {
            supportFragmentManager.beginTransaction().attach(fragment).commit()
        }
    }
}