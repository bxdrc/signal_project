package com.data_management;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import com.alerts.AlertGenerator;

/**
 * Manages storage and retrieval of patient data within a healthcare monitoring
 * system. This class serves as a repository for all patient records, organized
 * by patient IDs.
 *
 * <p>Implemented as a Singleton to ensure a single shared data store across
 * the application. Uses a {@link ConcurrentHashMap} for thread-safe access
 * from concurrent WebSocket message handlers.
 */
public class DataStorage {
    private static DataStorage instance;
    private final ConcurrentHashMap<Integer, Patient> patientMap;

    private DataStorage() {
        this.patientMap = new ConcurrentHashMap<>();
    }

    /**
     * Returns the single shared instance of {@code DataStorage}, creating it
     * on the first call.
     *
     * @return the singleton instance
     */
    public static synchronized DataStorage getInstance() {
        if (instance == null) {
            instance = new DataStorage();
        }
        return instance;
    }

    /**
     * Resets the singleton instance. Intended for use in unit tests only.
     */
    public static synchronized void resetInstance() {
        instance = null;
    }

    /**
     * Adds or updates patient data in the storage. Thread-safe: uses
     * {@link ConcurrentHashMap#computeIfAbsent} to create new patients atomically.
     *
     * @param patientId        the unique identifier of the patient
     * @param measurementValue the value of the health metric being recorded
     * @param recordType       the type of record, e.g., "HeartRate"
     * @param timestamp        milliseconds since the Unix epoch
     */
    public void addPatientData(int patientId, double measurementValue, String recordType, long timestamp) {
        patientMap.computeIfAbsent(patientId, Patient::new)
                  .addRecord(measurementValue, recordType, timestamp);
    }

    /**
     * Retrieves a list of PatientRecord objects for a specific patient, filtered
     * by a time range.
     *
     * @param patientId the unique identifier of the patient
     * @param startTime start of the time range (inclusive), in milliseconds
     * @param endTime   end of the time range (inclusive), in milliseconds
     * @return a list of matching records, or an empty list if the patient is unknown
     */
    public List<PatientRecord> getRecords(int patientId, long startTime, long endTime) {
        Patient patient = patientMap.get(patientId);
        if (patient != null) {
            return patient.getRecords(startTime, endTime);
        }
        return new ArrayList<>();
    }

    /**
     * Retrieves a collection of all patients stored in the data storage.
     *
     * @return a list of all patients
     */
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patientMap.values());
    }

    /**
     * The main method for the DataStorage class.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        DataStorage storage = getInstance();

        List<PatientRecord> records = storage.getRecords(1, 1700000000000L, 1800000000000L);
        for (PatientRecord record : records) {
            System.out.println("Record for Patient ID: " + record.getPatientId()
                    + ", Type: " + record.getRecordType()
                    + ", Data: " + record.getMeasurementValue()
                    + ", Timestamp: " + record.getTimestamp());
        }

        AlertGenerator alertGenerator = new AlertGenerator(storage);
        for (Patient patient : storage.getAllPatients()) {
            alertGenerator.evaluateData(patient);
        }
    }
}
