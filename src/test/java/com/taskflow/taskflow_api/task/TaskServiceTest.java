package com.taskflow.taskflow_api.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void findById_returnsTask_ifTaskExists() {
        // given
        Task task = new Task("new Task 1", Priority.HIGH,null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // when
        Task result = taskService.findById(1L);

        // then
        assertThat(result).isEqualTo(task);
    }

    @Test
    void findById_throws_whenTaskDoesNotExist() {
        // given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() ->  taskService.findById(1L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task 1 not found");
    }

    @Test
    void toggle_marksTaskAsDone_whenTaskIsNotDone(){
        // given
        Task task = new Task("new Task 1", Priority.HIGH,null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // when
        Task result = taskService.toggle(1L);

        //then
        assertThat(result.isDone()).isTrue();
        //dirty checking
        verify(taskRepository, never()).save(any());
    }

    @Test
    void toggle_marksTaskAsNotDone_whenTaskIsDone(){
        // given
        Task task = new Task("new Task 1", Priority.HIGH,null);
        task.toggle();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // when
        Task result = taskService.toggle(1L);

        // then
        assertThat(result.isDone()).isFalse();
        verify(taskRepository, never()).save(any());
    }

    @Test
    void findAll_returnsAllTasks_ifTasksExist() {
        // given
        Task task1 = new Task("new Task 1", Priority.HIGH,null);
        Task task2 = new Task("new Task 2", Priority.MEDIUM,null);
        Task task3 = new Task("new Task 3", Priority.LOW,null);
        when(taskRepository.findAll(Sort.by("id")))
                .thenReturn(List.of(task1,task2,task3));

        // when
        List<Task> result = taskService.findAll();

        // then
        assertThat(result).containsExactly(task1, task2, task3);
    }

    @Test
    void createTask_returnsTask_whenTaskIsCreated() {
        //given
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        // when
        Task result = taskService.create("new Task 1",Priority.HIGH,null);

        // then
        assertThat(result.getTitle()).isEqualTo("new Task 1");
        assertThat(result.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(result.getDescription()).isNull();
        assertThat(result.isDone()).isFalse();
    }

    @Test
    void deleteTask_whenTaskExists_TaskIsDeleted() {
        // given
        when(taskRepository.existsById(1L)).thenReturn(true);

        // when
        taskService.delete(1L);

        // then
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteTask_throws_whenTaskDoesNotExist() {
        // given
        when(taskRepository.existsById(1L)).thenReturn(false);

        // then
        assertThatThrownBy(() ->  taskService.delete(1L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task 1 not found");
        verify(taskRepository, never()).deleteById(1L);
    }
}
