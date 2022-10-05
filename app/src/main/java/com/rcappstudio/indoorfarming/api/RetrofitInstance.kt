package com.rcappstudio.placesapi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    val youtubeUrl = "https://youtube.googleapis.com/youtube/v3/"
    val aiUrl = "https://plant-disease-detector-pytorch.herokuapp.com"

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