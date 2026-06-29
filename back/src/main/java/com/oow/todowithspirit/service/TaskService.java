package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.task.NotificationType;
import com.oow.todowithspirit.domain.task.RepeatType;
import com.oow.todowithspirit.domain.task.Task;
import com.oow.todowithspirit.domain.task.TaskRepository;
import com.oow.todowithspirit.domain.task.TaskType;
import com.oow.todowithspirit.domain.user.User;
import com.oow.todowithspirit.domain.user.UserRepository;
import com.oow.todowithspirit.dto.task.RoutineCreateRequest;
import com.oow.todowithspirit.dto.task.ScheduleCreateRequest;
import com.oow.todowithspirit.dto.task.TaskCreateResponse;
import com.oow.todowithspirit.dto.task.TaskSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskService {

    private static final EnumSet<RepeatType> ROUTINE_ALLOWED_REPEAT =
            EnumSet.of(RepeatType.DAILY, RepeatType.WEEKLY, RepeatType.MONTHLY);

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskCreateResponse createSchedule(Long userId, ScheduleCreateRequest request) {
        validateRepeatDetails(request.getRepeatType(), request.getRepeatDaysOfWeek(), request.getRepeatDaysOfMonth());

        boolean isAllDay = Boolean.TRUE.equals(request.getIsAllDay());

        if (!isAllDay && request.getEndDatetime() == null) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "endDatetime",
                    "End datetime is required when not all day");
        }

        User user = userRepository.getReferenceById(userId);

        Task task = Task.createSchedule(
                user,
                request.getTitle(),
                request.getMemo(),
                request.getCategory(),
                request.getStartDatetime().toLocalDate(),
                request.getStartDatetime().toLocalTime(),
                request.getEndDatetime() != null ? request.getEndDatetime().toLocalDate() : null,
                request.getEndDatetime() != null ? request.getEndDatetime().toLocalTime() : null,
                isAllDay,
                Boolean.TRUE.equals(request.getIsImportant()),
                request.getRepeatType(),
                request.getRepeatEndDate(),
                request.getRepeatDaysOfWeek(),
                request.getRepeatDaysOfMonth(),
                resolveNotificationMinutes(request.getNotification()),
                Boolean.TRUE.equals(request.getIsPublic())
        );

        return TaskCreateResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskCreateResponse createRoutine(Long userId, RoutineCreateRequest request) {
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
                request.getRepeatType(),
                request.getRepeatEndDate(),
                request.getRepeatDaysOfWeek(),
                request.getRepeatDaysOfMonth(),
                resolveNotificationMinutes(request.getNotification()),
                Boolean.TRUE.equals(request.getIsPublic())
        );

        return TaskCreateResponse.from(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskSummaryResponse> getSchedules(Long userId, LocalDate from, LocalDate to) {
        List<Task> tasks = (from != null && to != null)
                ? taskRepository.findAllByUserIdAndTaskTypeAndDateRange(userId, TaskType.SCHEDULE, from, to)
                : taskRepository.findAllByUserIdAndTaskType(userId, TaskType.SCHEDULE);

        return tasks.stream().map(TaskSummaryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<TaskSummaryResponse> getRoutines(Long userId) {
        return taskRepository.findAllByUserIdAndTaskType(userId, TaskType.HABIT)
                .stream().map(TaskSummaryResponse::from).toList();
    }

    // 단건 조회: @Transactional 내에서 lazy 컬렉션(repeatDaysOfWeek/Month) 로딩
    @Transactional(readOnly = true)
    public TaskCreateResponse getScheduleDetail(Long userId, Long taskId) {
        Task task = taskRepository.findByIdAndUserIdAndType(taskId, userId, TaskType.SCHEDULE)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        return TaskCreateResponse.from(task);
    }

    @Transactional(readOnly = true)
    public TaskCreateResponse getRoutineDetail(Long userId, Long taskId) {
        Task task = taskRepository.findByIdAndUserIdAndType(taskId, userId, TaskType.HABIT)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        return TaskCreateResponse.from(task);
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
