package com.taskflow.taskflow_api.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank @Size(min = 3, max = 50) String title,
        @NotNull Priority priority,
        @Size(max = 500) String description
) {
}
