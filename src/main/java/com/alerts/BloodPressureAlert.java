package com.alerts;

/** Alert type for blood pressure conditions. */
public class BloodPressureAlert extends BasicAlert {
    public BloodPressureAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }
}
