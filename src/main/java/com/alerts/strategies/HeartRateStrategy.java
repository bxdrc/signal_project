package com.alerts.strategies;

import com.alerts.Alert;
import com.alerts.factory.ECGAlertFactory;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Checks ECG abnormal peak conditions and manually triggered alerts.
 */
public class HeartRateStrategy implements AlertStrategy {
    private final ECGAlertFactory factory = new ECGAlertFactory();

    @Override
    public List<Alert> checkAlert(int patientId, DataStorage dataStorage) {
        List<Alert> alerts = new ArrayList<>();
        List<PatientRecord> all = dataStorage.getRecords(patientId, 0, Long.MAX_VALUE);
        List<PatientRecord> ecg = filterByType(all, "ECG");
        List<PatientRecord> triggered = filterByType(all, "Alert");

        checkECGPeak(patientId, ecg, alerts);
        checkManualAlert(patientId, triggered, alerts);
        return alerts;
    }

    private void checkECGPeak(int patientId, List<PatientRecord> records, List<Alert> alerts) {
        int windowSize = 10;
        if (records.size() <= windowSize) return;
        for (int i = windowSize; i < records.size(); i++) {
            double sum = 0;
            for (int j = i - windowSize; j < i; j++) {
                sum += records.get(j).getMeasurementValue();
            }
            double avg = sum / windowSize;
            if (avg > 0 && records.get(i).getMeasurementValue() > 2.0 * avg) {
                alerts.add(factory.createAlert(String.valueOf(patientId),
                        "Abnormal ECG Peak Alert", records.get(i).getTimestamp()));
            }
        }
    }

    private void checkManualAlert(int patientId, List<PatientRecord> records, List<Alert> alerts) {
        for (PatientRecord r : records) {
            if (r.getMeasurementValue() == 1.0) {
                alerts.add(factory.createAlert(String.valueOf(patientId),
                        "Manual Alert Triggered", r.getTimestamp()));
            }
        }
    }

    private List<PatientRecord> filterByType(List<PatientRecord> records, String type) {
        return records.stream()
                .filter(r -> r.getRecordType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }
}
