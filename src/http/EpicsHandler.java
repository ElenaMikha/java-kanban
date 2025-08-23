package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.NotFoundException;
import manager.TaskManager;
import tasks.Epic;

import java.io.IOException;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;

    public EpicsHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        String path = h.getRequestURI().getPath();
        String method = h.getRequestMethod();

        try {
            switch (method) {
                case "GET" -> {
                    if ("/epics".equals(path)) {
                        sendText(h, gson.toJson(manager.getEpics()));
                        return;
                    }
                    if (path.startsWith("/epics/")) {
                        String rest = path.substring("/epics/".length());
                        int slash = rest.indexOf('/');
                        if (slash == -1) {
                            int id = Integer.parseInt(rest);
                            sendText(h, gson.toJson(manager.getEpicById(id)));
                        } else {
                            int id = Integer.parseInt(rest.substring(0, slash));
                            String tail = rest.substring(slash + 1);
                            if ("subtasks".equals(tail)) {
                                sendText(h, gson.toJson(manager.getSubtaskFromEpic(id)));
                            } else {
                                sendNotFound(h, "Unknown path");
                            }
                        }
                        return;
                    }
                    sendNotFound(h, "Unknown path");
                }
                case "POST" -> {
                    if (!"/epics".equals(path)) {
                        sendNotFound(h, "Unknown path");
                        return;
                    }
                    Epic body = gson.fromJson(readBody(h), Epic.class);
                    if (body.getId() == null) manager.createEpic(body);
                    else manager.updateEpic(body);
                    sendCreated(h);
                }
                case "DELETE" -> {
                    if (path.startsWith("/epics/")) {
                        int id = Integer.parseInt(path.substring("/epics/".length()));
                        manager.deleteEpicById(id);
                        sendText(h, "{\"result\":\"deleted\"}");
                    } else {
                        sendNotFound(h, "Unknown path");
                    }
                }
                case null, default -> sendInternalError(h, "Method not supported");
            }
        } catch (NotFoundException e) {
            sendNotFound(h, e.getMessage());
        } catch (Exception e) {
            sendInternalError(h, e.getMessage());
        }
    }
}

