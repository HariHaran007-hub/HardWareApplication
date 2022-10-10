package com.rcappstudio.indoorfarming.models.dbModel

data class PlantModel(
    val plantName: String ?= null,
    val plantImageUrl : String ?= null,
    var pumpWater : Boolean ?= false,
    val waterMoistureLevel : Int ?= 0,
    val environmentTemperature : Double ?= 0.00,
    val soilPh : Double ?= 0.0,
    val luminousIntensity : Int ?= 0,
    val environmentHumidity : Double ?= 0.0,
    val lastWateredTimeStamp : Long ?= 0,
    val key : String ?= null,
    val maxWaterMoistureLevel : Int ?= 0,
    val minWaterMoistureLevel : Int ?= 0,
    val maxEnvironmentTemperature : Double ?= 0.00,
    val minEnvironmentTemperature : Double ?= 0.00,
    val minSoilPh : Double ?= 0.0,
    val maxSoilPh : Double?= 0.0,
    val maxLuminousIntensity : Int ?= 0,
    val minLuminousIntensity : Int ?= 0,
    val maxEnvironmentHumidity : Double ?= 0.0,
    val airQualityLevel : Double ?= 0.0,
    val minEnvironmentHumidity : Double ?= 0.0,
    val healthLog : HashMap<String , HealthLogModel>?= null


)