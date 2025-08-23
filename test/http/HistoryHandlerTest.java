package http;

import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryHandlerTest extends BaseHttpHandlerTest {

    @Test
    void historyReturnsViewedItems() throws Exception {
        Task t = new Task("T", "d", TaskStatus.NEW);
        t.setStartTime(LocalDateTime.now().plusMinutes(5));
        t.setDuration(Duration.ofMinutes(5));
        int id = manager.createTask(t);
        httpGet("/tasks/" + id);
        HttpResponse<String> resp = httpGet("/history");
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("\"name\":\"T\""));
    }

    @Test
    void historyOnEmptyReturnsEmptyArray() throws Exception {
        HttpResponse<String> resp = httpGet("/history");
        assertEquals(200, resp.statusCode());
        assertEquals("[]", resp.body());
    }
}
