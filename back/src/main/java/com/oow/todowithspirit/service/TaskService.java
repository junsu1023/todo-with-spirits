package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.spirit.Spirit;
import com.oow.todowithspirit.domain.task.*;
import com.oow.todowithspirit.domain.user.UserRepository;
import com.oow.todowithspirit.dto.task.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TaskService {

    private static final EnumSet<RepeatType> ALLOWED_REPEAT =
            EnumSet.of(RepeatType.NONE, RepeatType.DAILY, RepeatType.WEEKLY, RepeatType.MONTHLY);
    private static final int AFFECTION_PER_COMPLETION = 1; // 할 일 완료 1회당 대표 정령 호감도 +1%

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final HolidayRepository holidayRepository;
    private final RoutineCompletionRepository routineCompletionRepository;
    private final TaskPostponementRepository taskPostponementRepository;
    private final SpiritService spiritService;
    private final SpiritActivityService spiritActivityService;
    private final CurrencyService currencyService;

    // =========================================================
    // 생성
    // =========================================================

    @Transactional
    public TaskResponse createTask(Long userId, TaskRequest request) {
        RepeatType repeatType = resolveRepeatType(request);
        LocalDate date = request.getEndDateTime() != null ? request.getEndDateTime().toLocalDate() : LocalDate.now();
        LocalTime time = request.getEndDateTime() != null ? request.getEndDateTime().toLocalTime() : null;
        boolean isAllDay = request.getIsAllDay() != null ? request.getIsAllDay() : time == null;
        validateRepeat(repeatType, date, request);

        Task task = Task.create(
                userRepository.getReferenceById(userId),
                request.getTitle(),
                date,
                time,
                isAllDay,
                Boolean.TRUE.equals(request.getIsImportant()),
                resolveNotificationMinutes(request.getNotificationType()),
                repeatType,
                request.getRepeatEndDate(),
                request.getRepeatDaysOfWeek(),
                request.getRepeatDaysOfMonth(),
                Boolean.TRUE.equals(request.getExcludeHoliday())
        );
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(Long userId, Long taskId, TaskRequest request) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

        RepeatType repeatType = resolveRepeatType(request);
        // endDateTime이 없으면 기존 날짜·시각 유지
        LocalDate date = request.getEndDateTime() != null ? request.getEndDateTime().toLocalDate() : task.getStartDate();
        LocalTime time = request.getEndDateTime() != null ? request.getEndDateTime().toLocalTime()
                : (task.isAllDay() ? null : task.getEndTime());
        boolean isAllDay = request.getIsAllDay() != null ? request.getIsAllDay() : time == null;
        validateRepeat(repeatType, date, request);

        task.update(
                request.getTitle(),
                date,
                time,
                isAllDay,
                Boolean.TRUE.equals(request.getIsImportant()),
                resolveNotificationMinutes(request.getNotificationType()),
                repeatType,
                request.getRepeatEndDate(),
                request.getRepeatDaysOfWeek(),
                request.getRepeatDaysOfMonth(),
                Boolean.TRUE.equals(request.getExcludeHoliday())
        );
        return TaskResponse.from(task);
    }

    // =========================================================
    // 미루기
    // =========================================================

    @Transactional
    public TaskPostponeResponse postponeTask(Long userId, Long taskId, TaskPostponeRequest request) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Not found task."));

        LocalDate originalDate;
        LocalDate currentDate;
        LocalTime currentTime;

        if (task.isRepeating()) {
            if (request.getOriginalDate() == null) {
                throw new ApiException(ErrorCode.INVALID_PARAMETER, "originalDate", "originalDate is required for repeating tasks");
            }
            originalDate = request.getOriginalDate();
            if (!isOccurrenceDate(task, originalDate)) {
                throw new ApiException(ErrorCode.INVALID_PARAMETER, "originalDate", "Not an occurrence date for this task");
            }

            TaskPostponement latest = taskPostponementRepository
                    .findTopByTaskIdAndOriginalDateOrderByCreatedAtDesc(taskId, originalDate)
                    .orElse(null);
            currentDate = latest != null ? latest.getPostponedDate() : originalDate;
            currentTime = latest != null ? latest.getPostponedTime() : task.getEndTime();
        } else {
            // 단발성: 처음 미룰 때는 지금 예정일을 원본으로 삼고, 이후로는 최초 로그의 원본을 계속 재사용
            originalDate = taskPostponementRepository.findTopByTaskIdOrderByCreatedAtAsc(taskId)
                    .map(TaskPostponement::getOriginalDate)
                    .orElse(task.getStartDate());
            currentDate = task.getStartDate();
            currentTime = task.getEndTime();
        }

        Integer maxCount = TaskPostponePolicy.maxPostponeCount(task);
        int currentCount = (int) taskPostponementRepository.countByTaskIdAndOriginalDate(taskId, originalDate);

        if (maxCount != null && currentCount >= maxCount) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "postponeCount",
                    "Postpone limit reached for this occurrence (max " + maxCount + ")");
        }

        LocalDate newDate = request.getNewDate() != null ? request.getNewDate() : currentDate;
        LocalTime newTime = request.getNewTime() != null ? request.getNewTime() : currentTime;

        if (task.isRepeating()) {
            validatePostponeRange(task, originalDate, newDate);
            validateNoCollision(task, originalDate, newDate);
        }

        taskPostponementRepository.save(TaskPostponement.create(task, originalDate, newDate, newTime));

        if (!task.isRepeating()) {
            task.rescheduleTo(newDate, request.getNewTime());
        }

        int newCount = currentCount + 1;
        Integer remaining = maxCount != null ? maxCount - newCount : null;

        return TaskPostponeResponse.builder()
                .taskId(taskId)
                .originalDate(originalDate)
                .postponedDate(newDate)
                .postponedTime(newTime)
                .postponeCount(newCount)
                .maxPostponeCount(maxCount)
                .remainingPostponeCount(remaining)
                .build();
    }

    /**
     * repeatType별 미루기 가능 날짜 범위 검증
     */
    private void validatePostponeRange(Task task, LocalDate originalDate, LocalDate newDate) {
        switch (task.getRepeatType()) {
            case DAILY -> {
                if (!newDate.equals(originalDate)) {
                    throw new ApiException(ErrorCode.INVALID_PARAMETER, "newDate",
                            "Daily tasks cannot change date, only time");
                }
            }
            case WEEKLY -> {
                LocalDate weekEnd = originalDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
                if (newDate.isBefore(originalDate) || newDate.isAfter(weekEnd)) {
                    throw new ApiException(ErrorCode.INVALID_PARAMETER, "newDate",
                            "Weekly tasks can only be postponed within the current week");
                }
            }
            case MONTHLY -> {
                LocalDate monthEnd = originalDate.with(TemporalAdjusters.lastDayOfMonth());
                if (newDate.isBefore(originalDate) || newDate.isAfter(monthEnd)) {
                    throw new ApiException(ErrorCode.INVALID_PARAMETER, "newDate",
                            "Monthly tasks can only be postponed within the current month");
                }
            }
            default ->
                    throw new ApiException(ErrorCode.INVALID_PARAMETER, "repeatType", "Unsupported repeat type for postpone");
        }
    }

    /**
     * 같은 반복 할 일의 다른 occurrence와 날짜가 겹치는지 검증
     */
    private void validateNoCollision(Task task, LocalDate originalDate, LocalDate newDate) {
        if (newDate.equals(originalDate)) return;

        if (isOccurrenceDate(task, newDate)) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "newDate",
                    "Target date already has an occurrence of this task");
        }

        Map<LocalDate, TaskPostponement> latestByOriginalDate = taskPostponementRepository
                .findAllByTaskId(task.getId()).stream()
                .collect(Collectors.toMap(TaskPostponement::getOriginalDate, Function.identity(),
                        (a, b) -> a.getCreatedAt().isAfter(b.getCreatedAt()) ? a : b));

        boolean collides = latestByOriginalDate.entrySet().stream()
                .anyMatch(e -> !e.getKey().equals(originalDate) && e.getValue().getPostponedDate().equals(newDate));

        if (collides) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "newDate",
                    "Target date is already occupied by another occurrence of this task");
        }
    }

    // =========================================================
    // 조회 (occurrence 기반 — from/to 필수, 최대 90일)
    // =========================================================

    @Transactional(readOnly = true)
    public CalendarTasksResponse getCalendarTasks(Long userId, LocalDate from, LocalDate to) {
        return CalendarTasksResponse.of(loadOccurrences(userId, from, to));
    }

    private List<TaskOccurrenceResponse> loadOccurrences(Long userId, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "dateRange", "From and to dates are required.");
        }
        if (from.isAfter(to)) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "dateRange", "From must not be after to.");
        }
        if (ChronoUnit.DAYS.between(from, to) > 90) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "dateRange", "Cannot query more than 90 days of task data.");
        }

        List<Task> tasks = taskRepository.findAllInDateRange(userId, from, to);

        List<Long> repeatingIds = tasks.stream().filter(Task::isRepeating).map(Task::getId).toList();
        Map<Long, Map<LocalDate, RoutineCompletion>> completionMap = loadCompletionMap(repeatingIds, from, to);

        return tasks.stream()
                .flatMap(task -> {
                    if (!task.isRepeating()) {
                        return Stream.of(new TaskOccurrenceResponse(
                                task, task.getStartDate(), task.isCompleted(), task.getCompletedAt()));
                    }
                    Map<LocalDate, RoutineCompletion> completions = completionMap.getOrDefault(task.getId(), Map.of());
                    return expandOccurrences(task, from, to).stream()
                            .map(date -> toOccurrence(task, date, completions.get(date)));
                })
                .sorted(Comparator.comparing(TaskOccurrenceResponse::getOccurrenceDate)
                        .thenComparing(TaskOccurrenceResponse::getEndTime, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(TaskOccurrenceResponse::getTaskId))
                .toList();
    }

    private TaskOccurrenceResponse toOccurrence(Task task, LocalDate date, RoutineCompletion completion) {
        return new TaskOccurrenceResponse(task, date,
                completion != null, completion != null ? completion.getCompletedAt() : null);
    }

    @Transactional(readOnly = true)
    public TaskOccurrenceResponse getTaskDetail(Long userId, Long taskId, LocalDate targetDate) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Not found task."));

        if (!task.isRepeating()) {
            return new TaskOccurrenceResponse(task, task.getStartDate(), task.isCompleted(), task.getCompletedAt());
        }

        // 반복: 해당 날짜가 수행일이 아니면 null
        if (!isOccurrenceDate(task, targetDate)) {
            return null;
        }

        RoutineCompletion completion = routineCompletionRepository
                .findByTaskIdAndCompletionDate(taskId, targetDate)
                .orElse(null);
        return toOccurrence(task, targetDate, completion);
    }

    /**
     * 해당 날짜가 반복 할 일의 수행일인지 판단
     */
    private boolean isOccurrenceDate(Task task, LocalDate targetDate) {
        // 기간 체크
        if (targetDate.isBefore(task.getStartDate())) return false;
        if (task.getRepeatEndDate() != null && targetDate.isAfter(task.getRepeatEndDate())) return false;

        // 반복 주기 및 요일/일자 조건 검증
        boolean isBaseMatch = switch (task.getRepeatType()) {
            case DAILY -> true;
            case WEEKLY ->
                    task.getRepeatDaysOfWeek() != null && task.getRepeatDaysOfWeek().contains(targetDate.getDayOfWeek());
            case MONTHLY ->
                    task.getRepeatDaysOfMonth() != null && task.getRepeatDaysOfMonth().contains(targetDate.getDayOfMonth());
            default -> false;
        };

        if (!isBaseMatch) return false;

        // 공휴일 제외 옵션이 켜져 있을 때 단건 DB 조회
        if (task.isExcludeHoliday()) {
            return !holidayRepository.existsByHolidayDate(targetDate);
        }

        return true;
    }

    // =========================================================
    // 완료 / 완료 취소
    // =========================================================

    @Transactional
    public TaskCompleteResponse completeTask(Long userId, Long taskId, LocalDate completionDate) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Not found task."));

        validateTaskDate(task, completionDate);

        // 완료 처리 (반복은 날짜별 완료 기록, 단발성은 task 자체 상태)
        if (task.isRepeating()) {
            if (routineCompletionRepository.findByTaskIdAndCompletionDate(taskId, completionDate).isPresent()) {
                throw new ApiException(ErrorCode.ALREADY_COMPLETED, "Task already completed on " + completionDate);
            }
            routineCompletionRepository.save(RoutineCompletion.of(task, completionDate));
        } else {
            if (task.isCompleted()) {
                throw new ApiException(ErrorCode.ALREADY_COMPLETED, "Task already completed");
            }
            task.completeTask();
        }

        // 대표 정령 경험치 증가 (없으면 자동 생성)
        Spirit spirit = spiritService.getOrCreateRepresentativeSpirit(userId);
        spirit.addExp(task.getGrowthValue(), task.getGrowthType());
        spiritActivityService.recordActivity(spirit, completionDate);

        // 재화 지급 (taskId+completionDate 단위로 멱등. 중복 요청이면 0 반환)
        long earned = currencyService.creditForTaskComplete(userId, task.getGrowthValue(), taskId, completionDate);
        long balance = currencyService.getBalance(userId);

        // 대표 정령 호감도 증가 (중복 완료 요청으로 재화가 지급되지 않은 경우는 제외). 완료 취소로 줄지 않음
        if (earned > 0) {
            spirit.increaseAffection(AFFECTION_PER_COMPLETION);
        }

        return new TaskCompleteResponse(earned, balance);
    }

    @Transactional
    public void undoCompleteTask(Long userId, Long taskId, LocalDate completionDate) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

        // 완료 취소 + 지급됐던 재화 회수
        if (task.isRepeating()) {
            RoutineCompletion completion = routineCompletionRepository
                    .findByTaskIdAndCompletionDate(taskId, completionDate)
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Task is not completed on " + completionDate));
            routineCompletionRepository.delete(completion);
            currencyService.reverseTaskComplete(userId, taskId, completionDate);
        } else {
            if (!task.isCompleted()) {
                throw new ApiException(ErrorCode.NOT_FOUND, "Task is not completed");
            }
            task.undoCompleteTask();
            // 단발성은 완료가 한 번뿐이므로 요청 date와 상관없이 회수 (미루기로 날짜가 바뀐 경우 포함)
            currencyService.reverseAllTaskComplete(userId, taskId);
        }

        // 대표 정령 경험치 차감 (없으면 자동 생성 후 차감)
        Spirit spirit = spiritService.getOrCreateRepresentativeSpirit(userId);
        spirit.decreaseExp(task.getGrowthValue(), task.getGrowthType());
    }

    /**
     * 완료 대상 날짜 정합성 검증
     */
    private void validateTaskDate(Task task, LocalDate targetDate) {
        if (task.isRepeating()) {
            if (!isOccurrenceDate(task, targetDate)) {
                throw new ApiException(ErrorCode.INVALID_PARAMETER,
                        String.format("Invalid date. %s is not an occurrence date for this task.", targetDate));
            }
            return;
        }
        if (!targetDate.equals(task.getStartDate())) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER,
                    String.format("Invalid date. Target date (%s) does not match task date (%s)", targetDate, task.getStartDate()));
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

    public Map<Long, Map<LocalDate, RoutineCompletion>> loadCompletionMap(
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

    public List<LocalDate> expandOccurrences(Task task, LocalDate from, LocalDate to) {
        LocalDate rangeStart = task.getStartDate().isAfter(from) ? task.getStartDate() : from;
        LocalDate rangeEnd = task.getRepeatEndDate() == null ? to
                : task.getRepeatEndDate().isBefore(to) ? task.getRepeatEndDate() : to;

        if (rangeStart.isAfter(rangeEnd)) return List.of();

        // 1. 기본 반복 규칙에 따라 발생 가능한 일자 생성
        List<LocalDate> baseDates = switch (task.getRepeatType()) {
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

        // 2. [추가] "공휴일 제외" 옵션이 켜져 있는 경우 필터링
        if (task.isExcludeHoliday() && !baseDates.isEmpty()) {
            // 루프 돌면서 매번 쿼리를 날리지 않고, 전개된 기간(rangeStart ~ rangeEnd) 내의 공휴일을 한 번에 가져옴
            Set<LocalDate> holidaysInRange = holidayRepository.findAllByHolidayDateBetween(rangeStart, rangeEnd)
                    .stream()
                    .map(Holiday::getHolidayDate)
                    .collect(Collectors.toSet());

            return baseDates.stream()
                    .filter(date -> !holidaysInRange.contains(date)) // 고속 해시셋 매칭 (O(1))
                    .toList();
        }

        return baseDates;
    }

    private RepeatType resolveRepeatType(TaskRequest request) {
        RepeatType repeatType = request.getRepeatType() != null ? request.getRepeatType() : RepeatType.NONE;
        if (!ALLOWED_REPEAT.contains(repeatType)) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "repeatType", "Repeat type must be NONE, DAILY, WEEKLY, or MONTHLY");
        }
        return repeatType;
    }

    private void validateRepeat(RepeatType repeatType, LocalDate startDate, TaskRequest request) {
        if (repeatType == RepeatType.NONE) return;

        if (request.getRepeatEndDate() != null && request.getRepeatEndDate().isBefore(startDate)) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "repeatEndDate",
                    "Repeat end date must not be before the start date");
        }
        Set<DayOfWeek> daysOfWeek = request.getRepeatDaysOfWeek();
        if (repeatType == RepeatType.WEEKLY && (daysOfWeek == null || daysOfWeek.isEmpty())) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "repeatDaysOfWeek",
                    "Days of week are required for weekly repeat");
        }
        if (repeatType == RepeatType.MONTHLY) {
            Set<Integer> daysOfMonth = request.getRepeatDaysOfMonth();
            if (daysOfMonth == null || daysOfMonth.isEmpty()) {
                throw new ApiException(ErrorCode.INVALID_PARAMETER, "repeatDaysOfMonth",
                        "Days of month are required for monthly repeat");
            }
            if (daysOfMonth.stream().anyMatch(d -> d == null || d < 1 || d > 31)) {
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
