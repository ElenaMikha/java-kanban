package http;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        if (!"GET".equals(h.getRequestMethod())) {
            sendNotFound(h, "Unknown method");
            return;
        }
        try {
            sendText(h, gson.toJson(manager.getPrioritizedTasks()));
        } catch (Exception e) {
            sendInternalError(h, e.getMessage());
        }
    }
}

