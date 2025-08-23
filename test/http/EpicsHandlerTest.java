package http;

import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.TaskStatus;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class EpicsHandlerTest extends BaseHttpHandlerTest {

    @Test
    void createEpicReturns201AndSaved() throws Exception {
        Epic e = new Epic("E1", "d");
        HttpResponse<String> resp = httpPost("/epics", gson.toJson(e));
        assertEquals(201, resp.statusCode());
        assertEquals(1, manager.getEpics().size());
    }

    @Test
    void getEpicByIdAndGetAllAnd404() throws Exception {
        int id = manager.createEpic(new Epic("E1", "d"));
        assertEquals(200, httpGet("/epics").statusCode());
        assertEquals(200, httpGet("/epics/" + id).statusCode());
        assertEquals(404, httpGet("/epics/999999").statusCode());
    }

    @Test
    void updateEpicViaPost201AndUpdated() throws Exception {
        int id = manager.createEpic(new Epic("E1", "d"));
        Epic upd = new Epic("E1-upd", "d2");
        upd.setId(id);
        HttpResponse<String> resp = httpPost("/epics", gson.toJson(upd));
        assertEquals(201, resp.statusCode());
        assertEquals("E1-upd", manager.getEpicById(id).getName());
    }

    @Test
    void deleteEpicByIdRemovesSubtasksAndHistory() throws Exception {
        int epicId = manager.createEpic(new Epic("E", "d"));
        Subtask s = new Subtask("S", "sd", TaskStatus.NEW, epicId);
        s.setStartTime(LocalDateTime.now().plusMinutes(2));
        s.setDuration(Duration.ofMinutes(5));
        int sid = manager.createSubtask(s);
        httpGet("/epics/" + epicId);
        httpGet("/subtasks/" + sid);
        HttpResponse<String> del = httpDelete("/epics/" + epicId);
        assertEquals(200, del.statusCode());
        assertTrue(manager.getSubtasks().isEmpty());
        assertEquals(404, httpGet("/epics/" + epicId).statusCode());
        assertTrue(manager.getHistory().stream().noneMatch(t -> t.getId() == sid));
    }

    @Test
    void getSubtasksOfEpicReturns200() throws Exception {
        int epicId = manager.createEpic(new Epic("E", "d"));
        Subtask s1 = new Subtask("S1", "d", TaskStatus.NEW, epicId);
        s1.setStartTime(LocalDateTime.now().plusMinutes(1));
        s1.setDuration(Duration.ofMinutes(3));
        manager.createSubtask(s1);
        HttpResponse<String> resp = httpGet("/epics/" + epicId + "/subtasks");
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().startsWith("["));
    }
}
