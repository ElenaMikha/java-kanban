package tasks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    @Test
    public void testConstructorAndGetters() {
        Task task = new Task(1, "Test Task", "Description", TaskStatus.NEW);

        assertEquals(1, task.getId());
        assertEquals("Test Task", task.getName());
        assertEquals("Description", task.getDescription());
        assertEquals(TaskStatus.NEW, task.getTaskStatus());
    }

    @Test
    public void testToString() {
        Task task = new Task(1, "TaskName", "TaskDesc", TaskStatus.NEW);
        String str = task.toString();

        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("name='TaskName'"));
        assertTrue(str.contains("description='TaskDesc'"));
        assertTrue(str.contains("taskStatus=NEW"));
    }
}
