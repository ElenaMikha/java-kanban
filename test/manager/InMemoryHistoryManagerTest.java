package manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    HistoryManager historyManager = Managers.getDefaultHistory();

    @Test
    void testGetDefaultHistoryManagerNotNullAndWorks() {
        assertNotNull(historyManager);
        tasks.Task task = new tasks.Task("History task", "Desc", tasks.TaskStatus.NEW);
        historyManager.add(task);
        assertFalse(historyManager.getHistory().isEmpty());
        assertEquals(task, historyManager.getHistory().get(0));
    }

}
