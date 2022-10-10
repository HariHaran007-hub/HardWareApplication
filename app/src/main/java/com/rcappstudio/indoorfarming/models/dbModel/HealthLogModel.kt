package com.rcappstudio.indoorfarming.models.dbModel

import android.net.MacAddress

data class HealthLogModel(
    val imageUrl : String ?= null,
    val diseaseName : String ?= null,
    val inference : String ?= null,
    val macAddress: String ?= null
)