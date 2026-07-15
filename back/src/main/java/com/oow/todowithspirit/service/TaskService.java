package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.task.*;
import com.oow.todowithspirit.domain.user.User;
import com.oow.todowithspirit.domain.user.UserRepository;
import com.oow.todowithspirit.dto.task.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TaskService {

    private static final EnumSet<RepeatType> ROUTINE_ALLOWED_REPEAT = EnumSet.of(RepeatType.DAILY, RepeatType.WEEKLY, RepeatType.MONTHLY);

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final RoutineCompletionRepository routineCompletionRepository;

    // =========================================================
    // 생성
    // =========================================================

    @Transactional
    public ScheduleCreateResponse createSchedule(Long userId, ScheduleCreateRequest request) {
        User user = userRepository.getReferenceById(userId);
        Task task = Task.createSchedule(
                user,
                request.getTitle(),
                request.getMemo(),
                request.getCategory(),
                request.getEndDateTime(),
                Boolean.TRUE.equals(request.getIsAllDay()),
                Boolean.TRUE.equals(request.getIsImportant()),
                resolveNotificationMinutes(request.getNotificationType()),
                Boolean.TRUE.equals(request.getIsPublic())
        );
        return ScheduleCreateResponse.from(taskRepository.save(task));
    }

    @Transactional
    public RoutineCreateResponse createRoutine(Long userId, RoutineCreateRequest request) {
        if (!ROUTINE_ALLOWED_REPEAT.contains(request.getRepeatType())) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "repeatType",
                    "Routine repeat type must be DAILY, WEEKLY, or MONTHLY");
        }
        validateRepeatDetails(request.getRepeatType(), request.getRepeatDaysOfWeek(), request.getRepeatDaysOfMonth());

        User user = userRepository.getReferenceById(userId);
        Task task = Task.createRoutine(
                user,
                request.getTitle(),
                request.getMemo(),
                request.getCategory(),
                request.getRepeatType(),
                request.getRepeatEndDate(),
                request.getRepeatDaysOfWeek(),
                request.getRepeatDaysOfMonth(),
                resolveNotificationMinutes(request.getNotificationType()),
                Boolean.TRUE.equals(request.getIsPublic())
        );
        return RoutineCreateResponse.from(taskRepository.save(task));
    }

    @Transactional
    public RoutineCreateResponse updateRoutine(Long userId, Long taskId, RoutineUpdateRequest request) {
        if (!ROUTINE_ALLOWED_REPEAT.contains(request.getRepeatType())) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "repeatType",
                    "Routine repeat type must be DAILY, WEEKLY, or MONTHLY");
        }
        validateRepeatDetails(request.getRepeatType(), request.getRepeatDaysOfWeek(), request.getRepeatDaysOfMonth());

        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

        if (task.getTaskType() != TaskType.ROUTINE) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "taskId",
                    "Task is not a routine");
        }

        task.updateRoutine(
                request.getTitle(),
                request.getMemo(),
                request.getStartDate(),
                request.getRepeatType(),
                request.getRepeatEndDate(),
                request.getRepeatDaysOfWeek(),
                request.getRepeatDaysOfMonth(),
                resolveNotificationMinutes(request.getNotification()),
                Boolean.TRUE.equals(request.getIsPublic())
        );

        return RoutineCreateResponse.from(task);
    }

    @Transactional
    public RoutineCreateResponse updateSchedule(Long userId, Long taskId, ScheduleUpdateRequest request) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

        if (task.getTaskType() != TaskType.SCHEDULE) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "taskId",
                    "Task is not a schedule");
        }

        task.updateSchedule(
                request.getTitle(),
                request.getMemo(),
                request.getCategory(),
                request.getEndDateTime(),
                Boolean.TRUE.equals(request.getIsAllDay()),
                Boolean.TRUE.equals(request.getIsImportant()),
                resolveNotificationMinutes(request.getNotification()),
                Boolean.TRUE.equals(request.getIsPublic())
        );

        return RoutineCreateResponse.from(task);
    }

    // =========================================================
    // 목록 조회
    // =========================================================

    @Transactional(readOnly = true)
    public TaskListResponse<TaskSummaryResponse> getSchedules(Long userId, LocalDate from, LocalDate to) {
        List<Task> tasks = (from != null && to != null)
                ? taskRepository.findAllByUserIdAndTaskTypeAndDateRange(userId, TaskType.SCHEDULE, from, to)
                : taskRepository.findAllByUserIdAndTaskType(userId, TaskType.SCHEDULE);
        return TaskListResponse.of(tasks.stream().map(TaskSummaryResponse::from).toList());
    }

    // 특정 날짜 기준 활성화 여부 및 완료 판단
    @Transactional(readOnly = true)
    public TaskListResponse<TaskSummaryResponse> getRoutines(Long userId, LocalDate targetDate) {
        // default: 오늘
        LocalDate date = (targetDate != null) ? targetDate : LocalDate.now();

        // 사용자의 전체 루틴 조회
        List<Task> allRoutines = taskRepository.findAllByUserIdAndTaskType(userId, TaskType.ROUTINE);

        // 1. 해당 일자에 실제로 '수행 조건'에 부합하는 루틴들만 필터링 (In-Memory Filter)
        List<Task> activeRoutines = allRoutines.stream()
                .filter(task -> isRoutineActiveOnDate(task, date))
                .toList();

        if (activeRoutines.isEmpty()) {
            return TaskListResponse.of(Collections.emptyList());
        }

        // 2. 해당 일자에 실제로 완료된 루틴들의 목록 매핑
        List<Long> routineIds = activeRoutines.stream().map(Task::getId).toList();
        Map<Long, Map<LocalDate, RoutineCompletion>> completionMap = loadCompletionMap(routineIds, date, date);

        // 3. 응답에 해당 일자 기준의 완료 여부(isCompleted)를 정확하게 담아 내려줌
        List<TaskSummaryResponse> items = activeRoutines.stream()
                .map(task -> {
                    boolean isCompleted = completionMap.getOrDefault(task.getId(), Map.of()).containsKey(date);
                    return TaskSummaryResponse.fromRoutineWithDateContext(task, isCompleted, date);
                })
                .toList();

        return TaskListResponse.of(items);
    }

    // =========================================================
    // 캘린더 통합 조회 (occurrence 기반 — from/to 필수)
    // =========================================================

    @Transactional(readOnly = true)
    public CalendarTaskListResponse getCalendarTasks(Long userId, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "dateRange", "From and to dates are required.");
        }

        // 성능 안전장치: 최대 90일(약 3개월) 까지만 캘린더 한 번에 조회 허용
        long daysBetween = ChronoUnit.DAYS.between(from, to);
        if (daysBetween > 90) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "dateRange", "Cannot query more than 90 days of calendar data.");
        }

        List<Task> tasks = taskRepository.findCalendarTasksWithDateRange(userId, from, to);

        List<Task> schedules = tasks.stream().filter(t -> t.getTaskType() == TaskType.SCHEDULE).toList();
        List<Task> routines = tasks.stream().filter(t -> t.getTaskType() == TaskType.ROUTINE).toList();

        // 1. 일정 Occurrence 변환
        List<CalendarOccurrenceResponse> scheduleOccurrences = schedules.stream()
                .map(CalendarOccurrenceResponse::fromSchedule)
                .toList();

        // 2. 루틴 Occurrence 변환 (완료 상태 맵핑 포함)
        List<Long> routineIds = routines.stream().map(Task::getId).toList();
        Map<Long, Map<LocalDate, RoutineCompletion>> completionMap = loadCompletionMap(routineIds, from, to);

        List<CalendarOccurrenceResponse> routineOccurrences = routines.stream()
                .flatMap(task -> expandOccurrences(task, from, to).stream()
                        .map(date -> CalendarOccurrenceResponse.fromRoutineOccurrence(
                                task, date,
                                completionMap.getOrDefault(task.getId(), Map.of()).get(date))))
                .toList();

        // 3. 최종 병합 및 정렬 (날짜 순 -> 같으면 시간 순 정렬)
        List<CalendarOccurrenceResponse> allItems = Stream
                .concat(scheduleOccurrences.stream(), routineOccurrences.stream())
                .sorted(Comparator.comparing(CalendarOccurrenceResponse::getOccurrenceDate)
                        .thenComparing(CalendarOccurrenceResponse::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        return CalendarTaskListResponse.of(allItems);
    }

    // =========================================================
    // 단건 조회
    // =========================================================

    @Transactional(readOnly = true)
    public RoutineCreateResponse getTaskDetail(Long userId, Long taskId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        return RoutineCreateResponse.from(task);
    }

    // =========================================================
    // 완료 / 완료 취소
    // =========================================================

    @Transactional
    public void completeTask(Long userId, Long taskId, LocalDate date) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

        if (task.getTaskType() == TaskType.ROUTINE) {
            LocalDate completionDate = date != null ? date : LocalDate.now();
            if (routineCompletionRepository.findByTaskIdAndCompletionDate(taskId, completionDate).isPresent()) {
                throw new ApiException(ErrorCode.ALREADY_COMPLETED,
                        "Routine already completed on " + completionDate);
            }
            routineCompletionRepository.save(RoutineCompletion.of(task, completionDate));
        } else {
            task.completeTask();
        }
    }

    @Transactional
    public void undoCompleteTask(Long userId, Long taskId, LocalDate date) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

        if (task.getTaskType() == TaskType.ROUTINE) {
            LocalDate completionDate = date != null ? date : LocalDate.now();
            RoutineCompletion completion = routineCompletionRepository
                    .findByTaskIdAndCompletionDate(taskId, completionDate)
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
            routineCompletionRepository.delete(completion);
        } else {
            task.undoCompleteTask();
        }
    }

    // =========================================================
    // 삭제
    // =========================================================

    @Transactional
    public TaskDeleteResponse deleteTasks(Long userId, TaskDeleteRequest request) {
        List<Task> tasks = taskRepository.findAllById(request.getTaskIds())
                .stream()
                .filter(t -> userId.equals(t.getUser().getId()))
                .toList();
        taskRepository.deleteAll(tasks);
        return TaskDeleteResponse.of(tasks.size());
    }

    // =========================================================
    // helpers
    // =========================================================

    private boolean isRoutineActiveOnDate(Task task, LocalDate date) {
        // 루틴 시작 전이거나, 종료일 이후라면 비활성화
        if (date.isBefore(task.getStartDate())) return false;
        if (task.getRepeatEndDate() != null && date.isAfter(task.getRepeatEndDate())) return false;

        return switch (task.getRepeatType()) {
            case DAILY -> true;
            case WEEKLY -> task.getRepeatDaysOfWeek().contains(date.getDayOfWeek());
            case MONTHLY -> task.getRepeatDaysOfMonth().contains(date.getDayOfMonth());
            default -> false;
        };
    }

    private Map<Long, Map<LocalDate, RoutineCompletion>> loadCompletionMap(
            List<Long> routineIds, LocalDate from, LocalDate to) {
        if (routineIds.isEmpty()) return Map.of();
        return routineCompletionRepository
                .findAllByTaskIdInAndCompletionDateBetween(routineIds, from, to)
                .stream()
                .collect(Collectors.groupingBy(
                        rc -> rc.getTask().getId(),
                        Collectors.toMap(RoutineCompletion::getCompletionDate, Function.identity())
                ));
    }

    private List<LocalDate> expandOccurrences(Task task, LocalDate from, LocalDate to) {
        LocalDate rangeStart = task.getStartDate().isAfter(from) ? task.getStartDate() : from;
        LocalDate rangeEnd = task.getRepeatEndDate() == null ? to
                : task.getRepeatEndDate().isBefore(to) ? task.getRepeatEndDate() : to;

        if (rangeStart.isAfter(rangeEnd)) return List.of();

        return switch (task.getRepeatType()) {
            case DAILY -> rangeStart.datesUntil(rangeEnd.plusDays(1)).toList();
            case WEEKLY -> {
                Set<DayOfWeek> days = task.getRepeatDaysOfWeek();
                yield rangeStart.datesUntil(rangeEnd.plusDays(1))
                        .filter(d -> days.contains(d.getDayOfWeek()))
                        .toList();
            }
            case MONTHLY -> {
                Set<Integer> dayNums = task.getRepeatDaysOfMonth();
                yield rangeStart.datesUntil(rangeEnd.plusDays(1))
                        .filter(d -> dayNums.contains(d.getDayOfMonth()))
                        .toList();
            }
            default -> List.of();
        };
    }

    private void validateRepeatDetails(RepeatType repeatType, Set<?> daysOfWeek, Set<Integer> daysOfMonth) {
        if (repeatType == RepeatType.WEEKLY && (daysOfWeek == null || daysOfWeek.isEmpty())) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "repeatDaysOfWeek",
                    "Days of week are required for weekly repeat");
        }
        if (repeatType == RepeatType.MONTHLY) {
            if (daysOfMonth == null || daysOfMonth.isEmpty()) {
                throw new ApiException(ErrorCode.INVALID_PARAMETER, "repeatDaysOfMonth",
                        "Days of month are required for monthly repeat");
            }
            if (daysOfMonth.stream().anyMatch(d -> d < 1 || d > 31)) {
                throw new ApiException(ErrorCode.INVALID_PARAMETER, "repeatDaysOfMonth",
                        "Day of month must be between 1 and 31");
            }
        }
    }

    private Integer resolveNotificationMinutes(NotificationType option) {
        if (option == null || option == NotificationType.NONE) return null;
        return option.getMinutes();
    }
}
