package com.taskflow.taskflow_api.task;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService  taskService;

    private Task taskWithId(Long id, String title, Priority priority, String description) {
        Task task = new Task(title, priority, description);
        ReflectionTestUtils.setField(task, "id", id);
        return task;
    }

    @Test
    void getTasks_returnsTasksAsJson() throws Exception {
        // given
        Task task = taskWithId(1L, "Learn tests", Priority.HIGH, "With MockMvc");
        when(taskService.findAll()).thenReturn(List.of(task));

        // when + then
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Learn tests"))
                .andExpect(jsonPath("$[0].priority").value("HIGH"))
                .andExpect(jsonPath("$[0].description").value("With MockMvc"))
                .andExpect(jsonPath("$[0].done").value(false));
    }

    @Test
    void getTask_returnsTask_whenItExists() throws Exception {
        // given
        Task task = taskWithId(2L, "Title 1", Priority.MEDIUM, null);
        when(taskService.findById(2L)).thenReturn(task);

        // when + then
        mockMvc.perform(get("/api/tasks/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("Title 1"));
    }

    @Test
    void getTask_returns404_whenTaskDoesNotExist() throws Exception {
        // given
        when(taskService.findById(99L)).thenThrow(new TaskNotFoundException(99L));

        // when + then
        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Task not found"))
                .andExpect(jsonPath("$.detail").value("Task 99 not found"));
    }

    @Test
    void createTask_returns201WithLocation_whenRequestIsValid() throws Exception {
        // given
        Task created = taskWithId(4L, "Title 2", Priority.HIGH, "Migrations");
        when(taskService.create("Title 2", Priority.HIGH, "Migrations")).thenReturn(created);

        String body = """
                { "title": "Title 2", "priority": "HIGH", "description": "Migrations" }
                """;

        // when + then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tasks/4"))
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.done").value(false));
    }


    @Test
    void createTask_returns400_whenRequestIsInvalid() throws Exception {
        // given
        String body = """
                { "title": "ab", "priority": null }
                """;

        // when + then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.priority").exists());

        verify(taskService, never()).create(any(), any(), any());
    }

    @Test
    void toggleTask_returnsUpdatedTask() throws Exception {
        // given
        Task task = taskWithId(1L, "Set up Angular", Priority.HIGH, null);
        task.toggle();
        when(taskService.toggle(1L)).thenReturn(task);

        // when + then
        mockMvc.perform(patch("/api/tasks/1/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.done").value(true));
    }

    @Test
    void deleteTask_returns204_whenTaskExists() throws Exception {
        // when + then
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService).delete(1L);
    }

    @Test
    void deleteTask_returns404_whenTaskDoesNotExist() throws Exception {
        // given
        doThrow(new TaskNotFoundException(99L)).when(taskService).delete(99L);

        // when + then
        mockMvc.perform(delete("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Task 99 not found"));
    }
}
