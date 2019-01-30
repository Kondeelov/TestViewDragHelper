package com.kondee.testwattpadview.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kondee.testwattpadview.R
import com.kondee.testwattpadview.model.ReqresModel
import com.kondee.testwattpadview.recyclerview.viewholder.TestVDHViewHolder

class TestVDHAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    private var mData: ReqresModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TestVDHViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.vh_test_vdh, parent, false))
    }

    override fun getItemCount(): Int {
        return mData?.data?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as TestVDHViewHolder
        holder.bind(mData?.data?.get(holder.adapterPosition))
    }

    fun setData(data: ReqresModel) {
        mData = data
        notifyDataSetChanged()
    }
}
