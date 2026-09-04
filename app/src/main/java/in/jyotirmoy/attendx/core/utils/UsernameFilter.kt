package `in`.jyotirmoy.attendx.core.utils

/**
 * Client-side sanity check for author usernames.
 *
 * Deliberate limitation: the charset and length rules below are mirrored in
 * firestore.rules and therefore hold against any client, but the wordlist runs
 * only here and a modified client can skip it. Unbypassable profanity filtering
 * needs a backend; until then the wordlist stops casual misuse and manual review
 * in the Firestore console is the moderation story.
 */
object UsernameFilter {

    private val PATTERN = Regex("^[A-Za-z0-9._-]{3,24}$")

    // Common substitutions, applied before matching so "f4ggot" and "sh1t" are
    // caught alongside their plain spellings.
    private val LEET = mapOf(
        '0' to 'o', '1' to 'i', '3' to 'e', '4' to 'a',
        '5' to 's', '7' to 't', '8' to 'b', '@' to 'a', '$' to 's'
    )

    /**
     * Matched only against the whole name or a separator-delimited token. These
     * are too short to match as substrings without blocking ordinary names —
     * "ass" would otherwise reject "class", and "das".
     */
    private val BLOCKED_TOKENS = setOf(
        "anal", "anus", "arse", "ass", "bitch", "boob", "clit", "cock", "cum",
        "cunt", "dick", "dildo", "fag", "fuck", "horny", "jizz", "milf", "nigga",
        "penis", "porn", "pussy", "rape", "semen", "sex", "shit", "slut", "sperm",
        "tits", "twat", "wank", "whore", "kkk", "nazi", "pedo", "isis",
        // impersonation of the app or its operators
        "admin", "administrator", "attendx", "mod", "moderator", "official",
        "root", "staff", "support", "system", "owner", "helpdesk", "team"
    )

    /** Long enough to be unambiguous, so a substring match is safe. */
    private val BLOCKED_SUBSTRINGS = setOf(
        "asshole", "bastard", "blowjob", "faggot", "handjob", "molest", "nigger",
        "pedophile", "rapist", "retard", "vagina", "hitler", "terrorist",
        "suicide", "motherfucker"
    )

    /** Lowercase, undo leetspeak, collapse runs of 3+ so "fuuuuck" reduces. */
    internal fun normalize(text: String): String {
        val mapped = text.lowercase()
            .map { LEET[it] ?: it }
            .filter { it.isLetterOrDigit() }
            .joinToString("")

        // Runs of 3+ collapse to a single character, doubles are left alone:
        // "fuuuuck" must reduce to "fuck", while "ass" must NOT reduce to "as"
        // (which would then match every name containing "as").
        return buildString {
            var i = 0
            while (i < mapped.length) {
                val c = mapped[i]
                var run = 0
                while (i + run < mapped.length && mapped[i + run] == c) run++
                repeat(if (run >= 3) 1 else run) { append(c) }
                i += run
            }
        }
    }

    sealed interface Result {
        data object Valid : Result
        data class Invalid(val reason: String) : Result
    }

    fun validate(name: String): Result {
        val trimmed = name.trim()
        if (!PATTERN.matches(trimmed)) {
            return Result.Invalid(
                "Use 3-24 characters: letters, numbers, dot, underscore or hyphen."
            )
        }

        val whole = normalize(trimmed)
        val tokens = trimmed.split('.', '_', '-')
            .map(::normalize)
            .filter { it.isNotEmpty() }

        val blocked = whole in BLOCKED_TOKENS ||
            tokens.any { it in BLOCKED_TOKENS } ||
            BLOCKED_SUBSTRINGS.any { whole.contains(it) }

        if (blocked) {
            return Result.Invalid("That name isn't available. Please choose another.")
        }
        return Result.Valid
    }
}
