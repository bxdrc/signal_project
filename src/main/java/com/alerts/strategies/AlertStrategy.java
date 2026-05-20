package com.alerts.strategies;

import com.alerts.Alert;
import com.data_management.DataStorage;

import java.util.List;

/**
 * Strategy interface for evaluating a specific category of health alert conditions.
 */
public interface AlertStrategy {
    List<Alert> checkAlert(int patientId, DataStorage dataStorage);
}
