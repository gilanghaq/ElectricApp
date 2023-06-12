package com.example.electricapp.model

import java.io.Serializable

data class ElectricModel(
    var fillName: String? = null,
    var fillId: String? = null,
    var imageUrl: String? = null,
    var fillAddress: String? = null,
    var fillDate: String? = null,
    var fillPower: String? = null,
    var fillFirstMeter: String? = null,
    var fillLastMeter: String? = null
): Serializable