package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * Implements the {@link OutputStrategy} interface to stream patient data
 * over a TCP socket connection. This class starts a TCP server on a specified
 * port and waits for a single client to connect. Once connected, patient data
 * is sent to the client as formatted text messages.
 */
public class TcpOutputStrategy implements OutputStrategy {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;

    /**
     * Constructs a {@code TcpOutputStrategy} that starts a TCP server on the given port.
     * Client connections are accepted in a separate thread to avoid blocking the main thread.
     *
     * @param port the port number on which the TCP server will listen for connections
     */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the patient data to the connected TCP client as a comma-separated message.
     * If no client is connected, the output is silently skipped.
     *
     * @param patientId the unique identifier of the patient
     * @param timestamp the time at which the data was generated, in milliseconds since epoch
     * @param label     the type of health data being sent (e.g., "ECG", "BloodPressure")
     * @param data      the actual data value to be sent as a string
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}