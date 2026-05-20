package data_management;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import com.data_management.WebSocketDataReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link WebSocketDataReader}.
 * Tests message parsing and URI validation without requiring a live server.
 */
class WebSocketDataReaderTest {

    private DataStorage storage;
    private WebSocketDataReader reader;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
        reader = new WebSocketDataReader("ws://localhost:8080");
    }

    @Test
    void testParseAndStoreValidMessage() {
        reader.parseAndStore("Patient ID: 1, Timestamp: 1000, Label: HeartRate, Data: 75.0", storage);

        List<PatientRecord> records = storage.getRecords(1, 0, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(75.0, records.get(0).getMeasurementValue());
        assertEquals("HeartRate", records.get(0).getRecordType());
        assertEquals(1000L, records.get(0).getTimestamp());
    }

    @Test
    void testParseAndStoreStripsPercentFromSaturation() {
        reader.parseAndStore("Patient ID: 1, Timestamp: 1000, Label: Saturation, Data: 98.0%", storage);

        assertEquals(98.0, storage.getRecords(1, 0, Long.MAX_VALUE).get(0).getMeasurementValue());
    }

    @Test
    void testParseAndStoreMalformedMessageIsSkipped() {
        assertDoesNotThrow(() -> reader.parseAndStore("THIS IS MALFORMED", storage));
        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testParseAndStoreNullMessageIsSkipped() {
        assertDoesNotThrow(() -> reader.parseAndStore(null, storage));
        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testParseAndStoreEmptyMessageIsSkipped() {
        assertDoesNotThrow(() -> reader.parseAndStore("", storage));
        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testParseAndStoreMultipleMessages() {
        reader.parseAndStore("Patient ID: 1, Timestamp: 1000, Label: HeartRate, Data: 70.0", storage);
        reader.parseAndStore("Patient ID: 1, Timestamp: 2000, Label: HeartRate, Data: 75.0", storage);
        reader.parseAndStore("Patient ID: 2, Timestamp: 1000, Label: HeartRate, Data: 80.0", storage);

        assertEquals(2, storage.getRecords(1, 0, Long.MAX_VALUE).size());
        assertEquals(1, storage.getRecords(2, 0, Long.MAX_VALUE).size());
    }

    @Test
    void testReadDataThrowsIOExceptionForInvalidUri() {
        WebSocketDataReader badReader = new WebSocketDataReader("ws://invalid host:8080");
        assertThrows(IOException.class, () -> badReader.readData(storage));
    }
}
