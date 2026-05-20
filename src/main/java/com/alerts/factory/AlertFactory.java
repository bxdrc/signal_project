package com.alerts.factory;

import com.alerts.Alert;

/**
 * Abstract factory for creating {@link Alert} objects.
 * Subclasses implement the factory method to produce specific alert types.
 */
public abstract class AlertFactory {
    public abstract Alert createAlert(String patientId, String condition, long timestamp);
}
