package com.alerts;

import com.alerts.strategies.AlertStrategy;
import com.alerts.strategies.BloodPressureStrategy;
import com.alerts.strategies.BloodSaturationStrategy;
import com.alerts.strategies.HeartRateStrategy;
import com.data_management.DataStorage;
import com.data_management.Patient;

import java.util.Arrays;
import java.util.List;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 *
 * <p>Uses the Strategy pattern: each {@link AlertStrategy} implementation
 * encapsulates one category of health-check logic.
 */
public class AlertGenerator {
    private final DataStorage dataStorage;
    private final List<AlertStrategy> strategies;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     *
     * @param dataStorage the data storage system that provides access to patient data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        this.strategies = Arrays.asList(
                new BloodPressureStrategy(),
                new BloodSaturationStrategy(),
                new HeartRateStrategy()
        );
    }

    /**
     * Evaluates the specified patient's data against all registered alert strategies.
     * Triggers an alert for every condition that is met.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        int patientId = patient.getPatientId();
        for (AlertStrategy strategy : strategies) {
            for (Alert alert : strategy.checkAlert(patientId, dataStorage)) {
                triggerAlert(alert);
            }
        }
    }

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        System.out.println("[ALERT] Patient " + alert.getPatientId()
                + " | Condition: " + alert.getCondition()
                + " | Time: " + alert.getTimestamp());
    }
}
