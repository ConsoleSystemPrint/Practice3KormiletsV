package com.example.practice3kormiletsv.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class TestKeyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.newBuilder().setQueryParameter("key", "1").build()
        return chain.proceed(request.newBuilder().url(url).build())
    }
}
