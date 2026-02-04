package `in`.jyotirmoy.attendx.timetable.domain.usecase

import `in`.jyotirmoy.attendx.timetable.domain.repository.TimeTableRepository
import javax.inject.Inject

class DeleteClassUseCase @Inject constructor(
    private val repository: TimeTableRepository
) {
    suspend operator fun invoke(id: Int) {
        repository.deleteClass(id)
    }
    
    suspend operator fun invoke(ids: List<Int>) {
        ids.forEach { repository.deleteClass(it) }
    }
}
