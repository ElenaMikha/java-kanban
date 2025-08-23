package http;

import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.TaskStatus;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SubtasksHandlerTest extends BaseHttpHandlerTest {

    @Test
    void createGetDeleteSubtask() throws Exception {
        int epicId = manager.createEpic(new Epic("E", "d"));
        Subtask s = new Subtask("S", "d", TaskStatus.NEW, epicId);
        s.setStartTime(LocalDateTime.now().plusMinutes(1));
        s.setDuration(Duration.ofMinutes(10));
        HttpResponse<String> created = httpPost("/subtasks", gson.toJson(s));
        assertEquals(201, created.statusCode());
        int subId = manager.getSubtasks().getFirst().getId();
        assertEquals(200, httpGet("/subtasks/" + subId).statusCode());
        HttpResponse<String> del = httpDelete("/subtasks/" + subId);
        assertEquals(200, del.statusCode());
        assertTrue(manager.getSubtasks().isEmpty());
    }

    @Test
    void createSubtask_withMissingEpicReturns404() throws Exception {
        Subtask s = new Subtask("S", "d", TaskStatus.NEW, 999999);
        HttpResponse<String> resp = httpPost("/subtasks", gson.toJson(s));
        assertEquals(404, resp.statusCode());
        assertTrue(resp.body().contains("\"error\""));
    }

    @Test
    void overlappingSubtasksReturns406() throws Exception {
        int epicId = manager.createEpic(new Epic("E", "d"));
        LocalDateTime base = LocalDateTime.now().plusMinutes(1);
        Subtask s1 = new Subtask("S1", "d", TaskStatus.NEW, epicId);
        s1.setStartTime(base);
        s1.setDuration(Duration.ofMinutes(15));
        manager.createSubtask(s1);
        Subtask s2 = new Subtask("S2", "d", TaskStatus.NEW, epicId);
        s2.setStartTime(base.plusMinutes(5));
        s2.setDuration(Duration.ofMinutes(10));
        HttpResponse<String> resp = httpPost("/subtasks", gson.toJson(s2));
        assertEquals(406, resp.statusCode());
        assertTrue(resp.body().toLowerCase().contains("overlap"));
    }
}
