package com.kondee.testwattpadview.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.kondee.testwattpadview.TestSlidingPagerFragment
import com.kondee.testwattpadview.model.ReqresModel

class TestViewPager(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private var mData: ReqresModel? = null

    override fun getItem(position: Int): Fragment {
        val firstName = mData?.data?.get(position)?.first_name ?: ""
        val lastName = mData?.data?.get(position)?.last_name ?: ""
        val fullName = "$firstName $lastName"
        return TestSlidingPagerFragment.newInstance(fullName, mData?.data?.get(position)?.avatar
                ?: "")
    }

    override fun getCount(): Int {
        return mData?.data?.size ?: 0
    }

    fun setData(data: ReqresModel) {
        mData = data
        notifyDataSetChanged()
    }
}
