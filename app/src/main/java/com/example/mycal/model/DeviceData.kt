package com.example.mycal.model

data class DeviceData(
    val location: LocationData,
    val cellInfoList: List<CellInfoLte>?
)
