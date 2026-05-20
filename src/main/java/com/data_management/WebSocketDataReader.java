package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/**
 * Implements {@link DataReader} by connecting to a WebSocket server and
 * streaming incoming messages into a {@link DataStorage} instance in real time.
 *
 * <p>Expected message format (same as file-based output):
 * {@code Patient ID: <id>, Timestamp: <ts>, Label: <label>, Data: <value>}
 */
public class WebSocketDataReader implements DataReader {
    private final String serverUri;
    private WebSocketClient client;

    /**
     * Constructs a {@code WebSocketDataReader} that will connect to the given URI.
     *
     * @param serverUri the WebSocket server URI, e.g. {@code ws://localhost:8080}
     */
    public WebSocketDataReader(String serverUri) {
        this.serverUri = serverUri;
    }

    /**
     * Connects to the WebSocket server and begins streaming data into the provided
     * storage. The connection is asynchronous; this method returns immediately after
     * initiating the connection.
     *
     * @param dataStorage the storage where incoming records will be saved
     * @throws IOException if the URI is syntactically invalid
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        try {
            URI uri = new URI(serverUri);
            client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("WebSocket connected: " + serverUri);
                }

                @Override
                public void onMessage(String message) {
                    parseAndStore(message, dataStorage);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("WebSocket closed: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("WebSocket error: " + ex.getMessage());
                }
            };
            client.connectBlocking(10, TimeUnit.SECONDS);
        } catch (URISyntaxException e) {
            throw new IOException("Invalid WebSocket URI: " + serverUri, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("WebSocket connection interrupted", e);
        }
    }

    /**
     * Parses a single message string and stores the record in the given storage.
     * Malformed messages are skipped with a warning printed to stderr.
     *
     * @param message     the raw message received from the WebSocket server
     * @param dataStorage the storage object to insert the parsed record into
     */
    public void parseAndStore(String message, DataStorage dataStorage) {
        if (message == null || message.trim().isEmpty()) return;
        try {
            String[] parts = message.split(", ");
            int patientId = Integer.parseInt(parts[0].replace("Patient ID: ", "").trim());
            long timestamp = Long.parseLong(parts[1].replace("Timestamp: ", "").trim());
            String label = parts[2].replace("Label: ", "").trim();
            String dataStr = parts[3].replace("Data: ", "").replace("%", "").trim();
            double value = Double.parseDouble(dataStr);
            dataStorage.addPatientData(patientId, value, label, timestamp);
        } catch (Exception e) {
            System.err.println("Skipping malformed message: [" + message + "] - " + e.getMessage());
        }
    }

    /**
     * Closes the WebSocket connection if it is currently open.
     */
    public void disconnect() {
        if (client != null && client.isOpen()) {
            client.close();
        }
    }
}
