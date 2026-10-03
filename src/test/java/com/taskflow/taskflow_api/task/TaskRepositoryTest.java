package com.taskflow.taskflow_api.task;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    void flywayMigrations_createTableAndSeedData() {
        // when
        List<Task> tasks = repository.findAll();

        // then
        assertThat(tasks).hasSize(3);
        assertThat(tasks).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Set up Angular", "Build the Spring API", "Build CI/CD");
    }

    @Test
    void flywayMigrations_seedTasksAreNotDone() {
        // when
        List<Task> tasks = repository.findAll();

        // then
        assertThat(tasks).allMatch(task -> !task.isDone());
    }


    @Test
    void save_generatesId() {
        // given
        Task task = new Task("Learn DataJpaTest", Priority.LOW, null);

        // when
        Task saved = repository.save(task);

        // then
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void save_thenFindById_returnsSameData() {
        // given
        Task saved = repository.save(new Task("Read back", Priority.MEDIUM, "Some description"));

        // when
        Optional<Task> found = repository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Read back");
        assertThat(found.get().getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(found.get().getDescription()).isEqualTo("Some description");
        assertThat(found.get().isDone()).isFalse();
    }

    @Test
    void priority_isStoredAsText() {
        // given
        Task saved = repository.saveAndFlush(new Task("Check enum mapping", Priority.HIGH, null));

        // when
        String priorityInDb = jdbcTemplate.queryForObject(
                "SELECT priority FROM task WHERE id = ?", String.class, saved.getId());

        // then
        assertThat(priorityInDb).isEqualTo("HIGH");
    }

    @Test
    void toggle_isPersisted_withoutCallingSave() {
        // given
        Task saved = repository.saveAndFlush(new Task("Dirty checking", Priority.LOW, null));

        // when
        Task task = repository.findById(saved.getId()).orElseThrow();
        task.toggle();          // aucun save() !
        repository.flush();     // force Hibernate à envoyer les modifications

        // then
        Boolean doneInDb = jdbcTemplate.queryForObject(
                "SELECT done FROM task WHERE id = ?", Boolean.class, saved.getId());
        assertThat(doneInDb).isTrue();
    }

    @Test
    void deleteById_removesTask() {
        // given
        Task saved = repository.saveAndFlush(new Task("To delete", Priority.LOW, null));

        // when
        repository.deleteById(saved.getId());
        repository.flush();

        // then
        assertThat(repository.existsById(saved.getId())).isFalse();
    }


    @Test
    void save_fails_whenTitleIsLongerThan50Characters() {
        // given
        String tooLongTitle = "x".repeat(51);
        Task task = new Task(tooLongTitle, Priority.LOW, null);

        // when + then
        assertThatThrownBy(() -> repository.saveAndFlush(task))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_fails_whenPriorityIsNull() {
        // given
        Task task = new Task("No priority", null, null);

        // when + then
        assertThatThrownBy(() -> repository.saveAndFlush(task))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}