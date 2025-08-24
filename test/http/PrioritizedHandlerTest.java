package http;

import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PrioritizedHandlerTest extends BaseHttpHandlerTest {

    @Test
    void prioritizedReturnsSortedByStartExcludesWithoutStart() throws Exception {
        Task t1 = new Task("A", "d", TaskStatus.NEW);
        t1.setStartTime(LocalDateTime.now().plusMinutes(30));
        t1.setDuration(Duration.ofMinutes(5));
        manager.createTask(t1);
        Task t2 = new Task("B", "d", TaskStatus.NEW);
        t2.setStartTime(LocalDateTime.now().plusMinutes(10));
        t2.setDuration(Duration.ofMinutes(5));
        manager.createTask(t2);
        Task t3 = new Task("C", "d", TaskStatus.NEW);
        manager.createTask(t3);
        HttpResponse<String> resp = httpGet("/prioritized");
        assertEquals(200, resp.statusCode());
        String body = resp.body();
        int bIdx = body.indexOf("\"name\":\"B\"");
        int aIdx = body.indexOf("\"name\":\"A\"");
        int cIdx = body.indexOf("\"name\":\"C\"");
        assertTrue(bIdx != -1 && aIdx != -1);
        assertTrue(bIdx < aIdx);
        assertEquals(-1, cIdx);
    }
}
