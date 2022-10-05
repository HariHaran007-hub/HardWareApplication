package com.rcappstudio.placesapi.youtubeDataModel


import com.google.gson.annotations.SerializedName

data class PageInfo(
    @SerializedName("resultsPerPage")
    var resultsPerPage: Int? = null,
    @SerializedName("totalResults")
    var totalResults: Int? = null
)