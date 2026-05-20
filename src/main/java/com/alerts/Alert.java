package com.alerts;

/**
 * Represents an alert in the health monitoring system.
 * Used as the component interface for the Decorator pattern.
 */
public interface Alert {
    String getPatientId();
    String getCondition();
    long getTimestamp();
}
