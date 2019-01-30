package com.kondee.testwattpadview.service

import com.kondee.testwattpadview.model.ReqresModel
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ReqresService {

    @GET("api/users")
    fun getUsers(@Query("page") page: Int): Observable<ReqresModel>
}