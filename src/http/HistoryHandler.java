package http;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        if (!"GET".equals(h.getRequestMethod())) {
            sendNotFound(h, "Unknown method");
            return;
        }
        try {
            sendText(h, gson.toJson(manager.getHistory()));
        } catch (Exception e) {
            sendInternalError(h, e.getMessage());
        }
    }
}

