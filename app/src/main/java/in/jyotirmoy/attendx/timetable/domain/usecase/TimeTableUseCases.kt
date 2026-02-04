package `in`.jyotirmoy.attendx.timetable.domain.usecase

import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject

data class TimeTableUseCases(
    val getDailySchedule: GetDailyScheduleUseCase,
    val getNextClass: GetNextClassUseCase,
    val getCurrentClass: GetCurrentClassUseCase,
    val detectTimeClash: DetectTimeClashUseCase,
    val addClassSlot: AddClassSlotUseCase,
    val deleteClass: DeleteClassUseCase
)
