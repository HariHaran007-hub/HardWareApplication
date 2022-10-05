package com.rcappstudio.placesapi.youtubeDataModel


import com.google.gson.annotations.SerializedName

data class Snippet(
    @SerializedName("channelId")
    var channelId: String? = null,
    @SerializedName("channelTitle")
    var channelTitle: String? = null,
    @SerializedName("description")
    var description: String? = null,
    @SerializedName("liveBroadcastContent")
    var liveBroadcastContent: String? = null,
    @SerializedName("publishTime")
    var publishTime: String? = null,
    @SerializedName("publishedAt")
    var publishedAt: String? = null,
    @SerializedName("thumbnails")
    var thumbnails: Thumbnails? = null,
    @SerializedName("title")
    var title: String? = null
)