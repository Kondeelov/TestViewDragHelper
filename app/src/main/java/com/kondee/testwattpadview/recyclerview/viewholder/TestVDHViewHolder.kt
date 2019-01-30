package com.kondee.testwattpadview.recyclerview.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kondee.testwattpadview.model.ReqresModel
import kotlinx.android.synthetic.main.vh_test_vdh.view.*

class TestVDHViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(user: ReqresModel.ReqresUserData?) {
        Glide.with(itemView)
            .load(user?.avatar)
            .into(itemView.image_view_avatar)

        itemView.text_view_name.text = "${user?.first_name} ${user?.last_name}"
    }
}
