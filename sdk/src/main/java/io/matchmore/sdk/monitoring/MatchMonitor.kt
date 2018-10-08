package io.matchmore.sdk.monitoring

import io.matchmore.sdk.AlpsManager
import io.matchmore.sdk.Matchmore
import io.matchmore.sdk.MatchmoreConfig
import io.matchmore.sdk.api.ApiClient
import io.matchmore.sdk.api.async
import io.matchmore.sdk.api.models.Device
import io.matchmore.sdk.api.models.Match
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.util.*

typealias MatchMonitorListener = (Set<Match>, Device) -> Unit

class MatchMonitor(private val manager: AlpsManager, private val config: MatchmoreConfig) {
    val deliveredMatches = mutableSetOf<Match>()

    private val monitoredDevices = mutableSetOf<Device>()
    private val socketListener = MatchSocketListener()

    private var listeners = mutableSetOf<MatchMonitorListener>()

    private var timer: Timer? = null

    private var socket: WebSocket? = null

    fun addOnMatchListener(listener: MatchMonitorListener) {
        listeners.add(listener)
    }

    fun removeOnMatchListener(listener: MatchMonitorListener) {
        listeners.remove(listener)
    }

    // TODO: start new socket after adding new device ?
    fun openSocketForMatches() {
        if (socket != null) {
            return
        }
        val deviceId = Matchmore.instance.devices.findAll().first().id
        val url = ApiClient.config.serverUrl
        val request = Request.Builder().url("ws://$url/pusher/${ApiClient.config.version}/ws/$deviceId")
                .header("Sec-WebSocket-Protocol", "api-key,${config.worldId}").build()
        val client = OkHttpClient()
        socketListener.onMessage = { text ->
            if (text != "" && text != "ping" && text != "pong") {
                getMatches()
            }
        }
        socketListener.onClosed = { _, _ ->
            if (socket != null) {
                openSocket(client, request)
            }
        }
        openSocket(client, request)
    }

    private fun openSocket(client: OkHttpClient, request: Request) {
        socket = client.newWebSocket(request, socketListener)
        client.dispatcher().executorService().shutdown()
    }

    fun closeSocketForMatches() {
        socket?.close(1001, "closed by SDK")
        socket = null
    }

    fun startMonitoringFor(device: Device) {
        monitoredDevices.add(device)
    }

    fun stopMonitoringFor(device: Device) {
        monitoredDevices.remove(device)
    }

    @JvmOverloads
    fun startPollingMatches(pollingIntervalMs: Long = DEFAULT_POLLING_INTERVAL_MS) {
        if (timer != null) {
            return
        }
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                getMatches()
            }
        }, 0, pollingIntervalMs)
    }

    fun stopPollingMatches() {
        timer?.cancel()
        timer = null
    }

    fun onReceiveMatchUpdate() {
        monitoredDevices.forEach { getMatchesForDevice(it) }
    }

    private fun getMatches() = monitoredDevices.forEach { getMatchesForDevice(it) }

    private fun getMatchesForDevice(device: Device) {
        device.id?.let { id ->
            manager.apiClient.matchesApi.getMatches(id).async({ matches ->
                val newMatches = matches - deliveredMatches
                if (newMatches.isNotEmpty()) {
                    deliveredMatches.addAll(newMatches)
                    listeners.forEach {
                        it.invoke(newMatches.toSet(), device)
                    }
                }
            }, null)
        }
    }

    companion object {
        private const val DEFAULT_POLLING_INTERVAL_MS: Long = 5000
    }
}
