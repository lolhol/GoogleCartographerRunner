package org.pwrup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.pwrup.message.Imu;
import org.pwrup.message.Lidar;
import org.pwrup.message.Odom;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import port.pwrup.google.potato.GooglePotato;

public class OdomMessageHandler implements HttpHandler {

    private final GooglePotato potato;
    private final Gson gson = new Gson();

    public OdomMessageHandler(GooglePotato potato) {
        this.potato = potato;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            InputStream requestBody = exchange.getRequestBody();
            String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);

            Odom odomMsg = gson.fromJson(body, Odom.class);

            potato.addOdomData(odomMsg.time, odomMsg.name, odomMsg.position, odomMsg.quaternion);

            exchange.sendResponseHeaders(200, 0);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write("Lidar data received".getBytes(StandardCharsets.UTF_8));
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

}
