package com.rcappstudio.indoorfarming.models.dbModel

data class HealthLogModel(
    val imageUrl : String ?= null,
    val diseaseName : String ?= null,
    val inference : String ?= null
)