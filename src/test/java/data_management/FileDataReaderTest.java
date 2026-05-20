package data_management;

import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FileDataReader}.
 * Uses a temporary directory to create mock data files.
 */
class FileDataReaderTest {

    private DataStorage storage;

    @BeforeEach
    void setUp() {
        storage = new DataStorage();
    }

    @Test
    void testReadsValidFileCorrectly(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("Saturation.txt");
        Files.writeString(file,
                "Patient ID: 1, Timestamp: 1000, Label: Saturation, Data: 98.0%\n" +
                "Patient ID: 1, Timestamp: 2000, Label: Saturation, Data: 97.0%\n");

        FileDataReader reader = new FileDataReader(tempDir.toString());
        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0, Long.MAX_VALUE);
        assertEquals(2, records.size());
        assertEquals(98.0, records.get(0).getMeasurementValue());
        assertEquals(97.0, records.get(1).getMeasurementValue());
    }

    @Test
    void testSkipsMalformedLines(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("ECG.txt");
        Files.writeString(file,
                "Patient ID: 1, Timestamp: 1000, Label: ECG, Data: 0.5\n" +
                "THIS IS A MALFORMED LINE\n" +
                "Patient ID: 1, Timestamp: 2000, Label: ECG, Data: 0.6\n");

        FileDataReader reader = new FileDataReader(tempDir.toString());
        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0, Long.MAX_VALUE);
        assertEquals(2, records.size());
    }

    @Test
    void testThrowsIOExceptionForMissingDirectory() {
        FileDataReader reader = new FileDataReader("/nonexistent/path/12345");
        assertThrows(IOException.class, () -> reader.readData(storage));
    }

    @Test
    void testEmptyDirectoryProducesNoRecords(@TempDir Path tempDir) throws IOException {
        FileDataReader reader = new FileDataReader(tempDir.toString());
        reader.readData(storage);
        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testMultipleFilesReadCorrectly(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("SystolicPressure.txt"),
                "Patient ID: 1, Timestamp: 1000, Label: SystolicPressure, Data: 120.0\n");
        Files.writeString(tempDir.resolve("DiastolicPressure.txt"),
                "Patient ID: 1, Timestamp: 1000, Label: DiastolicPressure, Data: 80.0\n");

        FileDataReader reader = new FileDataReader(tempDir.toString());
        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0, Long.MAX_VALUE);
        assertEquals(2, records.size());
    }

    @Test
    void testMultiplePatientsReadCorrectly(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("HeartRate.txt"),
                "Patient ID: 1, Timestamp: 1000, Label: HeartRate, Data: 72.0\n" +
                "Patient ID: 2, Timestamp: 1000, Label: HeartRate, Data: 80.0\n");

        FileDataReader reader = new FileDataReader(tempDir.toString());
        reader.readData(storage);

        assertEquals(1, storage.getRecords(1, 0, Long.MAX_VALUE).size());
        assertEquals(1, storage.getRecords(2, 0, Long.MAX_VALUE).size());
    }
}
