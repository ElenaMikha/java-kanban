package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.IntersectionException;
import exception.NotFoundException;
import manager.TaskManager;
import tasks.Subtask;

import java.io.IOException;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private static final String BASE = "/subtasks";
    private final TaskManager manager;

    public SubtasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        String path = h.getRequestURI().getPath();
        String method = h.getRequestMethod();

        try {
            if (BASE.equals(path)) {
                switch (method) {
                    case "GET" -> sendText(h, gson.toJson(manager.getSubtasks()));
                    case "POST" -> {
                        Subtask body = gson.fromJson(readBody(h), Subtask.class);
                        if (body.getId() == null) manager.createSubtask(body);
                        else manager.updateSubtask(body);
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
                    sendNotFound(h, "Invalid subtask id");
                    return;
                }

                switch (method) {
                    case "GET" -> sendText(h, gson.toJson(manager.getSubtaskById(id)));
                    case "DELETE" -> {
                        manager.deleteSubtaskById(id);
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
