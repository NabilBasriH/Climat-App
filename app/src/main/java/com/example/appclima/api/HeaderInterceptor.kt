package com.example.appclima.api

import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val originalUrl = original.url()

        val newRequest = if (originalUrl.toString().contains("openuv.io")) {
            original.newBuilder()
                .addHeader("x-access-token", OpenUv.API_KEY)
                .addHeader("Content-Type", "application/json")
                .build()
        } else {
            original.newBuilder()
                .url(
                    originalUrl.newBuilder()
                        .addQueryParameter("appid", OpenWeather.API_KEY)
                        .build()
                )
                .build()

        }
        return chain.proceed(newRequest)
    }
}