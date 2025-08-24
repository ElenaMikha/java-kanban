package http;

import com.sun.net.httpserver.HttpExchange;
import exception.IntersectionException;
import exception.NotFoundException;
import manager.TaskManager;
import tasks.Subtask;

import java.io.IOException;

public class SubtasksHandler extends BaseHttpHandler {
    private static final String BASE = "/subtasks";

    public SubtasksHandler(TaskManager manager) {
        super(manager);
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
                        String bodyStr = readBody(h);
                        if (bodyStr == null || bodyStr.isBlank()) {
                            sendBadRequest(h, "Request body is empty");
                            return;
                        }
                        Subtask body = gson.fromJson(bodyStr, Subtask.class);
                        if (body == null) {
                            sendBadRequest(h, "Subtask is null");
                            return;
                        }
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
