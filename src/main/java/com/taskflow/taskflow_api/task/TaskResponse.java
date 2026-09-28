package com.taskflow.taskflow_api.task;

public record TaskResponse(
        Long id,
        String title,
        Priority priority,
        String Description,
        boolean done
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getPriority(), task.getDescription(), task.isDone());
    }
}
