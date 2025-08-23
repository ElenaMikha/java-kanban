package http;

import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TasksHandlerTest extends BaseHttpHandlerTest {

    @Test
    void createTaskReturns201AndSaves() throws Exception {
        Task t = new Task("T1", "desc", TaskStatus.NEW);
        t.setStartTime(LocalDateTime.now().plusMinutes(5));
        t.setDuration(Duration.ofMinutes(30));
        HttpResponse<String> resp = httpPost("/tasks", gson.toJson(t));
        assertEquals(201, resp.statusCode());
        assertTrue(resp.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        List<Task> list = manager.getTasks();
        assertEquals(1, list.size());
        assertEquals("T1", list.getFirst().getName());
    }

    @Test
    void getTasksReturns200AndJsonArray() throws Exception {
        Task a = new Task("A", "d", TaskStatus.NEW);
        a.setStartTime(LocalDateTime.now().plusMinutes(1));
        a.setDuration(Duration.ofMinutes(5));
        manager.createTask(a);
        HttpResponse<String> resp = httpGet("/tasks");
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().startsWith("["));
    }

    @Test
    void getTaskById200WhenExists404WhenMissing() throws Exception {
        Task a = new Task("A", "d", TaskStatus.NEW);
        int id = manager.createTask(a);
        assertEquals(200, httpGet("/tasks/" + id).statusCode());
        HttpResponse<String> nf = httpGet("/tasks/999999");
        assertEquals(404, nf.statusCode());
        assertTrue(nf.body().contains("\"error\""));
    }

    @Test
    void updateTask_viaPost_returns201_andUpdates() throws Exception {
        Task a = new Task("A", "d", TaskStatus.NEW);
        a.setStartTime(LocalDateTime.now().plusMinutes(2));
        a.setDuration(Duration.ofMinutes(10));
        int id = manager.createTask(a);
        a.setId(id);
        a.setName("A-upd");
        HttpResponse<String> resp = httpPost("/tasks", gson.toJson(a));
        assertEquals(201, resp.statusCode());
        assertEquals("A-upd", manager.getTasksById(id).getName());
    }

    @Test
    void deleteTask_returns200_andRemoves() throws Exception {
        Task a = new Task("A", "d", TaskStatus.NEW);
        int id = manager.createTask(a);
        HttpResponse<String> del = httpDelete("/tasks/" + id);
        assertEquals(200, del.statusCode());
        assertTrue(del.body().contains("deleted"));
        assertEquals(404, httpGet("/tasks/" + id).statusCode());
    }

    @Test
    void overlappingTasks_returns406() throws Exception {
        LocalDateTime base = LocalDateTime.now().plusMinutes(1);
        Task t1 = new Task("T1", "d", TaskStatus.NEW);
        t1.setStartTime(base);
        t1.setDuration(Duration.ofMinutes(30));
        manager.createTask(t1);
        Task t2 = new Task("T2", "d", TaskStatus.NEW);
        t2.setStartTime(base.plusMinutes(10));
        t2.setDuration(Duration.ofMinutes(15));
        HttpResponse<String> resp = httpPost("/tasks", gson.toJson(t2));
        assertEquals(406, resp.statusCode());
        assertTrue(resp.body().toLowerCase().contains("overlap"));
    }

    @Test
    void unsupportedMethod_returns500() throws Exception {
        java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://localhost:8080/tasks"))
                .method("PUT", java.net.http.HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
        assertEquals(500, resp.statusCode());
        assertTrue(resp.body().contains("Method not supported"));
    }
}
