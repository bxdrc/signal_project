package com.cardio_generator.outputs;

/**
 * Defines the strategy interface for outputting patient data.
 * Implementations of this interface determine how and where
 * the generated patient data is sent, such as to the console,
 * a file, a WebSocket, or a TCP socket.
 */
public interface OutputStrategy {

    /**
     * Outputs the generated data for a specific patient.
     *
     * @param patientId the unique identifier of the patient
     * @param timestamp the time at which the data was generated, in milliseconds since epoch
     * @param label     the type of health data being output (e.g., "ECG", "BloodPressure")
     * @param data      the actual data value to be output as a string
     */
    void output(int patientId, long timestamp, String label, String data);
}