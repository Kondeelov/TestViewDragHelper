package com.kondee.testwattpadview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kondee.testwattpadview.recyclerview.adapter.TestVDHAdapter
import com.kondee.testwattpadview.service.HttpManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_test_sliding_view_vertical.*

class TestSlidingViewVerticalFragment : Fragment(), hasPrevious {

    override var isPrevious: Boolean = false

    private val compositeDisposable = CompositeDisposable()
    private val mAdapter = TestVDHAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("Kondee", "onCreate : ")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_test_sliding_view_vertical, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initInstance()
    }

    private fun initInstance() {
        Log.d("Kondee", "initInstance : ")

        val page = arguments?.getInt(KEY_PAGE, 1) ?: 1

        callServiceGetUser(page)

        recycler_view.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = mAdapter
            itemAnimator = null
        }
    }

    private fun callServiceGetUser(page: Int) {
        compositeDisposable.add(
            HttpManager.service.getUsers(page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ t ->
                    t?.let {
                        mAdapter.setData(t)
                        if (isPrevious) {
                            recycler_view?.scrollToPosition(mAdapter.itemCount - 1)
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
            val fragment = TestSlidingViewVerticalFragment()
            val bundle = Bundle()
            bundle.putInt(KEY_PAGE, page)
//            bundle.putBoolean(KEY_IS_PREVIOUS, hasPrevious)
            fragment.arguments = bundle
            return fragment
        }
    }
}