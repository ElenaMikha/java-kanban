package manager;

import tasks.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    TaskManager taskManager = Managers.getDefault();
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


    @Test
    void testAddAndGetTaskById() {
        Task task = new Task("Task 1", "Simple task", TaskStatus.NEW);
        int taskId = taskManager.createTask(task);
        assertTrue(taskId > 0);

        Task foundTask = taskManager.getTasksById(taskId);
        assertNotNull(foundTask);
        assertEquals(taskId, foundTask.getId());
        assertEquals("Task 1", foundTask.getName());
    }

    @Test
    void testCreateEpic() {
        taskManager.createEpic(new Epic("Epic 1", "One subtask"));
        List<Epic> epics = taskManager.getEpics();
        assertEquals(1, epics.size());

    }

    @Test
    void testAddAndGetEpicById() {
        Epic epic = new Epic("Epic 1", "Epic description");
        int epicId = taskManager.createEpic(epic);
        assertTrue(epicId > 0);

        Epic foundEpic = taskManager.getEpicById(epicId);
        assertNotNull(foundEpic);
        assertEquals(epicId, foundEpic.getId());
        assertEquals("Epic 1", foundEpic.getName());
    }

    @Test
    void testCreateSubtask() {
        Epic epic = new Epic("Epic", "One subtask");
        taskManager.createEpic(epic);
        int epicId = epic.getId();
        taskManager.createSubtask(new Subtask("Subtask", "Description sub", TaskStatus.NEW, epicId));
        List<Subtask> subtasks = taskManager.getSubtasks();
        assertEquals(1, subtasks.size());
    }

    @Test
    void testAddAndGetSubtaskById() {
        Epic epic = new Epic("Epic for subtask", "Epic description");
        int epicId = taskManager.createEpic(epic);
        assertTrue(epicId > 0);

        Subtask subtask = new Subtask("Subtask 1", "Subtask description", TaskStatus.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask);
        assertTrue(subtaskId > 0);

        Subtask foundSubtask = taskManager.getSubtaskById(subtaskId);
        assertNotNull(foundSubtask);
        assertEquals(subtaskId, foundSubtask.getId());
        assertEquals(epicId, foundSubtask.getEpicId());
        assertEquals("Subtask 1", foundSubtask.getName());
    }

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Other description", TaskStatus.DONE);
        task1.setId(1);
        task2.setId(1);
        assertEquals(task1, task2);
    }

    @Test
    void testSubtasksAreEqualIfIdsAreEqual() {
        Subtask sub1 = new Subtask("Sub1", "Desc1", TaskStatus.NEW, 5);
        Subtask sub2 = new Subtask("Sub2", "Desc2", TaskStatus.DONE, 5);
        sub1.setId(2);
        sub2.setId(2);
        assertEquals(sub1, sub2);
    }

    @Test
    void testEpicsAreEqualIfIdsAreEqual() {
        Epic epic1 = new Epic("Epic1", "Desc1");
        Epic epic2 = new Epic("Epic2", "Desc2");
        epic1.setId(3);
        epic2.setId(3);
        assertEquals(epic1, epic2);
    }

    @Test
    void testEpicCannotContainItselfAsSubtask() {
        Epic epic = new Epic("Epic", "Epic that should not contain itself");
        int epicId = taskManager.createEpic(epic);
        Subtask invalidSubtask = new Subtask("Invalid", "Should not be added", TaskStatus.NEW, epicId);
        invalidSubtask.setId(epicId);
        int resultId = taskManager.createSubtask(invalidSubtask);
        assertEquals(-1, resultId);
        Epic updatedEpic = taskManager.getEpicById(epicId);
        boolean hasSelfAsSubtask = false;
        for (Subtask s : updatedEpic.getSubtasks()) {
            if (s.getId().equals(epicId)) {
                hasSelfAsSubtask = true;
                break;
            }
        }
        assertFalse(hasSelfAsSubtask);
    }

    @Test
    void testSubtaskCannotContainItselfAsEpic() {
        Epic epic = new Epic("Epic", "Epic for testing");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Testing epic assignment", TaskStatus.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask);
        Subtask invalidSubtask = new Subtask("Invalid", "Should not assign itself as epic",
                TaskStatus.NEW, subtaskId);
        invalidSubtask.setId(subtaskId);
        int resultId = taskManager.createSubtask(invalidSubtask);
        assertEquals(-1, resultId);

        Epic epicWithSubtaskId = taskManager.getEpicById(subtaskId);
        if (epicWithSubtaskId != null) {
            boolean hasSelfAsSubtask = false;
            for (Subtask s : epicWithSubtaskId.getSubtasks()) {
                if (s.getId().equals(subtaskId)) {
                    hasSelfAsSubtask = true;
                    break;
                }
            }
            assertFalse(hasSelfAsSubtask);
        }
    }

    @Test
    void testTasksWithAssignedIdAndGeneratedIdShouldNotConflict() {
        Task taskWithId = new Task("TaskWithId", "Has preset id", TaskStatus.NEW);
        taskWithId.setId(100);
        int id1 = taskManager.createTask(taskWithId);
        Task taskGeneratedId = new Task("TaskGeneratedId", "Will get generated id", TaskStatus.NEW);
        int id2 = taskManager.createTask(taskGeneratedId);
        assertNotEquals(id1, id2);
        assertNotNull(taskManager.getTasksById(id1));
        assertNotNull(taskManager.getTasksById(id2));
    }

    @Test
    void testTaskShouldRemainUnchangedAfterAdding() {
        Task originalTask = new Task("OriginalTask", "Original description", TaskStatus.NEW);
        String expectedName = originalTask.getName();
        String expectedDescription = originalTask.getDescription();
        TaskStatus expectedStatus = originalTask.getTaskStatus();
        int assignedId = taskManager.createTask(originalTask);
        Task taskFromManager = taskManager.getTasksById(assignedId);

        assertEquals(expectedName, taskFromManager.getName());
        assertEquals(expectedDescription, taskFromManager.getDescription());
        assertEquals(expectedStatus, taskFromManager.getTaskStatus());
        assertEquals(assignedId, taskFromManager.getId());
    }

    @Test
    void testUpdateTaskChangesValues() {
        Task task = new Task("Old Name", "Old Desc", TaskStatus.NEW);
        int id = taskManager.createTask(task);
        Task updatedTask = new Task("New Name", "New Desc", TaskStatus.DONE);
        updatedTask.setId(id);
        taskManager.updateTask(updatedTask);
        Task result = taskManager.getTasksById(id);
        assertEquals("New Name", result.getName());
        assertEquals("New Desc", result.getDescription());
        assertEquals(TaskStatus.DONE, result.getTaskStatus());
    }

    @Test
    void shouldRemoveTaskFromHistoryWhenTaskIsDeleted() {
        Task task1 = new Task("Task 1", "Desc", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Desc", TaskStatus.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.getTasksById(task1.getId());
        taskManager.getTasksById(task2.getId());
        taskManager.deleteTaskById(task1.getId());
        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task2, history.getFirst());

    }
}

