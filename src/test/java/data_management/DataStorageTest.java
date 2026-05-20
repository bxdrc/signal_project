package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.List;

/**
 * Unit tests for {@link DataStorage}.
 * Covers adding records, retrieving by time range, and edge cases.
 */
class DataStorageTest {

    private DataStorage storage;

    @BeforeEach
    void setUp() {
        storage = new DataStorage();
    }

    @Test
    void testAddAndGetRecords() {
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size());
        assertEquals(100.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testGetRecordsTimeRangeFiltersCorrectly() {
        storage.addPatientData(1, 100.0, "HeartRate", 1000L);
        storage.addPatientData(1, 200.0, "HeartRate", 2000L);
        storage.addPatientData(1, 300.0, "HeartRate", 3000L);

        List<PatientRecord> records = storage.getRecords(1, 1000L, 2000L);
        assertEquals(2, records.size());
    }

    @Test
    void testGetRecordsReturnsEmptyForUnknownPatient() {
        List<PatientRecord> records = storage.getRecords(999, 0L, Long.MAX_VALUE);
        assertTrue(records.isEmpty());
    }

    @Test
    void testGetRecordsReturnsEmptyWhenNoRecordsInRange() {
        storage.addPatientData(1, 100.0, "HeartRate", 5000L);

        List<PatientRecord> records = storage.getRecords(1, 1000L, 2000L);
        assertTrue(records.isEmpty());
    }

    @Test
    void testMultiplePatientsStoredIndependently() {
        storage.addPatientData(1, 100.0, "HeartRate", 1000L);
        storage.addPatientData(2, 200.0, "HeartRate", 1000L);

        assertEquals(1, storage.getRecords(1, 0, Long.MAX_VALUE).size());
        assertEquals(1, storage.getRecords(2, 0, Long.MAX_VALUE).size());
        assertEquals(100.0, storage.getRecords(1, 0, Long.MAX_VALUE).get(0).getMeasurementValue());
        assertEquals(200.0, storage.getRecords(2, 0, Long.MAX_VALUE).get(0).getMeasurementValue());
    }

    @Test
    void testGetAllPatientsReturnsAllAdded() {
        storage.addPatientData(1, 100.0, "HeartRate", 1000L);
        storage.addPatientData(2, 200.0, "HeartRate", 1000L);
        storage.addPatientData(3, 300.0, "HeartRate", 1000L);

        assertEquals(3, storage.getAllPatients().size());
    }

    @Test
    void testRecordTypeStoredCorrectly() {
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);

        PatientRecord record = storage.getRecords(1, 0, Long.MAX_VALUE).get(0);
        assertEquals("SystolicPressure", record.getRecordType());
    }

    @Test
    void testTimestampBoundaryIncluded() {
        storage.addPatientData(1, 100.0, "HeartRate", 1000L);
        storage.addPatientData(1, 200.0, "HeartRate", 2000L);

        List<PatientRecord> records = storage.getRecords(1, 1000L, 2000L);
        assertEquals(2, records.size());
    }
}
