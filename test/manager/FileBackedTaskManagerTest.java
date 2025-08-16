package manager;

import org.junit.jupiter.api.Test;
import tasks.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    @TempDir
    Path tempDir;

    private File dataFile;

    @Override
    protected FileBackedTaskManager makeManager() {
        dataFile = tempDir.resolve("tasks.csv").toFile();
        return new FileBackedTaskManager(dataFile);
    }

    @Test
    void testSaveAndLoadEmptyFile() {
        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(dataFile);
        assertNotNull(loadedManager);
        assertTrue(loadedManager.getTasks().isEmpty());
        assertTrue(loadedManager.getEpics().isEmpty());
        assertTrue(loadedManager.getSubtasks().isEmpty());
    }

    @Test
    void testSaveAndLoadMultipleTasks() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.of(2025, 1, 1, 9, 0));
        task1.setDuration(Duration.ofMinutes(30));

        Epic epic1 = new Epic("Epic 1", "Epic Description");

        int taskId = manager.createTask(task1);
        int epicId = manager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask 1", "Subtask Description", TaskStatus.NEW, epicId);
        subtask1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        subtask1.setDuration(Duration.ofMinutes(20));

        int subtaskId = manager.createSubtask(subtask1);
        manager.save();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(dataFile);

        // Проверяем количество задач
        assertEquals(1, loadedManager.getTasks().size());
        assertEquals(1, loadedManager.getEpics().size());
        assertEquals(1, loadedManager.getSubtasks().size());

        // Проверяем task
        Task loadedTask = loadedManager.getTasks().getFirst();
        assertEquals(taskId, loadedTask.getId());
        assertEquals(task1.getName(), loadedTask.getName());
        assertEquals(task1.getDescription(), loadedTask.getDescription());
        assertEquals(task1.getTaskStatus(), loadedTask.getTaskStatus());
        assertEquals(TaskType.TASK, loadedTask.getType());
        assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), loadedTask.getStartTime());
        assertEquals(Duration.ofMinutes(30), loadedTask.getDuration());
        assertEquals(LocalDateTime.of(2025, 1, 1, 9, 30), loadedTask.getEndTime());

        // Проверяем epic
        Epic loadedEpic = loadedManager.getEpics().getFirst();
        assertEquals(epicId, loadedEpic.getId());
        assertEquals(epic1.getName(), loadedEpic.getName());
        assertEquals(epic1.getDescription(), loadedEpic.getDescription());
        assertEquals(TaskStatus.NEW, loadedEpic.getTaskStatus());
        assertEquals(TaskType.EPIC, loadedEpic.getType());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), loadedEpic.getStartTime());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 20), loadedEpic.getEndTime());
        assertEquals(Duration.ofMinutes(20), loadedEpic.getDuration());


        // Проверяем subtask
        Subtask loadedSubtask = loadedManager.getSubtasks().getFirst();
        assertEquals(subtaskId, loadedSubtask.getId());
        assertEquals(subtask1.getName(), loadedSubtask.getName());
        assertEquals(subtask1.getDescription(), loadedSubtask.getDescription());
        assertEquals(subtask1.getTaskStatus(), loadedSubtask.getTaskStatus());
        assertEquals(TaskType.SUBTASK, loadedSubtask.getType());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), loadedSubtask.getStartTime());
        assertEquals(Duration.ofMinutes(20), loadedSubtask.getDuration());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 20), loadedSubtask.getEndTime());

        // Проверяем связь подзадачи и эпика
        assertEquals(epicId, loadedSubtask.getEpicId());

        // Проверяем, что у эпика есть эта подзадача в списке
        List<Subtask> subtasksFromEpic = loadedEpic.getSubtasks();
        assertEquals(1, subtasksFromEpic.size());
        assertEquals(loadedSubtask, subtasksFromEpic.getFirst());

        List<Task> prioritized = loadedManager.getPrioritizedTasks();
        assertEquals(2, prioritized.size());
        assertEquals("Task 1", prioritized.get(0).getName());
        assertEquals("Subtask 1", prioritized.get(1).getName());
        for (Task task : prioritized) {
            assertNotEquals(TaskType.EPIC, task.getType());
        }
    }

    @Test
    void testLoadFromFileWithMultipleTasks() {
        // Создаем задачи через менеджер
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Epic epic1 = new Epic("Epic 1", "Epic Description");
        int taskId = manager.createTask(task1);
        int epicId = manager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask 1", "Subtask Description", TaskStatus.NEW, epicId);
        int subtaskId = manager.createSubtask(subtask1);

        // Сохраняем в файл
        manager.save();

        // Загружаем менеджер из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(dataFile);

        // Проверяем, что задачи загружены правильно
        Task loadedTask = loadedManager.getTasksById(taskId);
        assertNotNull(loadedTask);
        assertEquals(task1.getName(), loadedTask.getName());
        assertEquals(task1.getDescription(), loadedTask.getDescription());
        assertEquals(task1.getTaskStatus(), loadedTask.getTaskStatus());

        Epic loadedEpic = loadedManager.getEpicById(epicId);
        assertNotNull(loadedEpic);
        assertEquals(epic1.getName(), loadedEpic.getName());
        assertEquals(epic1.getDescription(), loadedEpic.getDescription());

        Subtask loadedSubtask = loadedManager.getSubtaskById(subtaskId);
        assertNotNull(loadedSubtask);
        assertEquals(subtask1.getName(), loadedSubtask.getName());
        assertEquals(subtask1.getDescription(), loadedSubtask.getDescription());
        assertEquals(subtask1.getTaskStatus(), loadedSubtask.getTaskStatus());
        assertEquals(epicId, loadedSubtask.getEpicId());

        // Проверяем связь подзадачи с эпиком
        List<Subtask> subtasksFromEpic = loadedEpic.getSubtasks();
        assertEquals(1, subtasksFromEpic.size());
        assertEquals(loadedSubtask, subtasksFromEpic.getFirst());
    }

    @Test
    void loadFromCorruptedFileThrowsIllegalArgumentException() throws Exception {
        File badFile = tempDir.resolve("bad.csv").toFile();
        Files.writeString(badFile.toPath(),
                "id,type,name,status,description,epic,startTime,durationMinutes\n" +
                        "42,UNKNOWN,Name,NEW,desc,,,\n");

        assertThrows(IllegalArgumentException.class,
                () -> FileBackedTaskManager.loadFromFile(badFile));
    }

    @Test
    void loadFromHeaderOnlyDoesNotThrow() throws Exception {
        File headerOnly = tempDir.resolve("header.csv").toFile();
        Files.writeString(headerOnly.toPath(),
                "id,type,name,status,description,epic,startTime,durationMinutes\n");

        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(headerOnly));
    }
}
