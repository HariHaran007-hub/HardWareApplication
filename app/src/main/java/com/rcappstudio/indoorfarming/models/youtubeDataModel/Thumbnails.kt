package com.rcappstudio.placesapi.youtubeDataModel


import com.google.gson.annotations.SerializedName

data class Thumbnails(
    @SerializedName("default")
    var default: Default? = null,
    @SerializedName("high")
    var high: High? = null,
    @SerializedName("medium")
    var medium: Medium? = null
)