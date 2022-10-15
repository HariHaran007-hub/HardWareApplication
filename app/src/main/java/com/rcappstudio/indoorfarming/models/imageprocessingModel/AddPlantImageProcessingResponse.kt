package com.rcappstudio.indoorfarming.models.imageprocessingModel


import com.google.gson.annotations.SerializedName

data class AddPlantImageProcessingResponse(
    @SerializedName("data")
    var data : String ?= null,
    @SerializedName("disease")
    var disease: String? = null,
    @SerializedName("maxEnvironmentHumidity")
    var maxEnvironmentHumidity: Double? = null,
    @SerializedName("maxEnvironmentTemperature")
    var maxEnvironmentTemperature: Double? = null,
    @SerializedName("maxLuminousIntensity")
    var maxLuminousIntensity: Int? = null,
    @SerializedName("maxSoilPh")
    var maxSoilPh: Double? = null,
    @SerializedName("maxWaterMoistureLevel")
    var maxWaterMoistureLevel: Double? = null,
    @SerializedName("minEnvironmentHumidity")
    var minEnvironmentHumidity: Double? = null,
    @SerializedName("minEnvironmentTemperature")
    var minEnvironmentTemperature: Double? = null,
    @SerializedName("minLuminousIntensity")
    var minLuminousIntensity: Double? = null,
    @SerializedName("minSoilPh")
    var minSoilPh: Double? = null,
    @SerializedName("minWaterMoistureLevel")
    var minWaterMoistureLevel: Double? = null,
    @SerializedName("plantName")
    var plantName: String? = null
)