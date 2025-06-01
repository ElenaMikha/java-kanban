package manager;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

public class ManagersTest {
    TaskManager taskManager = Managers.getDefault();
    HistoryManager history = Managers.getDefaultHistory();

    @Test
     void TestGetDefaultShouldReturnInitializedTaskManagerForTask() {
        assertNotNull(taskManager);
        Task task = new Task("Task", "Description", TaskStatus.NEW);
        int id = taskManager.createTask(task);
        Task retrieved = taskManager.getTasksById(id);
        assertNotNull(retrieved);
        assertEquals("Task", retrieved.getName());
    }

    @Test
    void TestGetDefaultShouldReturnInitializedTaskManagerForEpicAndSubtask() {
        assertNotNull(taskManager);
        Epic epic = new Epic("Epic", "One subtask");
        int epicId = taskManager.createEpic(epic);
        Epic retrievedEpic = taskManager.getEpicById(epicId);
        assertNotNull(retrievedEpic);

        Subtask subtask = new Subtask("Subtask", "Description", TaskStatus.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask);
        Subtask retrievedSubtasks = taskManager.getSubtaskById(subtaskId);
        assertNotNull(retrievedSubtasks);

        assertEquals(subtaskId, retrievedSubtasks.getId());
        assertEquals(epicId, retrievedSubtasks.getEpicId());
        assertEquals("Epic", retrievedEpic.getName());
        assertEquals("Subtask", retrievedSubtasks.getName());
    }

    @Test
    void TestGetDefaultHistoryShouldReturnInitializedHistoryManager() {
        assertNotNull(history);
        Task task = new Task("History Task", "Test description", TaskStatus.NEW);
        task.setId(1);
        history.add(task);
        assertEquals(1, history.getHistory().size());
    }
}
