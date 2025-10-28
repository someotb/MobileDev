package com.example.mycal.model

data class CellSignalStrengthLte(
    val asuLevel: Int?,
    val cqi: Int?,
    val rsrp: Int?,
    val rsrq: Int?,
    val rssi: Int?,
    val rssnr: Int?,
    val timingAdvance: Int?
)
