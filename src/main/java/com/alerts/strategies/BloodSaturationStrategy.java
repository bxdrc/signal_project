package com.alerts.strategies;

import com.alerts.Alert;
import com.alerts.factory.BloodSaturationAlertFactory;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Checks blood oxygen saturation conditions: low saturation, rapid drop,
 * and combined hypotensive hypoxemia.
 */
public class BloodSaturationStrategy implements AlertStrategy {
    private final BloodSaturationAlertFactory factory = new BloodSaturationAlertFactory();

    @Override
    public List<Alert> checkAlert(int patientId, DataStorage dataStorage) {
        List<Alert> alerts = new ArrayList<>();
        List<PatientRecord> all = dataStorage.getRecords(patientId, 0, Long.MAX_VALUE);
        List<PatientRecord> saturation = filterByType(all, "Saturation");
        List<PatientRecord> systolic = filterByType(all, "SystolicPressure");

        checkLowSaturation(patientId, saturation, alerts);
        checkRapidDrop(patientId, saturation, alerts);
        checkHypotensiveHypoxemia(patientId, systolic, saturation, alerts);
        return alerts;
    }

    private void checkLowSaturation(int patientId, List<PatientRecord> records, List<Alert> alerts) {
        for (PatientRecord r : records) {
            if (r.getMeasurementValue() < 92) {
                alerts.add(factory.createAlert(String.valueOf(patientId),
                        "Low Blood Saturation Alert", r.getTimestamp()));
            }
        }
    }

    private void checkRapidDrop(int patientId, List<PatientRecord> records, List<Alert> alerts) {
        long windowMs = 600000L;
        for (int i = 0; i < records.size(); i++) {
            for (int j = i + 1; j < records.size(); j++) {
                PatientRecord r1 = records.get(i);
                PatientRecord r2 = records.get(j);
                if (r2.getTimestamp() - r1.getTimestamp() <= windowMs) {
                    if (r1.getMeasurementValue() - r2.getMeasurementValue() >= 5) {
                        alerts.add(factory.createAlert(String.valueOf(patientId),
                                "Rapid Blood Saturation Drop Alert", r2.getTimestamp()));
                        return;
                    }
                }
            }
        }
    }

    private void checkHypotensiveHypoxemia(int patientId, List<PatientRecord> systolic,
                                            List<PatientRecord> saturation, List<Alert> alerts) {
        boolean lowSystolic = systolic.stream().anyMatch(r -> r.getMeasurementValue() < 90);
        boolean lowSaturation = saturation.stream().anyMatch(r -> r.getMeasurementValue() < 92);
        if (lowSystolic && lowSaturation) {
            alerts.add(factory.createAlert(String.valueOf(patientId),
                    "Hypotensive Hypoxemia Alert", System.currentTimeMillis()));
        }
    }

    private List<PatientRecord> filterByType(List<PatientRecord> records, String type) {
        return records.stream()
                .filter(r -> r.getRecordType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }
}
