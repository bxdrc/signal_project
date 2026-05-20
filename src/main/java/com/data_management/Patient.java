package com.data_management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a patient and manages their medical records.
 * Thread-safe: the record list is backed by a synchronized list so that
 * concurrent WebSocket message handlers can add records safely.
 */
public class Patient {
    private int patientId;
    private List<PatientRecord> patientRecords;

    /**
     * Constructs a new Patient with a specified ID.
     * Initializes an empty, thread-safe list of patient records.
     *
     * @param patientId the unique identifier for the patient
     */
    public Patient(int patientId) {
        this.patientId = patientId;
        this.patientRecords = Collections.synchronizedList(new ArrayList<>());
    }

    public int getPatientId() {
        return patientId;
    }

    /**
     * Adds a new record to this patient's list of medical records.
     *
     * @param measurementValue the value of the health metric
     * @param recordType       the type of record, e.g., "HeartRate"
     * @param timestamp        milliseconds since the UNIX epoch
     */
    public void addRecord(double measurementValue, String recordType, long timestamp) {
        patientRecords.add(new PatientRecord(this.patientId, measurementValue, recordType, timestamp));
    }

    /**
     * Retrieves records within a specified time range.
     * Iterates under the list's intrinsic lock to prevent ConcurrentModificationException.
     *
     * @param startTime start of the time range (inclusive), in milliseconds
     * @param endTime   end of the time range (inclusive), in milliseconds
     * @return a snapshot list of matching records
     */
    public List<PatientRecord> getRecords(long startTime, long endTime) {
        List<PatientRecord> result = new ArrayList<>();
        synchronized (patientRecords) {
            for (PatientRecord record : patientRecords) {
                if (record.getTimestamp() >= startTime && record.getTimestamp() <= endTime) {
                    result.add(record);
                }
            }
        }
        return result;
    }
}
