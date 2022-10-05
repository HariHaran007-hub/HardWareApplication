package com.rcappstudio.indoorfarming.models.imageprocessingModel


import com.google.gson.annotations.SerializedName

data class ImageProcessingResponseData(
    @SerializedName("disease")
    var disease: String? = null,
    @SerializedName("plant")
    var plant: String? = null,
    @SerializedName("remedy")
    var remedy: String? = null
)