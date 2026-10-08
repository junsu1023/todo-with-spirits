package com.oow.todowithspirit.dto.spirit;

import com.oow.todowithspirit.domain.task.CategoryType;
import com.oow.todowithspirit.domain.task.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class SpiritTodoSummaryResponse {

    private final int totalCount;
    private final int completedCount;
    private final List<TodoEntry> todos;

    public static SpiritTodoSummaryResponse of(List<Task> tasks) {
        long completed = tasks.stream().filter(Task::isCompleted).count();
        List<TodoEntry> todos = tasks.stream()
                .map(task -> new TodoEntry(task.getId(), task.getTitle(), task.getCategory(), task.getStartDate(), task.isCompleted()))
                .toList();
        return new SpiritTodoSummaryResponse(tasks.size(), (int) completed, todos);
    }

    @Getter
    @AllArgsConstructor
    public static class TodoEntry {
        private final Long id;
        private final String title;
        private final CategoryType category;
        private final LocalDate date;
        private final boolean completed;
    }
}