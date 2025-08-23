package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;

    public HistoryHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        String path = h.getRequestURI().getPath();
        if (!"GET".equals(h.getRequestMethod()) || !"/history".equals(path)) {
            sendNotFound(h, "Unknown path or method");
            return;
        }
        try {
            sendText(h, gson.toJson(manager.getHistory()));
        } catch (Exception e) {
            sendInternalError(h, e.getMessage());
        }
    }
}

