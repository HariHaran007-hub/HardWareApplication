package com.rcappstudio.placesapi.youtubeDataModel


import com.google.gson.annotations.SerializedName

data class Item(
    @SerializedName("etag")
    var etag: String? = null,
    @SerializedName("id")
    var id: Id? = null,
    @SerializedName("kind")
    var kind: String? = null,
    @SerializedName("snippet")
    var snippet: Snippet? = null
)