package http;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager manager;

    protected BaseHttpHandler(TaskManager manager) {
        this.manager = manager;
    }

    protected final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, type, ctx)
                            -> LocalDateTime.parse(json.getAsString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, type, ctx)
                            -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(Duration.class,
                    (JsonDeserializer<Duration>) (json, type, ctx)
                            -> Duration.parse(json.getAsString()))
            .registerTypeAdapter(Duration.class,
                    (JsonSerializer<Duration>) (src, type, ctx)
                            -> new JsonPrimitive(src.toString()))
            .create();

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
        h.getResponseBody().close();
        h.close();
    }

    protected void sendCreated(HttpExchange h) throws IOException {
        h.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(201, 0);
        h.getResponseBody().close();
        h.close();
    }

    protected void sendNotFound(HttpExchange h, String message) throws IOException {
        sendError(h, 404, message == null ? "Not Found" : message);
    }

    protected void sendHasInteractions(HttpExchange h, String message) throws IOException {
        sendError(h, 406, message == null ? "Task overlaps with another" : message);
    }

    protected void sendInternalError(HttpExchange h, String message) throws IOException {
        sendError(h, 500, message == null ? "Internal Server Error" : message);
    }

    protected void sendBadRequest(HttpExchange h, String message) throws IOException {
        sendError(h, 400, message == null ? "Bad Request" : message);
    }


    private void sendError(HttpExchange h, int code, String message) throws IOException {
        String safe = message.replace("\"", "\\\"");
        String body = "{\"error\":\"" + safe + "\"}";
        byte[] resp = body.getBytes(StandardCharsets.UTF_8);

        h.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(code, resp.length);
        h.getResponseBody().write(resp);
        h.getResponseBody().close();
        h.close();
    }

    protected String readBody(HttpExchange h) throws IOException {
        try (InputStream is = h.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
