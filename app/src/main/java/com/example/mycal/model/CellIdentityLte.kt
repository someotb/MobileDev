package com.example.mycal.model

data class CellIdentityLte(
    val band: Int?,
    val cellIdentity: Int?,
    val earfcn: Int?,
    val mcc: Int?,
    val mnc: Int?,
    val pci: Int?,
    val tac: Int?
)
