package com.example.appclima.data.remote.api.openweathermap

import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val originalUrl = original.url()
        val newUrl = originalUrl.newBuilder()
            .addQueryParameter("appid", OpenWeather.API_KEY)
            .build()
        val request = original.newBuilder().url(newUrl).build()
        return chain.proceed(request)
    }
}