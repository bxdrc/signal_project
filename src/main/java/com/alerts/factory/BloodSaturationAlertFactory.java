package com.alerts.factory;

import com.alerts.Alert;
import com.alerts.BloodSaturationAlert;

/** Factory that creates {@link BloodSaturationAlert} instances. */
public class BloodSaturationAlertFactory extends AlertFactory {
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BloodSaturationAlert(patientId, condition, timestamp);
    }
}
