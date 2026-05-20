package com.alerts;

/** Alert type for blood oxygen saturation conditions. */
public class BloodSaturationAlert extends BasicAlert {
    public BloodSaturationAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }
}
