package data_management;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import com.data_management.WebSocketDataReader;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for {@link WebSocketDataReader}.
 * Starts an embedded WebSocket server, connects the reader to it, and
 * verifies that streamed messages are stored in {@link DataStorage}.
 */
class WebSocketIntegrationTest {
    private static final int PORT = 8766;
    private TestWebSocketServer server;
    private WebSocketDataReader reader;

    @BeforeEach
    void setUp() throws Exception {
        DataStorage.resetInstance();
        server = new TestWebSocketServer(PORT);
        server.start();
        // Wait for the server's onStart() callback rather than sleeping blindly
        assertTrue(server.awaitStart(5, TimeUnit.SECONDS), "Server did not start within 5 seconds");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (reader != null) reader.disconnect();
        if (server != null) server.stop(1000);
    }

    @Test
    void testDataReceivedAndStoredFromServer() throws Exception {
        DataStorage storage = DataStorage.getInstance();
        reader = new WebSocketDataReader("ws://localhost:" + PORT);
        reader.readData(storage);

        // readData() used connectBlocking(), so the handshake is already done
        // and the server's onOpen() message is in flight — short poll suffices
        List<PatientRecord> records = List.of();
        for (int i = 0; i < 50; i++) {
            Thread.sleep(100);
            records = storage.getRecords(1, 0, Long.MAX_VALUE);
            if (!records.isEmpty()) break;
        }

        assertFalse(records.isEmpty(), "Expected at least one record from the WebSocket server");
        assertEquals(75.0, records.get(0).getMeasurementValue());
        assertEquals("HeartRate", records.get(0).getRecordType());
    }

    /** Minimal server that sends one patient record as soon as a client connects. */
    private static class TestWebSocketServer extends WebSocketServer {
        private final CountDownLatch startLatch = new CountDownLatch(1);

        TestWebSocketServer(int port) {
            super(new InetSocketAddress(port));
        }

        boolean awaitStart(long timeout, TimeUnit unit) throws InterruptedException {
            return startLatch.await(timeout, unit);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            conn.send("Patient ID: 1, Timestamp: 1000, Label: HeartRate, Data: 75.0");
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {}

        @Override
        public void onMessage(WebSocket conn, String message) {}

        @Override
        public void onError(WebSocket conn, Exception ex) {}

        @Override
        public void onStart() {
            startLatch.countDown();
        }
    }
}
