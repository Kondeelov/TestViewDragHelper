package com.kondee.testwattpadview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_test_sliding_pager.*

class TestSlidingPagerFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_test_sliding_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initInstance()
    }

    var name = ""
    var imageUrl = ""

    private fun initInstance() {
        name = arguments?.getString(KEY_NAME) ?: ""
        imageUrl = arguments?.getString(KEY_IMAGE_URL) ?: ""

        text_view_name.text = name

        Glide.with(this)
                .load(imageUrl)
                .into(image_view_avatar)
    }

    companion object {

        private const val KEY_NAME = "key_name"
        private const val KEY_IMAGE_URL = "key_image_url"

        fun newInstance(name: String, imageUrl: String): Fragment {
            val fragment = TestSlidingPagerFragment()
            val bundle = Bundle()
            bundle.putString(KEY_NAME, name)
            bundle.putString(KEY_IMAGE_URL, imageUrl)
            fragment.arguments = bundle
            return fragment
        }
    }
}
