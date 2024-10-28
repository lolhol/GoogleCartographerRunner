package org.pwrup;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.imageio.ImageIO;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import port.pwrup.google.potato.GetType;
import port.pwrup.google.potato.GooglePotato;
import port.pwrup.google.potato.ImuSensor;
import port.pwrup.google.potato.LidarSensor;
import port.pwrup.google.potato.Odom;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        var potato = new GooglePotato();

        List<ImuSensor> imuList = new ArrayList<>();
        List<Odom> odoms = new ArrayList<>();
        List<LidarSensor> lidarSensors = new ArrayList<>();
        lidarSensors.add(new LidarSensor(0, 0, 0, 10.0f, "range0"));

        potato.init("/mnt/configuration_files", "cartographer_config_main.lua", imuList, odoms, lidarSensors);

        System.out.println("Google Cartographer initialized!");

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);
        server.createContext("/lidar-msg", new LidarMessageHandler(potato));
        server.createContext("/imu-msg", new ImuMessageHandler(potato));
        server.createContext("/get-map", new GetMapMessageHandler(potato));

        server.start();
        System.out.println("Server started on port 8080");

        new CountDownLatch(1).await();

        /*
         * Thread.sleep(1000);
         * 
         * int count = 0;
         * while (true) {
         * Thread.sleep(100);
         * // System.out.println("Inserting points...");
         * var circlePoints = generateCircularPoints(360, 10);
         * potato.addLidarData(System.currentTimeMillis(), "range0", circlePoints[0],
         * circlePoints[1], circlePoints[2],
         * circlePoints[3]);
         * 
         * if (count == 20) {
         * var map = potato.getMap2D(1.0f, 127.0f);
         * 
         * List<int[]> points = new ArrayList<>();
         * for (int i = 0; i < map.points.length; i += 4) {
         * points.add(new int[] { (int) map.points[i], (int) map.points[i + 1] });
         * }
         * 
         * createPNGFromPoints(points, 100, 100, "mapOutput.png");
         * }
         * 
         * count++;
         * }
         */
    }

    public static float[][] generateCircularPoints(int numPoints, double radius) {
        float[] xPoints = new float[numPoints];
        float[] yPoints = new float[numPoints];
        float[] zPoints = new float[numPoints];
        float[] intensities = new float[numPoints];

        double angleIncrement = 2 * Math.PI / numPoints; // Divide the circle evenly

        for (int i = 0; i < numPoints; i++) {
            double angle = i * angleIncrement; // Angle for each point
            xPoints[i] = (float) (radius * Math.cos(angle)); // x = r * cos()
            yPoints[i] = (float) (radius * Math.sin(angle)); // y = r * sin()
            zPoints[i] = 0;
            intensities[i] = 0;
        }

        return new float[][] { xPoints, yPoints, zPoints, intensities }; // Return the arrays
    }

    public static void createPNGFromPoints(List<int[]> points, int width, int height, String fileName) {
        // Create a blank image with white background
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Set background color to white
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Set drawing color to black for points
        g2d.setColor(Color.BLACK);

        // Draw each point (x, y) in the list
        for (int[] point : points) {
            int x = point[0];
            int y = point[1];
            g2d.fillRect(x, y, 1, 1); // Draw a 1x1 pixel for each point
        }

        // Dispose of graphics object
        g2d.dispose();

        // Save the image as PNG
        try {
            ImageIO.write(image, "png", new File(fileName));
            System.out.println("PNG file created successfully.");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}