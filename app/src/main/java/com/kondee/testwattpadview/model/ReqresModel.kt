package com.kondee.testwattpadview.model

class ReqresModel {

    val page: Int = 0
    val per_page: Int = 0
    val total: Int = 0
    val total_pages: Int = 0

    val data: List<ReqresUserData>? = null

    class ReqresUserData {
        val id: Int = 0
        val first_name: String = ""
        val last_name: String = ""
        val avatar: String = ""
    }
}

