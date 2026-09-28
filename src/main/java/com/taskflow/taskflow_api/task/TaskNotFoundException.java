package com.taskflow.taskflow_api.task;


public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(Long id) {
        super("Task "+id+" not found");
    }
}
