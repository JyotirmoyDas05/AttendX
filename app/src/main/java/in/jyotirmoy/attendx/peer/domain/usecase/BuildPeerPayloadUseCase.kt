package `in`.jyotirmoy.attendx.peer.domain.usecase

import `in`.jyotirmoy.attendx.core.domain.model.AttendanceStatus
import `in`.jyotirmoy.attendx.core.domain.repository.AttendanceRepository
import `in`.jyotirmoy.attendx.core.domain.repository.SubjectRepository
import `in`.jyotirmoy.attendx.peer.data.model.PeerSubjectPayload
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildPeerPayloadUseCase @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val attendanceRepository: AttendanceRepository,
    private val matchSubjectsUseCase: MatchSubjectsUseCase
) {
    /**
     * Reads the user's subjects from Room, computes per-subject attendance %,
     * and returns a list of PeerSubjectPayload ready to write to Firestore.
     * Only subjects with at least one recorded attendance are included.
     */
    suspend operator fun invoke(): List<PeerSubjectPayload> {
        val subjects = subjectRepository.getAllSubjectsOnce()
        return subjects.mapNotNull { subject ->
            val present = attendanceRepository
                .getCountBySubjectAndStatus(subject.id, AttendanceStatus.PRESENT)
                .first()
            val absent = attendanceRepository
                .getCountBySubjectAndStatus(subject.id, AttendanceStatus.ABSENT)
                .first()
            val total = present + absent
            if (total == 0) return@mapNotNull null  // skip subjects with no data

            val percentage = (present.toFloat() / total.toFloat()) * 100f
            PeerSubjectPayload(
                subjectCode          = subject.subjectCode,
                normalizedName       = matchSubjectsUseCase.normalize(subject.subject),
                originalName         = subject.subject,
                attendancePercentage = percentage
            )
        }
    }
}
