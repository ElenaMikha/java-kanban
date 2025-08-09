package manager;

import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {
    TaskManager taskManager = Managers.getDefault();

    @Test
    void testCreateTask() {
        taskManager.createTask(new Task("Task 1", "Description 1", TaskStatus.NEW));
        List<Task> tasks = taskManager.getTasks();
        assertEquals(1, tasks.size());
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
    void testGetDefaultTaskManagerNotNullAndWorks() {
        assertNotNull(taskManager);
        int id = taskManager.createTask(new tasks.Task("Test task", "Description",
                tasks.TaskStatus.NEW));
        assertTrue(id > 0);
        assertFalse(taskManager.getTasks().isEmpty());
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
    void testHistoryManagerShouldKeepTaskAsItWasWhenAdded() {
        Task task = new Task("HistoryTask", "Initial description", TaskStatus.NEW);
        int id = taskManager.createTask(task);
        Task retrieved = taskManager.getTasksById(id);
        retrieved.setName("Changed name");
        retrieved.setDescription("Changed description");
        retrieved.setTaskStatus(TaskStatus.DONE);
        List<Task> history = taskManager.getHistory();

        Task taskInHistory = null;
        for (Task t : history) {
            if (t.getId().equals(id)) {
                taskInHistory = t;
                break;
            }
        }
        assertNotNull(taskInHistory);
        assertEquals(id, taskInHistory.getId());
        assertEquals("Changed name", taskInHistory.getName());
        assertEquals("Changed description", taskInHistory.getDescription());
        assertEquals(TaskStatus.DONE, taskInHistory.getTaskStatus());
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
