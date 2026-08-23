package `in`.jyotirmoy.attendx.peer.domain.usecase

import `in`.jyotirmoy.attendx.peer.data.model.PeerSubjectPayload
import `in`.jyotirmoy.attendx.peer.domain.model.MatchConfidence
import `in`.jyotirmoy.attendx.peer.domain.model.SubjectComparisonResult
import `in`.jyotirmoy.attendx.peer.domain.model.SubjectMatchProposal
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class MatchSubjectsUseCase @Inject constructor() {

    /**
     * Produces a list of match proposals given two subject lists.
     * Priority order per host subject:
     *   1. Exact subjectCode match        → AUTO_CODE  (score 1.0)
     *   2. Exact normalized name match    → AUTO_NAME  (score 0.95)
     *   3. Jaccard token overlap ≥ 0.55   → FUZZY      (score = overlap)
     *   4. Edit distance ratio ≤ 0.35     → FUZZY      (score = 1 - ratio)
     *   5. No match found                 → null guest (score 0)
     */
    operator fun invoke(
        hostSubjects: List<PeerSubjectPayload>,
        guestSubjects: List<PeerSubjectPayload>
    ): List<SubjectMatchProposal> {
        val remainingGuests = guestSubjects.toMutableList()
        val proposals = mutableListOf<SubjectMatchProposal>()

        for (host in hostSubjects) {
            val (matched, confidence, score) = findBestMatch(host, remainingGuests)
            proposals.add(SubjectMatchProposal(host, matched, confidence, score))
            if (matched != null) remainingGuests.remove(matched)
        }

        return proposals
    }

    /**
     * Converts confirmed pairs into final per-subject comparison results.
     */
    fun computeResults(
        confirmedPairs: List<Pair<PeerSubjectPayload, PeerSubjectPayload>>,
        confidence: List<MatchConfidence>
    ): List<SubjectComparisonResult> {
        return confirmedPairs.mapIndexed { i, (host, guest) ->
            val delta = host.attendancePercentage - guest.attendancePercentage
            SubjectComparisonResult(
                label            = host.originalName.ifBlank { host.normalizedName },
                hostPercentage   = host.attendancePercentage,
                guestPercentage  = guest.attendancePercentage,
                delta            = delta,
                confidence       = confidence.getOrElse(i) { MatchConfidence.MANUAL }
            )
        }
    }

    // ── Normalizer ────────────────────────────────────────────────────────────

    fun normalize(name: String): String = name
        .lowercase()
        .replace(Regex("[^a-z0-9 ]"), " ")
        .replace(
            Regex("\\b(lab|theory|dr|prof|practical|tut|tutorial|and|the|of|to|in|for|a)\\b"),
            " "
        )
        .replace(Regex("\\s+"), " ")
        .trim()

    // ── Internal matching logic ───────────────────────────────────────────────

    private data class MatchResult(
        val payload: PeerSubjectPayload?,
        val confidence: MatchConfidence,
        val score: Float
    )

    private fun findBestMatch(
        host: PeerSubjectPayload,
        candidates: List<PeerSubjectPayload>
    ): MatchResult {
        if (candidates.isEmpty()) return MatchResult(null, MatchConfidence.FUZZY, 0f)

        // 1. Exact code match
        if (!host.subjectCode.isNullOrBlank()) {
            val codeMatch = candidates.firstOrNull {
                !it.subjectCode.isNullOrBlank() &&
                        it.subjectCode.trim().equals(host.subjectCode.trim(), ignoreCase = true)
            }
            if (codeMatch != null) return MatchResult(codeMatch, MatchConfidence.AUTO_CODE, 1f)
        }

        // 2. Exact normalized name match
        val hostNorm = host.normalizedName.ifBlank { normalize(host.originalName) }
        val exactName = candidates.firstOrNull {
            val gNorm = it.normalizedName.ifBlank { normalize(it.originalName) }
            gNorm == hostNorm
        }
        if (exactName != null) return MatchResult(exactName, MatchConfidence.AUTO_NAME, 0.95f)

        // 3. Fuzzy — Jaccard token overlap
        val hostTokens = hostNorm.split(" ").filter { it.isNotBlank() }.toSet()
        var bestScore = 0f
        var bestCandidate: PeerSubjectPayload? = null

        for (guest in candidates) {
            val gNorm = guest.normalizedName.ifBlank { normalize(guest.originalName) }
            val guestTokens = gNorm.split(" ").filter { it.isNotBlank() }.toSet()
            val intersection = (hostTokens intersect guestTokens).size.toFloat()
            val union = (hostTokens union guestTokens).size.toFloat()
            val jaccard = if (union > 0) intersection / union else 0f

            // 4. Edit distance fallback if names are short
            val editScore = if (hostNorm.length <= 30 && gNorm.length <= 30) {
                val dist = levenshtein(hostNorm, gNorm)
                val maxLen = maxOf(hostNorm.length, gNorm.length).toFloat()
                if (maxLen > 0) 1f - (dist / maxLen) else 0f
            } else 0f

            val combined = maxOf(jaccard, editScore)
            if (combined > bestScore) {
                bestScore = combined
                bestCandidate = guest
            }
        }

        return if (bestScore >= 0.55f) {
            MatchResult(bestCandidate, MatchConfidence.FUZZY, bestScore)
        } else {
            MatchResult(null, MatchConfidence.FUZZY, 0f)
        }
    }

    // ── Levenshtein distance ──────────────────────────────────────────────────

    private fun levenshtein(a: String, b: String): Float {
        val m = a.length; val n = b.length
        val dp = Array(m + 1) { IntArray(n + 1) }
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        for (i in 1..m) for (j in 1..n) {
            dp[i][j] = if (a[i - 1] == b[j - 1]) dp[i - 1][j - 1]
            else 1 + min(dp[i - 1][j], min(dp[i][j - 1], dp[i - 1][j - 1]))
        }
        return dp[m][n].toFloat()
    }
}
