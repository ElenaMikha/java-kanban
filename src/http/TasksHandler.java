package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.IntersectionException;
import exception.NotFoundException;
import manager.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.util.List;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private static final String BASE = "/tasks";
    private final TaskManager manager;

    public TasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        String path = h.getRequestURI().getPath();
        String method = h.getRequestMethod();

        try {
            if (BASE.equals(path)) {
                switch (method) {
                    case "GET" -> {
                        List<Task> list = manager.getTasks();
                        sendText(h, gson.toJson(list));
                    }
                    case "POST" -> {
                        Task body = gson.fromJson(readBody(h), Task.class);
                        if (body.getId() == null) manager.createTask(body);
                        else manager.updateTask(body);
                        sendCreated(h);
                    }
                    default -> sendInternalError(h, "Method not supported");
                }
                return;
            }

            if (path.startsWith(BASE + "/")) {
                String idStr = path.substring((BASE + "/").length());
                int id;
                try {
                    id = Integer.parseInt(idStr);
                } catch (NumberFormatException ex) {
                    sendNotFound(h, "Invalid task id");
                    return;
                }

                switch (method) {
                    case "GET" -> {
                        Task t = manager.getTasksById(id);
                        sendText(h, gson.toJson(t));
                    }
                    case "DELETE" -> {
                        manager.deleteTaskById(id);
                        sendText(h, "{\"result\":\"deleted\"}");
                    }
                    default -> sendInternalError(h, "Method not supported");
                }
                return;
            }

            sendNotFound(h, "Unknown path");
        } catch (NotFoundException e) {
            sendNotFound(h, e.getMessage());
        } catch (IntersectionException e) {
            sendHasInteractions(h, e.getMessage());
        } catch (Exception e) {
            sendInternalError(h, e.getMessage());
        }
    }
}
