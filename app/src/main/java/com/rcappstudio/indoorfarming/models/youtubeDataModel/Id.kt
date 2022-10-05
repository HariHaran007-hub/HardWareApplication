package com.rcappstudio.placesapi.youtubeDataModel


import com.google.gson.annotations.SerializedName

data class Id(
    @SerializedName("kind")
    var kind: String? = null,
    @SerializedName("videoId")
    var videoId: String? = null
)