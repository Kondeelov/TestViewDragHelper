package com.kondee.testwattpadview

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kondee.testwattpadview.recyclerview.adapter.TestVDHAdapter
import com.kondee.testwattpadview.service.HttpManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main_view_drag_helper.*

class MainVDHActivity : AppCompatActivity() {

    var page = 1
    private val compositeDisposable = CompositeDisposable()

    private val mAdapter = TestVDHAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_view_drag_helper)

        initInstance()
    }

    private fun initInstance() {

        callServiceGetUser(page)

        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@MainVDHActivity)
            adapter = mAdapter
        }

        layout_container.setOnPageChangeListener(object : SlidingPageLayout.OnPageChangeListener {
            override fun prevPage() {
                callServiceGetUser(page - 1)
            }

            override fun nextPage() {
                callServiceGetUser(page + 1)
            }
        })
    }

    private fun callServiceGetUser(page1: Int) {

//        TODO()
//        mAdapter.setLoading()

        compositeDisposable.add(
                HttpManager.service.getUsers(page1)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            mAdapter.setData(it)
                        }
        )
    }

    private fun dp2px(dp: Number): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
    }
}
