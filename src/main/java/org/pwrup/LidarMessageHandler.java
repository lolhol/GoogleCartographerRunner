package org.pwrup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.pwrup.message.Lidar;

import com.google.gson.Gson;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import port.pwrup.google.potato.GooglePotato;

public class LidarMessageHandler implements HttpHandler {

    private final GooglePotato potato;
    private final Gson gson = new Gson();

    public LidarMessageHandler(GooglePotato potato) {
        this.potato = potato;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            InputStream requestBody = exchange.getRequestBody();
            String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);

            // Deserialize JSON to Lidar object
            Lidar lidarMsg = gson.fromJson(body, Lidar.class);

            // Process the data
            potato.addLidarData(lidarMsg.time, lidarMsg.name, lidarMsg.x, lidarMsg.y, lidarMsg.z, lidarMsg.i);

            // System.out.println("Lidar MSG received!");

            // Send 200 OK response
            exchange.sendResponseHeaders(200, 0);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write("Lidar data received".getBytes(StandardCharsets.UTF_8));
            }
        } else {
            // Method not allowed for non-POST requests
            exchange.sendResponseHeaders(405, -1);
        }
    }
}