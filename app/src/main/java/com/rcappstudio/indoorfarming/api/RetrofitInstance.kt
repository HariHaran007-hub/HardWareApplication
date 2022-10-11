package com.rcappstudio.indoorfarming.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    val youtubeUrl = "https://youtube.googleapis.com/youtube/v3/"
    val aiUrl = "https://polar-ridge-44292.herokuapp.com/"

    val api: Api by lazy {
        Retrofit.Builder()
            .baseUrl(youtubeUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Api::class.java)
    }

    val aiApi: Api by lazy {
        Retrofit.Builder()
            .baseUrl(aiUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Api::class.java)
    }
}