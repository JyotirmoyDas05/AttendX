package `in`.jyotirmoy.attendx.peer.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.jyotirmoy.attendx.peer.data.model.PeerSessionDoc
import `in`.jyotirmoy.attendx.peer.data.model.PeerSubjectPayload
import `in`.jyotirmoy.attendx.peer.data.repository.PeerSessionRepository
import `in`.jyotirmoy.attendx.peer.domain.model.MatchConfidence
import `in`.jyotirmoy.attendx.peer.domain.model.PeerSessionState
import `in`.jyotirmoy.attendx.peer.domain.model.SubjectMatchProposal
import `in`.jyotirmoy.attendx.peer.domain.usecase.BuildPeerPayloadUseCase
import `in`.jyotirmoy.attendx.peer.domain.usecase.MatchSubjectsUseCase
import `in`.jyotirmoy.attendx.peer.presentation.util.QrGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PeerComparisonViewModel @Inject constructor(
    private val peerSessionRepository: PeerSessionRepository,
    private val buildPeerPayloadUseCase: BuildPeerPayloadUseCase,
    private val matchSubjectsUseCase: MatchSubjectsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<PeerSessionState>(PeerSessionState.Idle)
    val state: StateFlow<PeerSessionState> = _state.asStateFlow()

    private var activeSessionId: String? = null
    private var cachedHostPayload: List<PeerSubjectPayload> = emptyList()
    private var cachedGuestPayload: List<PeerSubjectPayload> = emptyList()

    private var timerJob: Job? = null
    private var sessionObserverJob: Job? = null

    /**
     * Host starts a comparison session:
     * 1. Reads local subjects and attendance.
     * 2. Writes session to Firestore (status=WAITING).
     * 3. Generates styled QR bitmap with bear logo.
     * 4. Starts countdown timer and listens for guest join.
     */
    fun startHosting(
        context: Context,
        moduleColorArgb: Int,
        backgroundColorArgb: Int
    ) {
        viewModelScope.launch {
            _state.value = PeerSessionState.Joining // temporary loader

            val payload = buildPeerPayloadUseCase()
            if (payload.isEmpty()) {
                _state.value = PeerSessionState.Error(
                    "No attendance data found. Please record attendance for at least one subject first."
                )
                return@launch
            }

            cachedHostPayload = payload
            val result = peerSessionRepository.createSession(payload)

            result.onSuccess { sessionId ->
                activeSessionId = sessionId

                // Generate QR bitmap on background thread
                val qrBitmap = withContext(Dispatchers.Default) {
                    QrGenerator.generate(
                        content = sessionId,
                        context = context.applicationContext,
                        moduleColorArgb = moduleColorArgb,
                        backgroundColorArgb = backgroundColorArgb
                    )
                }

                _state.value = PeerSessionState.Hosting(
                    sessionId = sessionId,
                    qrBitmap = qrBitmap,
                    secondsRemaining = 900
                )

                startCountdownTimer(sessionId)
                observeSessionUpdates(sessionId)
            }.onFailure { e ->
                _state.value = PeerSessionState.Error(
                    e.localizedMessage ?: "Failed to create comparison session."
                )
            }
        }
    }

    /**
     * Guest initiates QR scanning viewfinder.
     */
    fun startScanning() {
        _state.value = PeerSessionState.Scanning
    }

    /**
     * Guest successfully scanned host's QR code (sessionId).
     */
    fun onQrCodeScanned(sessionId: String) {
        if (_state.value !is PeerSessionState.Scanning) return
        val cleanSessionId = sessionId.trim()

        viewModelScope.launch {
            _state.value = PeerSessionState.Joining

            val payload = buildPeerPayloadUseCase()
            if (payload.isEmpty()) {
                _state.value = PeerSessionState.Error(
                    "No attendance data found on this device. Please mark attendance for your subjects first."
                )
                return@launch
            }

            cachedGuestPayload = payload
            val result = peerSessionRepository.joinSession(cleanSessionId, payload)

            result.onSuccess { sessionDoc ->
                // Tracked so the guest erases the session too on leaving; otherwise
                // only the host ever cleans up.
                activeSessionId = cleanSessionId

                // Compute comparison directly for guest using auto/fuzzy matches
                val proposals = matchSubjectsUseCase(
                    hostSubjects = payload,
                    guestSubjects = sessionDoc.hostData
                )
                val confirmedPairs = proposals
                    .filter { it.guestSubject != null }
                    .map { it.hostSubject to it.guestSubject!! }
                val confidences = proposals
                    .filter { it.guestSubject != null }
                    .map { it.confidence }

                if (confirmedPairs.isEmpty()) {
                    _state.value = PeerSessionState.Error(
                        "No common subjects could be matched between both devices."
                    )
                } else {
                    val results = matchSubjectsUseCase.computeResults(confirmedPairs, confidences)
                    _state.value = PeerSessionState.Results(results)
                }
            }.onFailure { e ->
                _state.value = PeerSessionState.Error(
                    e.localizedMessage ?: "Failed to join session. The QR code may have expired."
                )
            }
        }
    }

    /**
     * Host updates a pairing proposal before final confirmation.
     */
    fun updateProposal(index: Int, guestSubject: PeerSubjectPayload?) {
        val current = _state.value
        if (current is PeerSessionState.PendingConfirmation) {
            val updatedList = current.proposals.toMutableList()
            if (index in updatedList.indices) {
                val old = updatedList[index]
                val newConfidence = if (guestSubject == null) MatchConfidence.FUZZY else MatchConfidence.MANUAL
                updatedList[index] = old.copy(guestSubject = guestSubject, confidence = newConfidence)
                _state.value = PeerSessionState.PendingConfirmation(updatedList)
            }
        }
    }

    /**
     * Host confirms pairings and computes results.
     */
    fun confirmPairings() {
        val current = _state.value
        if (current is PeerSessionState.PendingConfirmation) {
            val confirmedPairs = current.proposals
                .filter { it.guestSubject != null }
                .map { it.hostSubject to it.guestSubject!! }
            val confidences = current.proposals
                .filter { it.guestSubject != null }
                .map { it.confidence }

            if (confirmedPairs.isEmpty()) {
                _state.value = PeerSessionState.Error("Please match at least one subject to compare.")
                return
            }

            val results = matchSubjectsUseCase.computeResults(confirmedPairs, confidences)
            _state.value = PeerSessionState.Results(results)
        }
    }

    /**
     * Guest subjects available for manual re-pairing dropdown/sheet.
     */
    fun getAllGuestSubjects(): List<PeerSubjectPayload> = cachedGuestPayload

    /**
     * Resets state back to Idle and expires any active session.
     */
    fun cancelOrReset() {
        val sessionId = activeSessionId
        if (sessionId != null) {
            viewModelScope.launch {
                peerSessionRepository.expireSession(sessionId)
            }
        }
        timerJob?.cancel()
        sessionObserverJob?.cancel()
        activeSessionId = null
        cachedHostPayload = emptyList()
        cachedGuestPayload = emptyList()
        _state.value = PeerSessionState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        cancelOrReset()
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun startCountdownTimer(sessionId: String) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remaining = 900
            while (remaining > 0) {
                delay(1000L)
                remaining -= 1
                _state.update { current ->
                    if (current is PeerSessionState.Hosting) {
                        current.copy(secondsRemaining = remaining)
                    } else current
                }
            }
            // Expired
            peerSessionRepository.expireSession(sessionId)
            _state.value = PeerSessionState.Expired
        }
    }

    private fun observeSessionUpdates(sessionId: String) {
        sessionObserverJob?.cancel()
        sessionObserverJob = viewModelScope.launch {
            peerSessionRepository.observeSession(sessionId).collect { sessionDoc ->
                if (sessionDoc != null && sessionDoc.status == PeerSessionDoc.STATUS_PAIRED && sessionDoc.guestData.isNotEmpty()) {
                    timerJob?.cancel()
                    cachedGuestPayload = sessionDoc.guestData
                    val proposals = matchSubjectsUseCase(
                        hostSubjects = cachedHostPayload,
                        guestSubjects = sessionDoc.guestData
                    )
                    _state.value = PeerSessionState.PendingConfirmation(proposals)
                } else if (sessionDoc?.status == PeerSessionDoc.STATUS_EXPIRED) {
                    _state.value = PeerSessionState.Expired
                }
            }
        }
    }
}
