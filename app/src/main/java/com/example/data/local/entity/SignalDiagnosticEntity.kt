package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.SignalDiagnosticRecord

@Entity(tableName = "signal_diagnostics")
data class SignalDiagnosticEntity(
    @PrimaryKey(autoGenerate = true)
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
    val note: String?
) {
    fun toDomain(): SignalDiagnosticRecord {
        return SignalDiagnosticRecord(
            id = id,
            timestamp = timestamp,
            operatorName = operatorName,
            operatorBrand = operatorBrand,
            mccMnc = mccMnc,
            networkGeneration = networkGeneration,
            simSlotIndex = simSlotIndex,
            dbm = dbm,
            rsrp = rsrp,
            rsrq = rsrq,
            sinr = sinr,
            qualityLabel = qualityLabel,
            cellId = cellId,
            pci = pci,
            tac = tac,
            note = note
        )
    }

    companion object {
        fun fromDomain(record: SignalDiagnosticRecord): SignalDiagnosticEntity {
            return SignalDiagnosticEntity(
                id = record.id,
                timestamp = record.timestamp,
                operatorName = record.operatorName,
                operatorBrand = record.operatorBrand,
                mccMnc = record.mccMnc,
                networkGeneration = record.networkGeneration,
                simSlotIndex = record.simSlotIndex,
                dbm = record.dbm,
                rsrp = record.rsrp,
                rsrq = record.rsrq,
                sinr = record.sinr,
                qualityLabel = record.qualityLabel,
                cellId = record.cellId,
                pci = record.pci,
                tac = record.tac,
                note = record.note
            )
        }
    }
}
