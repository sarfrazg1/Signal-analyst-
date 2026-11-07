package com.example.domain.model

data class SimSlotInfo(
    val slotIndex: Int,          // 0 for SIM 1, 1 for SIM 2
    val subscriptionId: Int,
    val displayName: String,
    val carrierName: String,
    val mcc: String?,
    val mnc: String?,
    val operator: PakistaniOperator,
    val isDataDefault: Boolean,
    val isRoaming: Boolean
)
