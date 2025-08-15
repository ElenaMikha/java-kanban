package manager;

import tasks.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T makeManager();

    @BeforeEach
    void setUp() {
        manager = makeManager();
    }

    protected Task newTask(String name, LocalDateTime start, long minutes) {
        Task task = new Task(name, "", TaskStatus.NEW);
        task.setStartTime(start);
        task.setDuration(Duration.ofMinutes(minutes));
        return task;
    }

    protected int add(Task t) {
        return manager.createTask(t);
    }

    protected int addEpic(String name) {
        return manager.createEpic(new Epic(name, ""));
    }

    protected int addSub(int epicId, String name, TaskStatus status, LocalDateTime start, long minutes) {
        Subtask subtask = new Subtask(name, "", status, epicId);
        subtask.setStartTime(start);
        subtask.setDuration(Duration.ofMinutes(minutes));
        return manager.createSubtask(subtask);
    }

    @Test
    void createAndGetTaskStandard() {
        Task task = newTask("T1", LocalDateTime.of(2025, 1, 1, 10, 0), 30);
        int id = add(task);
        Task got = manager.getTasksById(id);
        assertNotNull(got);
        assertEquals("T1", got.getName());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), got.getStartTime());
        assertEquals(Duration.ofMinutes(30), got.getDuration());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 30), got.getEndTime());
    }

    @Test
    void updateTaskAppliesChanges_ifNoOverlap() {
        Task task = newTask("T1", LocalDateTime.of(2025, 1, 1, 10, 0), 30);
        int id = add(task);

        Task upd = new Task(id, "T1-upd", "", TaskStatus.IN_PROGRESS);
        upd.setStartTime(LocalDateTime.of(2025, 1, 1, 11, 0));
        upd.setDuration(Duration.ofMinutes(40));
        manager.updateTask(upd);

        Task got = manager.getTasksById(id);
        assertEquals("T1-upd", got.getName());
        assertEquals(TaskStatus.IN_PROGRESS, got.getTaskStatus());
        assertEquals(LocalDateTime.of(2025, 1, 1, 11, 0), got.getStartTime());
        assertEquals(Duration.ofMinutes(40), got.getDuration());
    }

    @Test
    void deleteTaskByIdRemovesFromStoreHistoryAndPriority() {
        int id = add(newTask("T", LocalDateTime.of(2025, 1, 1, 10, 0), 10));
        manager.getTasksById(id); // в историю
        manager.deleteTaskById(id);

        assertNull(manager.getTasksById(id));
        assertTrue(manager.getHistory().stream().noneMatch(x -> x.getId() == id));
        assertTrue(manager.getPrioritizedTasks().stream().noneMatch(x -> x.getId() == id));
    }

    @Test
    void prioritizedSortedSkipsNullStart() {
        int a = add(newTask("A", LocalDateTime.of(2025, 1, 1, 10, 0), 10));
        int b = add(newTask("B", LocalDateTime.of(2025, 1, 1, 8, 0), 10));
        int c = manager.createTask(new Task("C", "", TaskStatus.NEW));
        assertTrue(a > 0 && b > 0 && c > 0);

        List<Task> pr = manager.getPrioritizedTasks();
        assertEquals(2, pr.size());
        assertEquals("B", pr.get(0).getName());
        assertEquals("A", pr.get(1).getName());
    }

    @Test
    void createSubtaskRequiresExistingEpic_andLinksToIt() {
        int epicId = addEpic("E");
        int sid = addSub(epicId, "S", TaskStatus.NEW, LocalDateTime.of(2025, 1, 2, 9, 0), 20);
        assertTrue(sid > 0);

        Epic e = manager.getEpicById(epicId);
        assertNotNull(e);
        assertEquals(1, e.getSubtasks().size());
        assertEquals(sid, e.getSubtasks().getFirst().getId());
    }

    @Test
    void createSubtaskFailsIfEpicMissing() {
        int sid = addSub(999, "S", TaskStatus.NEW, LocalDateTime.of(2025, 1, 2, 9, 0), 20);
        assertEquals(-1, sid);
    }

    @Test
    void overlapsPreventedOnCreateAndUpdate() {
        int id1 = add(newTask("A", LocalDateTime.of(2025, 1, 1, 9, 0), 60));
        assertTrue(id1 > 0);

        int id2 = add(newTask("B", LocalDateTime.of(2025, 1, 1, 9, 30), 10));
        assertEquals(-1, id2);

        int id3 = add(newTask("C", LocalDateTime.of(2025, 1, 1, 10, 0), 10));
        assertTrue(id3 > 0);

        Task upd = new Task(id3, "C", "", TaskStatus.NEW);
        upd.setStartTime(LocalDateTime.of(2025, 1, 1, 9, 30));
        upd.setDuration(Duration.ofMinutes(5));
        manager.updateTask(upd);

        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), manager.getTasksById(id3).getStartTime());
    }

    @Test
    void epicStatusAllNew() {
        int e = addEpic("E");
        addSub(e, "S1", TaskStatus.NEW, LocalDateTime.of(2025, 1, 1, 9, 0), 10);
        addSub(e, "S2", TaskStatus.NEW, LocalDateTime.of(2025, 1, 1, 10, 0), 10);
        assertEquals(TaskStatus.NEW, manager.getEpicById(e).getTaskStatus());
    }

    @Test
    void epicStatusAllDone() {
        int e = addEpic("E");
        addSub(e, "S1", TaskStatus.DONE, LocalDateTime.of(2025, 1, 1, 9, 0), 10);
        addSub(e, "S2", TaskStatus.DONE, LocalDateTime.of(2025, 1, 1, 10, 0), 10);
        assertEquals(TaskStatus.DONE, manager.getEpicById(e).getTaskStatus());
    }

    @Test
    void epicStatusMixedNewAndDone() {
        int e = addEpic("E");
        addSub(e, "S1", TaskStatus.NEW, LocalDateTime.of(2025, 1, 1, 9, 0), 10);
        addSub(e, "S2", TaskStatus.DONE, LocalDateTime.of(2025, 1, 1, 10, 0), 10);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(e).getTaskStatus());
    }

    @Test
    void epicStatusHasInProgress() {
        int e = addEpic("E");
        addSub(e, "S1", TaskStatus.IN_PROGRESS, LocalDateTime.of(2025, 1, 1, 9, 0), 10);
        addSub(e, "S2", TaskStatus.NEW, LocalDateTime.of(2025, 1, 1, 10, 0), 10);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(e).getTaskStatus());
    }
}

