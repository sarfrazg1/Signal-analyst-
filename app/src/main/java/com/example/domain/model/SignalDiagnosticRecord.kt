package com.example.domain.model

data class SignalDiagnosticRecord(
    val id: Long = 0,
    val timestamp: Long,
    val operatorName: String,
    val operatorBrand: String,
    val mccMnc: String,
    val networkGeneration: String,
    val simSlotIndex: Int,
    val dbm: Int?,
    val rsrp: Int?,
    val rsrq: Int?,
    val sinr: Double?,
    val qualityLabel: String,
    val cellId: Long?,
    val pci: Int?,
    val tac: Int?,
    val note: String? = null
)
