package com.example.data.speedtest

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.SubscriptionManager
import com.example.domain.model.PakistaniOperator
import com.example.domain.model.SignalReading
import com.example.domain.model.SimSlotInfo
import com.example.domain.model.SpeedTestProgress
import com.example.domain.model.SpeedTestRecord
import com.example.domain.model.SpeedTestState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext
import kotlin.math.abs
import kotlin.math.round

class SpeedTestEngine(private val context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // Legitimate, documented high-speed public test endpoints with reasonable data transfer
    private val pingUrl = "https://speed.cloudflare.com/__down?bytes=0"
    private val downloadUrl = "https://speed.cloudflare.com/__down?bytes=5242880" // 5 MB payload (~5MB cellular data)
    private val uploadUrl = "https://speed.cloudflare.com/__up"

    fun isInternetAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun isConnectedToWifi(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /**
     * Checks if the target SIM slot is the active cellular data route.
     * Returns a pair of (isUsableForTesting: Boolean, explanationIfBlocked: String?)
     */
    fun validateSimDataRoute(selectedSlot: Int, simSlots: List<SimSlotInfo>): Pair<Boolean, String?> {
        val targetSim = simSlots.firstOrNull { it.slotIndex == selectedSlot }
        val isWifi = isConnectedToWifi()

        if (isWifi) {
            // Wi-Fi active: We can run speed test over Wi-Fi, but indicate cellular data is bypassed
            return Pair(true, null)
        }

        if (simSlots.size > 1 && targetSim != null) {
            val defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId()
            if (defaultDataSubId != SubscriptionManager.INVALID_SUBSCRIPTION_ID &&
                targetSim.subscriptionId != defaultDataSubId
            ) {
                val activeDataSim = simSlots.firstOrNull { it.subscriptionId == defaultDataSubId }
                val activeName = activeDataSim?.let { "SIM ${it.slotIndex + 1} (${it.operator.brandName})" } ?: "another SIM"
                return Pair(
                    false,
                    "Mobile data is currently routed through $activeName. Android does not allow routing background data through inactive SIM ${selectedSlot + 1}. Please switch Mobile Data to SIM ${selectedSlot + 1} in Android SIM Settings to benchmark this line."
                )
            }
        }

        return Pair(true, null)
    }

    fun runSpeedTest(
        targetSlot: Int,
        simSlots: List<SimSlotInfo>,
        currentSignal: SignalReading?
    ): Flow<SpeedTestProgress> = flow {
        // Step 1: Validation
        val (isRouteValid, routeBlockReason) = validateSimDataRoute(targetSlot, simSlots)
        if (!isRouteValid && routeBlockReason != null) {
            emit(
                SpeedTestProgress(
                    state = SpeedTestState.ERROR,
                    errorMessage = routeBlockReason
                )
            )
            return@flow
        }

        val network = connectivityManager.activeNetwork
        val capabilities = network?.let { connectivityManager.getNetworkCapabilities(it) }
        val hasNet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        if (!hasNet) {
            emit(
                SpeedTestProgress(
                    state = SpeedTestState.ERROR,
                    errorMessage = "No internet connection detected. Please verify mobile data or Wi-Fi is active."
                )
            )
            return@flow
        }

        val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val targetSim = simSlots.firstOrNull { it.slotIndex == targetSlot }
        val operatorName = if (isWifi) "Wi-Fi Network" else (targetSim?.operator?.brandName ?: currentSignal?.operator?.brandName ?: "Cellular")
        val operatorBrand = if (isWifi) "Wi-Fi" else (targetSim?.operator?.brandName ?: "Cellular")
        val mccMnc = if (isWifi) "N/A" else (currentSignal?.mccMncString ?: "410")
        val netGen = if (isWifi) "Wi-Fi" else (currentSignal?.networkGeneration ?: "4G LTE")

        emit(SpeedTestProgress(state = SpeedTestState.PREPARING, progressPercent = 0.05f))

        try {
            // Step 2: Ping, Jitter & Packet Loss measurement
            emit(SpeedTestProgress(state = SpeedTestState.PINGING, progressPercent = 0.10f))
            val pingResults = mutableListOf<Double>()
            var droppedProbes = 0
            val totalProbes = 6

            for (i in 1..totalProbes) {
                if (!coroutineContext.isActive) throw CancellationException()

                val startTime = System.nanoTime()
                var success = false
                try {
                    val request = Request.Builder()
                        .url(pingUrl)
                        .header("Cache-Control", "no-cache")
                        .build()
                    val response = okHttpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        val durationMs = (System.nanoTime() - startTime) / 1_000_000.0
                        pingResults.add(durationMs)
                        success = true
                    } else {
                        droppedProbes++
                    }
                    response.close()
                } catch (e: Exception) {
                    droppedProbes++
                }

                val currentAvg = if (pingResults.isNotEmpty()) pingResults.average() else null
                val progress = 0.10f + (i.toFloat() / totalProbes) * 0.15f
                emit(
                    SpeedTestProgress(
                        state = SpeedTestState.PINGING,
                        currentPingMs = currentAvg?.let { round(it * 10) / 10.0 },
                        progressPercent = progress
                    )
                )
                delay(80)
            }

            if (pingResults.isEmpty()) {
                throw IOException("Unable to reach ping test server. Please verify cellular connectivity.")
            }

            val finalPingMs = round(pingResults.minOrNull()!! * 10) / 10.0
            var jitterSum = 0.0
            if (pingResults.size > 1) {
                for (j in 1 until pingResults.size) {
                    jitterSum += abs(pingResults[j] - pingResults[j - 1])
                }
            }
            val finalJitterMs = if (pingResults.size > 1) {
                round((jitterSum / (pingResults.size - 1)) * 10) / 10.0
            } else {
                1.0
            }
            val packetLossPercent = round((droppedProbes.toDouble() / totalProbes) * 100.0 * 10) / 10.0

            emit(
                SpeedTestProgress(
                    state = SpeedTestState.DOWNLOADING,
                    currentPingMs = finalPingMs,
                    currentJitterMs = finalJitterMs,
                    currentPacketLossPercent = packetLossPercent,
                    progressPercent = 0.25f
                )
            )

            // Step 3: Download Speed Measurement (5 MB payload)
            val downloadRequest = Request.Builder()
                .url(downloadUrl)
                .header("Cache-Control", "no-cache")
                .build()

            val downloadCall = okHttpClient.newCall(downloadRequest)
            val downloadResponse = downloadCall.execute()

            if (!downloadResponse.isSuccessful) {
                downloadResponse.close()
                throw IOException("Download benchmark server returned HTTP ${downloadResponse.code}")
            }

            val body = downloadResponse.body ?: throw IOException("Empty response body from test server")
            val source = body.source()
            val buffer = ByteArray(8192)
            var totalBytesRead = 0L
            val downloadStartNano = System.nanoTime()
            var lastUpdateNano = downloadStartNano
            var measuredDownloadMbps = 0.0

            while (coroutineContext.isActive) {
                val bytesRead = source.read(buffer)
                if (bytesRead == -1) break
                totalBytesRead += bytesRead

                val now = System.nanoTime()
                val elapsedOverallSeconds = (now - downloadStartNano) / 1_000_000_000.0

                if (now - lastUpdateNano > 150_000_000L && elapsedOverallSeconds > 0.1) {
                    measuredDownloadMbps = (totalBytesRead * 8.0) / (elapsedOverallSeconds * 1_000_000.0)
                    val progressFraction = (totalBytesRead.toFloat() / 5_242_880f).coerceIn(0f, 1f)
                    val progress = 0.25f + (progressFraction * 0.40f)

                    emit(
                        SpeedTestProgress(
                            state = SpeedTestState.DOWNLOADING,
                            currentPingMs = finalPingMs,
                            currentJitterMs = finalJitterMs,
                            currentPacketLossPercent = packetLossPercent,
                            currentDownloadMbps = round(measuredDownloadMbps * 10) / 10.0,
                            progressPercent = progress,
                            bytesTransferred = totalBytesRead
                        )
                    )
                    lastUpdateNano = now
                }
            }
            body.close()

            val finalDownloadDuration = (System.nanoTime() - downloadStartNano) / 1_000_000_000.0
            if (finalDownloadDuration > 0.05 && totalBytesRead > 0) {
                measuredDownloadMbps = (totalBytesRead * 8.0) / (finalDownloadDuration * 1_000_000.0)
            }
            val finalDownloadMbps = round(measuredDownloadMbps * 10) / 10.0

            // Step 4: Upload Speed Measurement (1.5 MB payload buffer)
            emit(
                SpeedTestProgress(
                    state = SpeedTestState.UPLOADING,
                    currentPingMs = finalPingMs,
                    currentJitterMs = finalJitterMs,
                    currentPacketLossPercent = packetLossPercent,
                    currentDownloadMbps = finalDownloadMbps,
                    progressPercent = 0.65f,
                    bytesTransferred = totalBytesRead
                )
            )

            val uploadPayloadSize = 1_572_864 // 1.5 MB payload for cellular speed test
            val payloadBytes = ByteArray(uploadPayloadSize) { (it % 256).toByte() }
            var totalBytesWritten = 0L
            val uploadStartNano = System.nanoTime()
            var lastUploadUpdateNano = uploadStartNano
            var measuredUploadMbps = 0.0

            val countingBody = object : RequestBody() {
                override fun contentType() = "application/octet-stream".toMediaType()
                override fun contentLength() = payloadBytes.size.toLong()

                override fun writeTo(sink: BufferedSink) {
                    var offset = 0
                    val chunkSize = 8192
                    while (offset < payloadBytes.size) {
                        val toWrite = minOf(chunkSize, payloadBytes.size - offset)
                        sink.write(payloadBytes, offset, toWrite)
                        sink.flush()
                        offset += toWrite
                        totalBytesWritten = offset.toLong()

                        val now = System.nanoTime()
                        val elapsedSeconds = (now - uploadStartNano) / 1_000_000_000.0
                        if (now - lastUploadUpdateNano > 150_000_000L && elapsedSeconds > 0.1) {
                            measuredUploadMbps = (totalBytesWritten * 8.0) / (elapsedSeconds * 1_000_000.0)
                            lastUploadUpdateNano = now
                        }
                    }
                }
            }

            val uploadRequest = Request.Builder()
                .url(uploadUrl)
                .post(countingBody)
                .build()

            val uploadResponse = okHttpClient.newCall(uploadRequest).execute()
            uploadResponse.close()

            val finalUploadDuration = (System.nanoTime() - uploadStartNano) / 1_000_000_000.0
            if (finalUploadDuration > 0.05 && totalBytesWritten > 0) {
                measuredUploadMbps = (totalBytesWritten * 8.0) / (finalUploadDuration * 1_000_000.0)
            }
            val finalUploadMbps = round(measuredUploadMbps * 10) / 10.0

            // Step 5: Completed record creation
            val completedRecord = SpeedTestRecord(
                timestamp = System.currentTimeMillis(),
                operatorName = operatorName,
                operatorBrand = operatorBrand,
                mccMnc = mccMnc,
                simSlotIndex = targetSlot,
                networkGeneration = netGen,
                downloadMbps = finalDownloadMbps,
                uploadMbps = finalUploadMbps,
                pingMs = finalPingMs,
                jitterMs = finalJitterMs,
                packetLossPercent = packetLossPercent,
                signalDbm = currentSignal?.rawDbm,
                signalRsrp = currentSignal?.lteRsrp,
                isWifi = isWifi
            )

            emit(
                SpeedTestProgress(
                    state = SpeedTestState.COMPLETED,
                    currentPingMs = finalPingMs,
                    currentJitterMs = finalJitterMs,
                    currentPacketLossPercent = packetLossPercent,
                    currentDownloadMbps = finalDownloadMbps,
                    currentUploadMbps = finalUploadMbps,
                    progressPercent = 1.0f,
                    bytesTransferred = totalBytesRead + totalBytesWritten,
                    completedRecord = completedRecord
                )
            )

        } catch (e: CancellationException) {
            emit(SpeedTestProgress(state = SpeedTestState.CANCELLED))
            throw e
        } catch (e: UnknownHostException) {
            emit(
                SpeedTestProgress(
                    state = SpeedTestState.ERROR,
                    errorMessage = "DNS resolution failed. No active internet route available."
                )
            )
        } catch (e: IOException) {
            emit(
                SpeedTestProgress(
                    state = SpeedTestState.ERROR,
                    errorMessage = "Network benchmark error: ${e.localizedMessage ?: "Connection interrupted"}"
                )
            )
        } catch (e: Exception) {
            emit(
                SpeedTestProgress(
                    state = SpeedTestState.ERROR,
                    errorMessage = "Test error: ${e.localizedMessage ?: "Unexpected error"}"
                )
            )
        }
    }.flowOn(Dispatchers.IO)
}
