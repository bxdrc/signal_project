package com.alerts;

/** Alert type for ECG and heart-rate related conditions. */
public class ECGAlert extends BasicAlert {
    public ECGAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }
}
