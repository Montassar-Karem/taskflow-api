package com.taskflow.taskflow_api.task;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly=true)
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> findAll() {
        return taskRepository.findAll(Sort.by("id"));
    }

    public Task findById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Transactional
    public Task create(String title, Priority priority, String description) {
        return taskRepository.save(new Task(title, priority, description));
    }

    @Transactional
    public Task toggle(Long id) {
        Task task = findById(id);
        task.toggle();
        return task;
    }

    @Transactional
    public void delete(Long id) {
        if  (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

}



