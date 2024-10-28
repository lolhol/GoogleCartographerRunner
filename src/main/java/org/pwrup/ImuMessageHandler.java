package org.pwrup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.pwrup.message.Imu;
import org.pwrup.message.Lidar;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import port.pwrup.google.potato.GooglePotato;

public class ImuMessageHandler implements HttpHandler {

    private final GooglePotato potato;
    private final Gson gson = new Gson();

    public ImuMessageHandler(GooglePotato googlePotato) {
        this.potato = googlePotato;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            InputStream requestBody = exchange.getRequestBody();
            String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);

            Imu imuMsg = gson.fromJson(body, Imu.class);

            potato.addIMUData(imuMsg.time, imuMsg.name, imuMsg.linear_acceleration, imuMsg.angular_velocity);

            exchange.sendResponseHeaders(200, 0);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write("Lidar data received".getBytes(StandardCharsets.UTF_8));
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

}
