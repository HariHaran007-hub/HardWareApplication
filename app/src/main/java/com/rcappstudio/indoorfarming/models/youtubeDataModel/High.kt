package com.rcappstudio.placesapi.youtubeDataModel


import com.google.gson.annotations.SerializedName

data class High(
    @SerializedName("height")
    var height: Int? = null,
    @SerializedName("url")
    var url: String? = null,
    @SerializedName("width")
    var width: Int? = null
)