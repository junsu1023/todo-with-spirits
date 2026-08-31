package com.oow.todowithspirit.dto.task;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class TaskDeleteRequest {

    @NotEmpty(message = "taskIds must not be empty")
    private List<Long> taskIds;
}
