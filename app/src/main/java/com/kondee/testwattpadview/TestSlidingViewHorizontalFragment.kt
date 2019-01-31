package com.kondee.testwattpadview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kondee.testwattpadview.service.HttpManager
import com.kondee.testwattpadview.viewpager.TestViewPager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_test_sliding_view_horizontal.*
import kotlinx.android.synthetic.main.fragment_test_sliding_view_vertical.*

class TestSlidingViewHorizontalFragment : Fragment() {

    private val compositeDisposable = CompositeDisposable()

    private var isPrevious: Boolean = false

    private var mAdapter: TestViewPager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_test_sliding_view_horizontal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initInstance()
    }

    private fun initInstance() {
        isPrevious = arguments?.getBoolean(KEY_IS_PREVIOUS) ?: false

        mAdapter = TestViewPager(childFragmentManager)

        callServiceGetUser(arguments?.getInt(KEY_PAGE, 1) ?: 1)

        view_pager.apply {
            adapter = mAdapter
        }
    }

    private fun callServiceGetUser(page: Int) {
        compositeDisposable.add(
                HttpManager.service.getUsers(page)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ t ->
                            t?.let {
                                mAdapter?.setData(t)
                                if (isPrevious) {
                                            view_pager?.setCurrentItem((mAdapter?.count
                                                    ?: 0) - 1, false)
                                }
                            }
                        }, { t ->
                            Log.w("Kondee", t)
                        })
        )
    }

    companion object {

        private const val KEY_PAGE: String = "key_page"
        private const val KEY_IS_PREVIOUS: String = "key_is_previous"

        fun newInstance(page: Int): Fragment {
            return newInstance(page, false)
        }

        fun newInstance(page: Int, isPrevious: Boolean): Fragment {
            val fragment = TestSlidingViewHorizontalFragment()
            val bundle = Bundle()
            bundle.putInt(KEY_PAGE, page)
            bundle.putBoolean(KEY_IS_PREVIOUS, isPrevious)
            fragment.arguments = bundle
            return fragment
        }
    }
}