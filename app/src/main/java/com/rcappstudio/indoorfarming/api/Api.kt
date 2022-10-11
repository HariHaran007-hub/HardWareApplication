package com.rcappstudio.indoorfarming.api

import com.rcappstudio.indoorfarming.models.imageprocessingModel.AddPlantImageProcessingResponse
import com.rcappstudio.indoorfarming.models.imageprocessingModel.Data
import com.rcappstudio.indoorfarming.models.imageprocessingModel.ImageProcessingResponseData
import com.rcappstudio.placesapi.youtubeDataModel.YoutubeResults
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.*

interface Api {

    @Headers("Content-Type: application/json")
    @POST("addHealthLog")
    suspend fun getHealthLog(@Body data: Data): Response<ImageProcessingResponseData>

    @GET("search")
    suspend fun getYoutubeResults(
        @Query("part") part : String,
        @Query("q") q: String,
        @Query("key") key : String,
        @Query("maxResults") maxResults: Int ?= 20
    ) : Response<YoutubeResults>

    @Headers("Content-Type: application/json")
    @POST("addNewPlant")
    suspend fun addPlantGetData(@Body data : Data) : Response<AddPlantImageProcessingResponse>



}