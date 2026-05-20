package com.alerts.factory;

import com.alerts.Alert;
import com.alerts.ECGAlert;

/** Factory that creates {@link ECGAlert} instances. */
public class ECGAlertFactory extends AlertFactory {
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new ECGAlert(patientId, condition, timestamp);
    }
}
