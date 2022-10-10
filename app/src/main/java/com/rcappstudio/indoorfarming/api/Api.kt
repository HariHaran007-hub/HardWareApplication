package com.rcappstudio.placesapi

import com.rcappstudio.indoorfarming.models.imageprocessingModel.Data
import com.rcappstudio.indoorfarming.models.imageprocessingModel.ImageProcessingResponseData
import com.rcappstudio.placesapi.youtubeDataModel.YoutubeResults
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface Api {

    @POST("/")
    suspend fun getTodos(@Body data: Data): Response<ImageProcessingResponseData>

    @GET("search")
    suspend fun getYoutubeResults(
        @Query("part") part : String,
        @Query("q") q: String,
        @Query("key") key : String,
        @Query("maxResults") maxResults: Int ?= 20
    ) : Response<YoutubeResults>

}