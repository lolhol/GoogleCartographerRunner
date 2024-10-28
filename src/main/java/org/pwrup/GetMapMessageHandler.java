package org.pwrup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.pwrup.message.GetMap;
import org.pwrup.message.Lidar;

import com.google.gson.Gson;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import port.pwrup.google.potato.GooglePotato;

import com.sun.net.httpserver.HttpExchange;

public class GetMapMessageHandler implements HttpHandler {

    private GooglePotato potato;
    private final Gson gson = new Gson();

    public GetMapMessageHandler(
            GooglePotato potato) {
        this.potato = potato;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            InputStream requestBody = exchange.getRequestBody();
            String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);

            // Deserialize JSON into GetMap class
            GetMap lidarMsg = gson.fromJson(body, GetMap.class);

            // Get the 2D map points and serialize it back to JSON
            String mapPoints = gson.toJson(potato.getMap2D(lidarMsg.alpha, lidarMsg.intensity));

            // Set response headers for JSON
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, mapPoints.getBytes(StandardCharsets.UTF_8).length);

            // Write the JSON response
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(mapPoints.getBytes(StandardCharsets.UTF_8));
            }
        } else {
            // Send a 405 Method Not Allowed response for non-POST requests
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(405, -1);
        }
    }
}
