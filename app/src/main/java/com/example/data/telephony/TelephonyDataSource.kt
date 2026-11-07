package com.example.data.telephony

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.telephony.CellIdentityGsm
import android.telephony.CellIdentityLte
import android.telephony.CellIdentityNr
import android.telephony.CellIdentityWcdma
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoWcdma
import android.telephony.CellSignalStrengthGsm
import android.telephony.CellSignalStrengthLte
import android.telephony.CellSignalStrengthNr
import android.telephony.CellSignalStrengthWcdma
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.example.domain.model.PakistaniOperator
import com.example.domain.model.SignalQuality
import com.example.domain.model.SignalReading
import com.example.domain.model.SimSlotInfo
import com.example.domain.model.TelephonyStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.Executor

class TelephonyDataSource(private val context: Context) {

    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager

    fun hasPermissions(): Boolean {
        val fineLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val phoneState = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        return fineLoc && phoneState
    }

    fun isAirplaneModeOn(): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
        } catch (_: Exception) {
            false
        }
    }

    fun isLocationEnabled(): Boolean {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                    locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
        } catch (_: Exception) {
            true
        }
    }

    fun observeSimSlots(): Flow<List<SimSlotInfo>> = callbackFlow {
        fun updateSims() {
            if (!hasPermissions()) {
                trySend(emptyList())
                return
            }
            try {
                val subList: List<SubscriptionInfo>? = subscriptionManager?.activeSubscriptionInfoList
                if (subList.isNullOrEmpty()) {
                    // Check if SIM is physically absent
                    trySend(emptyList())
                    return
                }

                val result = subList.map { subInfo ->
                    val mcc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        subInfo.mccString
                    } else {
                        @Suppress("DEPRECATION")
                        subInfo.mcc.takeIf { it != 0 }?.toString()
                    }

                    val mnc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        subInfo.mncString
                    } else {
                        @Suppress("DEPRECATION")
                        subInfo.mnc.takeIf { it != 0 }?.toString()
                    }

                    val carrierName = subInfo.carrierName?.toString() ?: ""
                    val displayName = subInfo.displayName?.toString() ?: "SIM ${subInfo.simSlotIndex + 1}"
                    val operator = PakistaniOperator.fromMccMnc(mcc, mnc, carrierName)

                    SimSlotInfo(
                        slotIndex = subInfo.simSlotIndex,
                        subscriptionId = subInfo.subscriptionId,
                        displayName = displayName,
                        carrierName = carrierName,
                        mcc = mcc,
                        mnc = mnc,
                        operator = operator,
                        isDataDefault = (SubscriptionManager.getDefaultDataSubscriptionId() == subInfo.subscriptionId),
                        isRoaming = subInfo.dataRoaming == SubscriptionManager.DATA_ROAMING_ENABLE
                    )
                }.sortedBy { it.slotIndex }

                trySend(result)
            } catch (e: SecurityException) {
                trySend(emptyList())
            } catch (e: Exception) {
                trySend(emptyList())
            }
        }

        updateSims()

        val subListener = object : SubscriptionManager.OnSubscriptionsChangedListener() {
            override fun onSubscriptionsChanged() {
                updateSims()
            }
        }

        try {
            subscriptionManager?.addOnSubscriptionsChangedListener(subListener)
        } catch (_: Exception) {}

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction("android.intent.action.SIM_STATE_CHANGED")
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                updateSims()
            }
        }
        try {
            context.registerReceiver(receiver, filter)
        } catch (_: Exception) {}

        awaitClose {
            try {
                subscriptionManager?.removeOnSubscriptionsChangedListener(subListener)
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    fun observeTelephonyStatus(): Flow<TelephonyStatus> = callbackFlow {
        fun checkStatus() {
            if (!hasPermissions()) {
                trySend(TelephonyStatus.PERMISSION_REQUIRED)
                return
            }
            if (isAirplaneModeOn()) {
                trySend(TelephonyStatus.AIRPLANE_MODE)
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isLocationEnabled()) {
                trySend(TelephonyStatus.LOCATION_SERVICES_DISABLED)
                return
            }

            try {
                val subList = subscriptionManager?.activeSubscriptionInfoList
                if (subList.isNullOrEmpty()) {
                    if (telephonyManager.simState == TelephonyManager.SIM_STATE_ABSENT) {
                        trySend(TelephonyStatus.NO_SIM)
                    } else {
                        trySend(TelephonyStatus.NO_SIGNAL)
                    }
                    return
                }
                trySend(TelephonyStatus.NORMAL)
            } catch (_: SecurityException) {
                trySend(TelephonyStatus.PERMISSION_REQUIRED)
            } catch (_: Exception) {
                trySend(TelephonyStatus.NORMAL)
            }
        }

        checkStatus()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction("android.intent.action.SIM_STATE_CHANGED")
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                checkStatus()
            }
        }

        try {
            context.registerReceiver(receiver, filter)
        } catch (_: Exception) {}

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    fun observeRealSignal(slotIndex: Int): Flow<SignalReading?> = callbackFlow {
        if (!hasPermissions() || isAirplaneModeOn()) {
            trySend(null)
        }

        fun scanAndEmit() {
            if (!hasPermissions() || isAirplaneModeOn()) {
                trySend(null)
                return
            }

            try {
                val subList = subscriptionManager?.activeSubscriptionInfoList
                val targetSub = subList?.firstOrNull { it.simSlotIndex == slotIndex }
                    ?: subList?.firstOrNull()

                val targetSubId = targetSub?.subscriptionId
                val targetTelephony = if (targetSubId != null && targetSubId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    telephonyManager.createForSubscriptionId(targetSubId)
                } else {
                    telephonyManager
                }

                val mcc = if (targetSub != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    targetSub.mccString
                } else {
                    targetTelephony.networkOperator?.takeIf { it.length >= 3 }?.substring(0, 3)
                }

                val mnc = if (targetSub != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    targetSub.mncString
                } else {
                    targetTelephony.networkOperator?.takeIf { it.length > 3 }?.substring(3)
                }

                val carrierName = targetSub?.carrierName?.toString()
                    ?: targetTelephony.networkOperatorName
                    ?: targetTelephony.simOperatorName
                    ?: "Cellular"

                val operator = PakistaniOperator.fromMccMnc(mcc, mnc, carrierName)
                val isRoaming = targetSub?.dataRoaming == SubscriptionManager.DATA_ROAMING_ENABLE || targetTelephony.isNetworkRoaming

                // Query cell infos
                var cellList: List<CellInfo>? = null
                try {
                    cellList = targetTelephony.allCellInfo
                } catch (_: SecurityException) {}

                val registeredCell = cellList?.firstOrNull { it.isRegistered } ?: cellList?.firstOrNull()

                // Query SignalStrength
                var signalStrengthObj: SignalStrength? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        signalStrengthObj = targetTelephony.signalStrength
                    } catch (_: Exception) {}
                }

                val reading = extractSignalReading(
                    targetTelephony = targetTelephony,
                    registeredCell = registeredCell,
                    signalStrengthObj = signalStrengthObj,
                    operator = operator,
                    operatorName = carrierName,
                    mcc = mcc,
                    mnc = mnc,
                    slotIndex = slotIndex,
                    subscriptionId = targetSubId,
                    isRoaming = isRoaming
                )

                trySend(reading)
            } catch (e: SecurityException) {
                trySend(null)
            } catch (e: Exception) {
                trySend(null)
            }
        }

        scanAndEmit()

        // Register hardware callback listener
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val executor = ContextCompat.getMainExecutor(context)
            val callback = object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener, TelephonyCallback.CellInfoListener {
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                    scanAndEmit()
                }

                override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>) {
                    scanAndEmit()
                }
            }

            try {
                telephonyManager.registerTelephonyCallback(executor, callback)
            } catch (_: Exception) {}

            awaitClose {
                try {
                    telephonyManager.unregisterTelephonyCallback(callback)
                } catch (_: Exception) {}
            }
        } else {
            @Suppress("DEPRECATION")
            val listener = object : PhoneStateListener() {
                @Deprecated("Deprecated in Java")
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
                    scanAndEmit()
                }

                @Deprecated("Deprecated in Java")
                override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
                    scanAndEmit()
                }
            }

            try {
                @Suppress("DEPRECATION")
                telephonyManager.listen(
                    listener,
                    PhoneStateListener.LISTEN_SIGNAL_STRENGTHS or PhoneStateListener.LISTEN_CELL_INFO
                )
            } catch (_: Exception) {}

            awaitClose {
                try {
                    @Suppress("DEPRECATION")
                    telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE)
                } catch (_: Exception) {}
            }
        }
    }

    private fun extractSignalReading(
        targetTelephony: TelephonyManager,
        registeredCell: CellInfo?,
        signalStrengthObj: SignalStrength?,
        operator: PakistaniOperator,
        operatorName: String,
        mcc: String?,
        mnc: String?,
        slotIndex: Int,
        subscriptionId: Int?,
        isRoaming: Boolean
    ): SignalReading {
        var rawDbm: Int? = null
        var lteRsrp: Int? = null
        var lteRsrq: Int? = null
        var lteSinr: Double? = null
        var lteCqi: Int? = null
        var lteTimingAdvance: Int? = null
        var cellId: Long? = null
        var pci: Int? = null
        var tac: Int? = null
        var earfcn: Int? = null
        var generation = "4G LTE"
        var techDetails = "LTE"
        var isRegistered = false

        // Extract from Registered CellInfo if available
        if (registeredCell != null) {
            isRegistered = registeredCell.isRegistered
            when (registeredCell) {
                is CellInfoLte -> {
                    generation = "4G LTE"
                    techDetails = "LTE"
                    val strength = registeredCell.cellSignalStrength
                    val identity = registeredCell.cellIdentity

                    rawDbm = cleanMeasurement(strength.dbm)
                    lteRsrp = cleanMeasurement(strength.rsrp)
                    lteRsrq = cleanMeasurement(strength.rsrq)
                    
                    val rssnr = strength.rssnr
                    if (rssnr != Int.MAX_VALUE && rssnr != 0) {
                        lteSinr = rssnr / 10.0 // rssnr in android is reported in units of 0.1 dB
                    }
                    if (strength.cqi != Int.MAX_VALUE && strength.cqi in 1..15) {
                        lteCqi = strength.cqi
                    }
                    if (strength.timingAdvance != Int.MAX_VALUE && strength.timingAdvance >= 0) {
                        lteTimingAdvance = strength.timingAdvance
                    }

                    if (identity.ci != Int.MAX_VALUE && identity.ci > 0) cellId = identity.ci.toLong()
                    if (identity.pci != Int.MAX_VALUE && identity.pci >= 0) pci = identity.pci
                    if (identity.tac != Int.MAX_VALUE && identity.tac > 0) tac = identity.tac
                    if (identity.earfcn != Int.MAX_VALUE && identity.earfcn > 0) earfcn = identity.earfcn
                }
                is CellInfoNr -> {
                    generation = "5G NR"
                    techDetails = "NR (New Radio)"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val strength = registeredCell.cellSignalStrength as? CellSignalStrengthNr
                        if (strength != null) {
                            rawDbm = cleanMeasurement(strength.dbm)
                            val ssRsrp = cleanMeasurement(strength.ssRsrp)
                            val csiRsrp = cleanMeasurement(strength.csiRsrp)
                            lteRsrp = ssRsrp ?: csiRsrp
                            val ssRsrq = cleanMeasurement(strength.ssRsrq)
                            val csiRsrq = cleanMeasurement(strength.csiRsrq)
                            lteRsrq = ssRsrq ?: csiRsrq
                            val ssSinr = strength.ssSinr
                            if (ssSinr != Int.MAX_VALUE && ssSinr != 0) {
                                lteSinr = ssSinr.toDouble()
                            }
                        }
                        val identity = registeredCell.cellIdentity as? CellIdentityNr
                        if (identity != null) {
                            if (identity.nci != Long.MAX_VALUE && identity.nci > 0) cellId = identity.nci
                            if (identity.pci != Int.MAX_VALUE && identity.pci >= 0) pci = identity.pci
                            if (identity.tac != Int.MAX_VALUE && identity.tac > 0) tac = identity.tac
                            if (identity.nrarfcn != Int.MAX_VALUE && identity.nrarfcn > 0) earfcn = identity.nrarfcn
                        }
                    }
                }
                is CellInfoWcdma -> {
                    generation = "3G UMTS/HSPA"
                    techDetails = "WCDMA / HSPA"
                    val strength = registeredCell.cellSignalStrength
                    val identity = registeredCell.cellIdentity
                    rawDbm = cleanMeasurement(strength.dbm)
                    if (identity.cid != Int.MAX_VALUE && identity.cid > 0) cellId = identity.cid.toLong()
                    if (identity.lac != Int.MAX_VALUE && identity.lac > 0) tac = identity.lac
                    if (identity.psc != Int.MAX_VALUE && identity.psc > 0) pci = identity.psc
                }
                is CellInfoGsm -> {
                    generation = "2G GSM"
                    techDetails = "GSM / EDGE"
                    val strength = registeredCell.cellSignalStrength
                    val identity = registeredCell.cellIdentity
                    rawDbm = cleanMeasurement(strength.dbm)
                    if (identity.cid != Int.MAX_VALUE && identity.cid > 0) cellId = identity.cid.toLong()
                    if (identity.lac != Int.MAX_VALUE && identity.lac > 0) tac = identity.lac
                }
            }
        }

        // Secondary fallback from SignalStrength object if CellInfo didn't give numbers
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && signalStrengthObj != null) {
            val cellSignalStrengths = signalStrengthObj.cellSignalStrengths
            for (css in cellSignalStrengths) {
                if (css is CellSignalStrengthLte) {
                    if (rawDbm == null) rawDbm = cleanMeasurement(css.dbm)
                    if (lteRsrp == null) lteRsrp = cleanMeasurement(css.rsrp)
                    if (lteRsrq == null) lteRsrq = cleanMeasurement(css.rsrq)
                    if (lteSinr == null && css.rssnr != Int.MAX_VALUE && css.rssnr != 0) {
                        lteSinr = css.rssnr / 10.0
                    }
                    if (lteCqi == null && css.cqi != Int.MAX_VALUE && css.cqi in 1..15) {
                        lteCqi = css.cqi
                    }
                    if (lteTimingAdvance == null && css.timingAdvance != Int.MAX_VALUE && css.timingAdvance >= 0) {
                        lteTimingAdvance = css.timingAdvance
                    }
                } else if (css is CellSignalStrengthNr && rawDbm == null) {
                    rawDbm = cleanMeasurement(css.dbm)
                    lteRsrp = cleanMeasurement(css.ssRsrp) ?: cleanMeasurement(css.csiRsrp)
                    lteRsrq = cleanMeasurement(css.ssRsrq) ?: cleanMeasurement(css.csiRsrq)
                } else if (rawDbm == null) {
                    rawDbm = cleanMeasurement(css.dbm)
                }
            }
        }

        // Resolve generation from Telephony dataNetworkType if still ambiguous
        try {
            @Suppress("DEPRECATION")
            val networkType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    targetTelephony.dataNetworkType
                } catch (_: SecurityException) {
                    targetTelephony.networkType
                }
            } else {
                targetTelephony.networkType
            }

            when (networkType) {
                TelephonyManager.NETWORK_TYPE_NR -> {
                    generation = "5G NR"
                    techDetails = "NR"
                }
                TelephonyManager.NETWORK_TYPE_LTE -> {
                    generation = "4G LTE"
                    techDetails = "LTE"
                }
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_UMTS -> {
                    generation = "3G HSPA/UMTS"
                    techDetails = "3G"
                }
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_GSM -> {
                    generation = "2G GSM"
                    techDetails = "2G"
                }
            }
        } catch (_: Exception) {}

        // Evaluate Signal Quality
        val quality = SignalQuality.evaluate(lteRsrp, rawDbm)

        return SignalReading(
            operator = operator,
            operatorName = operatorName.ifBlank { operator.brandName },
            mcc = mcc,
            mnc = mnc,
            networkGeneration = generation,
            networkTypeDetails = techDetails,
            rawDbm = rawDbm,
            lteRsrp = lteRsrp,
            lteRsrq = lteRsrq,
            lteSinr = lteSinr,
            lteCqi = lteCqi,
            lteTimingAdvance = lteTimingAdvance,
            cellId = cellId,
            pci = pci,
            tac = tac,
            earfcn = earfcn,
            quality = quality,
            simSlotIndex = slotIndex,
            subscriptionId = subscriptionId,
            isRegistered = isRegistered,
            isRoaming = isRoaming,
            timestamp = System.currentTimeMillis(),
            isActualHardwareReading = true
        )
    }

    private fun cleanMeasurement(value: Int?): Int? {
        if (value == null) return null
        if (value == Int.MAX_VALUE || value == 0 || value > 0) return null
        if (value < -160) return null // Out of valid cellular dBm range
        return value
    }
}
