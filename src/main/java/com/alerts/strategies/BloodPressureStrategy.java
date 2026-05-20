package com.alerts.strategies;

import com.alerts.Alert;
import com.alerts.factory.BloodPressureAlertFactory;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Checks blood pressure trend and critical threshold conditions.
 */
public class BloodPressureStrategy implements AlertStrategy {
    private final BloodPressureAlertFactory factory = new BloodPressureAlertFactory();

    @Override
    public List<Alert> checkAlert(int patientId, DataStorage dataStorage) {
        List<Alert> alerts = new ArrayList<>();
        List<PatientRecord> all = dataStorage.getRecords(patientId, 0, Long.MAX_VALUE);
        List<PatientRecord> systolic = filterByType(all, "SystolicPressure");
        List<PatientRecord> diastolic = filterByType(all, "DiastolicPressure");

        checkTrend(patientId, systolic, "Systolic", alerts);
        checkTrend(patientId, diastolic, "Diastolic", alerts);
        checkCritical(patientId, systolic, diastolic, alerts);
        return alerts;
    }

    private void checkTrend(int patientId, List<PatientRecord> records, String type, List<Alert> alerts) {
        if (records.size() < 3) return;
        PatientRecord r1 = records.get(records.size() - 3);
        PatientRecord r2 = records.get(records.size() - 2);
        PatientRecord r3 = records.get(records.size() - 1);
        double diff1 = r2.getMeasurementValue() - r1.getMeasurementValue();
        double diff2 = r3.getMeasurementValue() - r2.getMeasurementValue();
        if (diff1 > 10 && diff2 > 10) {
            alerts.add(factory.createAlert(String.valueOf(patientId),
                    type + " Blood Pressure Increasing Trend Alert", r3.getTimestamp()));
        } else if (diff1 < -10 && diff2 < -10) {
            alerts.add(factory.createAlert(String.valueOf(patientId),
                    type + " Blood Pressure Decreasing Trend Alert", r3.getTimestamp()));
        }
    }

    private void checkCritical(int patientId, List<PatientRecord> systolic,
                                List<PatientRecord> diastolic, List<Alert> alerts) {
        for (PatientRecord r : systolic) {
            if (r.getMeasurementValue() > 180) {
                alerts.add(factory.createAlert(String.valueOf(patientId),
                        "Critical High Systolic Blood Pressure Alert", r.getTimestamp()));
            } else if (r.getMeasurementValue() < 90) {
                alerts.add(factory.createAlert(String.valueOf(patientId),
                        "Critical Low Systolic Blood Pressure Alert", r.getTimestamp()));
            }
        }
        for (PatientRecord r : diastolic) {
            if (r.getMeasurementValue() > 120) {
                alerts.add(factory.createAlert(String.valueOf(patientId),
                        "Critical High Diastolic Blood Pressure Alert", r.getTimestamp()));
            } else if (r.getMeasurementValue() < 60) {
                alerts.add(factory.createAlert(String.valueOf(patientId),
                        "Critical Low Diastolic Blood Pressure Alert", r.getTimestamp()));
            }
        }
    }

    private List<PatientRecord> filterByType(List<PatientRecord> records, String type) {
        return records.stream()
                .filter(r -> r.getRecordType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }
}
