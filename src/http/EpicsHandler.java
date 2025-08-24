package http;

import com.sun.net.httpserver.HttpExchange;
import exception.NotFoundException;
import manager.TaskManager;
import tasks.Epic;

import java.io.IOException;

public class EpicsHandler extends BaseHttpHandler {
    private static final String BASE = "/epics";

    public EpicsHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        String path = h.getRequestURI().getPath();
        String method = h.getRequestMethod();

        try {
            String rest = path.startsWith(BASE) ? path.substring(BASE.length()) : "";
            if (rest.startsWith("/")) rest = rest.substring(1);

            switch (method) {
                case "GET" -> {
                    if (rest.isEmpty()) {
                        sendText(h, gson.toJson(manager.getEpics()));
                        return;
                    }
                    int slash = rest.indexOf('/');
                    if (slash == -1) {
                        try {
                            int id = Integer.parseInt(rest);
                            sendText(h, gson.toJson(manager.getEpicById(id)));
                        } catch (NumberFormatException ex) {
                            sendNotFound(h, "Invalid epic id");
                        }
                    } else {
                        try {
                            int id = Integer.parseInt(rest.substring(0, slash));
                            String tail = rest.substring(slash + 1);
                            if ("subtasks".equals(tail)) {
                                sendText(h, gson.toJson(manager.getSubtaskFromEpic(id)));
                            } else {
                                sendNotFound(h, "Unknown path");

                            }
                        } catch (NumberFormatException ex) {
                            sendNotFound(h, "Invalid epic id");

                        }

                    }

                }
                case "POST" -> {
                    if (!rest.isEmpty()) {
                        sendNotFound(h, "Unknown path");
                        return;
                    }
                    String bodyStr = readBody(h);
                    if (bodyStr == null || bodyStr.isBlank()) {
                        sendBadRequest(h, "Request body is empty");
                        return;
                    }
                    Epic body = gson.fromJson(bodyStr, Epic.class);

                    if (body == null) {
                        sendBadRequest(h, "Epic is null");
                        return;
                    }
                    if (body.getId() != null) {
                        sendBadRequest(h, "Epic id must be null on create");
                        return;
                    }

                    manager.createEpic(body);
                    sendCreated(h);
                }
                case "DELETE" -> {
                    if (rest.isEmpty() || rest.contains("/")) {
                        sendNotFound(h, "Unknown path");
                        return;
                    }

                    try {
                        int id = Integer.parseInt(rest);
                        manager.deleteEpicById(id);
                        sendText(h, "{\"result\":\"deleted\"}");

                    } catch (NumberFormatException ex) {
                        sendNotFound(h, "Invalid epic id");


                    }

                }
                default -> sendInternalError(h, "Method not supported");
            }
        } catch (NotFoundException e) {
            sendNotFound(h, e.getMessage());
        } catch (Exception e) {
            sendInternalError(h, e.getMessage());
        }
    }
}

