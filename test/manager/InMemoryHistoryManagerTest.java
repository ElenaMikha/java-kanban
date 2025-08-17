package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class InMemoryHistoryManagerTest {
    HistoryManager historyManager = Managers.getDefaultHistory();

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void testAddAndGetHistory() {
        assertNotNull(historyManager);
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.IN_PROGRESS);
        task2.setId(2);
        historyManager.add(task1);
        historyManager.add(task2);
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void testRemoveById() {
        Task task1 = new Task("Task 1", "Desc", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Desc", TaskStatus.NEW);
        task2.setId(2);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task2, history.getFirst());
    }

    @Test
    void testAddTasksMaintainsOrder() {
        Task task1 = new Task("Task 1", "Desc 1", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Desc 2", TaskStatus.NEW);
        task2.setId(2);
        Task task3 = new Task("Task 3", "Desc 3", TaskStatus.NEW);
        task3.setId(3);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));
    }

    @Test
    void testRemoveTail() {
        Task task1 = new Task("Task 1", "Desc", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Desc", TaskStatus.NEW);
        task2.setId(2);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(2);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task1, history.getFirst());
    }

    @Test
    void testRemoveHead() {
        Task task1 = new Task("Task 1", "Desc", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Desc", TaskStatus.NEW);
        task2.setId(2);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task2, history.getFirst());
    }

    @Test
    void testEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой");
    }

    @Test
    void testDuplicatesMoveToTailAndStaySingle() {
        Task a = new Task("A", "", TaskStatus.NEW);
        a.setId(1);
        Task b = new Task("B", "", TaskStatus.NEW);
        b.setId(2);
        Task c = new Task("C", "", TaskStatus.NEW);
        c.setId(3);

        historyManager.add(a);
        historyManager.add(b);
        historyManager.add(c);
        historyManager.add(b);

        var ids = historyManager.getHistory().stream().map(Task::getId).toList();
        assertEquals(List.of(1, 3, 2), ids);
    }

    @Test
    void testRemoveMiddle() {
        Task a = new Task("A", "", TaskStatus.NEW);
        a.setId(1);
        Task b = new Task("B", "", TaskStatus.NEW);
        b.setId(2);
        Task c = new Task("C", "", TaskStatus.NEW);
        c.setId(3);

        historyManager.add(a);
        historyManager.add(b);
        historyManager.add(c);

        historyManager.remove(2);
        var ids = historyManager.getHistory().stream().map(Task::getId).toList();
        assertEquals(List.of(1, 3), ids);
    }

    @Test
    void testRemoveNonExistingIsNoOp() {
        Task a = new Task("A", "", TaskStatus.NEW);
        a.setId(1);
        historyManager.add(a);
        assertDoesNotThrow(() -> historyManager.remove(999));
        var ids = historyManager.getHistory().stream().map(Task::getId).toList();
        assertEquals(List.of(1), ids);
    }

    @Test
    void testAddNullIsNoOp() {
        assertDoesNotThrow(() -> historyManager.add(null));
        assertTrue(historyManager.getHistory().isEmpty());
    }

}
