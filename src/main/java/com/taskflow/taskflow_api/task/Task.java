package com.taskflow.taskflow_api.task;

import jakarta.persistence.*;

@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Column(length = 500)
    private String description;

    private boolean done;

    protected Task() {
    }

    public Task(String title, Priority priority, String description) {
        this.title = title;
        this.priority = priority;
        this.description = description;
        this.done = false;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDone() {
        return done;
    }

    public void toggle() {
        this.done = !this.done;
    }
}
