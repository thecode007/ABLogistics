package org.safieddine.ablogistics.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object NotificationManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var listenJob: Job? = null
    private var running = false
    private var lastBaseUrl: String? = null
    private var lastJwt: String? = null

    private val _blocked = MutableStateFlow(false)
    val blocked: StateFlow<Boolean> = _blocked

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    private val _lastEvent = MutableStateFlow<NotificationEvent?>(null)
    val lastEvent: StateFlow<NotificationEvent?> = _lastEvent

    private val _pendingDecisions = MutableStateFlow(0)
    val pendingDecisions: StateFlow<Int> = _pendingDecisions

    private fun log(message: String) {
        println("[NotificationManager] $message")
    }

    private fun maskToken(token: String?): String = when {
        token.isNullOrBlank() -> "null"
        token.length <= 8 -> "***"
        else -> "***" + token.takeLast(6)
    }

    fun clearLastEvent() {
        _lastEvent.value = null
        log("Cleared lastEvent")
    }

    fun startAfterLogin(baseUrl: String, jwt: String, username: String) {
        log("startAfterLogin called; running=$running, user=$username, baseUrl=$baseUrl, jwt=${maskToken(jwt)}")

        if (running) {
            log("Already running; ignoring start request")
            return
        }
        running = true
        log("Marked running=true")
        // server registers token during login; just init tray and start SSE
        TrayNotifier.init()
        log("Tray initialized")
        // Setup JWT refresh hook: validate and reuse token if still valid
        NotificationSseClient.onUnauthorizedRefresh = {
            log("401 from SSE; attempting to refresh/validate token")
            try {
                val authValid = AuthManager.validateCurrentToken()
                if (authValid) {
                    val t = org.safieddine.ablogistics.data.session.SessionStore.token.value
                    log("Token still valid; reusing ${maskToken(t)}")
                    t
                } else {
                    log("Token invalid; no refresh available")
                    null
                }
            } catch (_: Exception) { null }
        }
        listenJob = scope.launch {
            log("Launching SSE listen loop")
            NotificationSseClient.listen(baseUrl, jwt) { name, evt ->
                log("Event received; name=$name, type=${evt.type}, title=${evt.title}, dataKeys=${evt.data.keys}")
                when (name) {
                    "connected" -> {
                        log("SSE connected")
                        _connected.value = true
                    }
                    "disconnected" -> {
                        _connected.value = false
                        log("SSE disconnected")
                    }
                    "ACCOUNT_STATUS" -> {
                        TrayNotifier.show(evt.title, evt.message ?: evt.body ?: "")
                        val status = evt.data["status"]?.lowercase()
                        log("User marked as blocked from server event $status")


                        if (status == "blocked") {
                            _blocked.value = true
                        }
                        else {
                            _blocked.value = false
                        }
                    }
                    else -> {
                        TrayNotifier.show(evt.title, evt.message ?: evt.body ?: "")
                        // If a DECISION-related event arrives, refresh pending count
                        val t = evt.type.lowercase()
                        if (t.contains("decision")) {
                            refreshPendingDecisions()
                        }
                    }
                }
                _lastEvent.value = evt
                log("lastEvent updated")
            }
        }

        // Initial and periodic refresh of pending decisions count
        scope.launch {
            refreshPendingDecisions()
            while (running) {
                try { kotlinx.coroutines.delay(60_000) } catch (_: Exception) {}
                if (!running) break
                refreshPendingDecisions()
            }
        }
    }

    fun stop() {
        log("stop() called; stopping listener and clearing state")
        listenJob?.cancel()
        log("SSE listen job cancelled")
        listenJob = null
        running = false
        log("Marked running=false")
        _blocked.value = false
        log("Cleared blocked state")
        _connected.value = false
        log("Marked connected=false")
        lastBaseUrl = null
        lastJwt = null
        _pendingDecisions.value = 0
    }

    fun stopOnLogout(baseUrl: String, jwt: String) {
        // server unregisters token during logout; just stop listener
        log("stopOnLogout called; baseUrl=$baseUrl, jwt=${maskToken(jwt)}")
        stop()
    }

    fun updatePendingCount(count: Int) {
        _pendingDecisions.value = count
    }

    fun refreshPendingDecisions() {
        scope.launch {
            try {
                val res = org.safieddine.ablogistics.data.service.NotificationService.listPendingDecisions(limit = 1000)
                if (res.isSuccess) {
                    val count = res.getOrNull()?.data?.size ?: 0
                    _pendingDecisions.value = count
                    log("Pending decisions count updated to $count")
                }
            } catch (_: Exception) {}
        }
    }
}
