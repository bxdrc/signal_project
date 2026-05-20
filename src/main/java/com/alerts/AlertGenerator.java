package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private DataStorage dataStorage;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert} method.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        int patientId = patient.getPatientId();
        List<PatientRecord> allRecords = dataStorage.getRecords(patientId, 0, Long.MAX_VALUE);

        List<PatientRecord> systolicRecords = filterByType(allRecords, "SystolicPressure");
        List<PatientRecord> diastolicRecords = filterByType(allRecords, "DiastolicPressure");
        List<PatientRecord> saturationRecords = filterByType(allRecords, "Saturation");
        List<PatientRecord> ecgRecords = filterByType(allRecords, "ECG");
        List<PatientRecord> alertRecords = filterByType(allRecords, "Alert");

        checkBloodPressureTrend(patientId, systolicRecords, "Systolic");
        checkBloodPressureTrend(patientId, diastolicRecords, "Diastolic");
        checkBloodPressureCritical(patientId, systolicRecords, diastolicRecords);
        checkLowSaturation(patientId, saturationRecords);
        checkRapidSaturationDrop(patientId, saturationRecords);
        checkHypotensiveHypoxemia(patientId, systolicRecords, saturationRecords);
        checkECGAbnormalPeak(patientId, ecgRecords);
        checkTriggeredAlert(patientId, alertRecords);
    }

    private void checkBloodPressureTrend(int patientId, List<PatientRecord> records, String type) {
        if (records.size() < 3) return;

        PatientRecord r1 = records.get(records.size() - 3);
        PatientRecord r2 = records.get(records.size() - 2);
        PatientRecord r3 = records.get(records.size() - 1);

        double diff1 = r2.getMeasurementValue() - r1.getMeasurementValue();
        double diff2 = r3.getMeasurementValue() - r2.getMeasurementValue();

        if (diff1 > 10 && diff2 > 10) {
            triggerAlert(new Alert(String.valueOf(patientId),
                    type + " Blood Pressure Increasing Trend Alert", r3.getTimestamp()));
        } else if (diff1 < -10 && diff2 < -10) {
            triggerAlert(new Alert(String.valueOf(patientId),
                    type + " Blood Pressure Decreasing Trend Alert", r3.getTimestamp()));
        }
    }

    private void checkBloodPressureCritical(int patientId, List<PatientRecord> systolicRecords,
                                             List<PatientRecord> diastolicRecords) {
        for (PatientRecord r : systolicRecords) {
            if (r.getMeasurementValue() > 180) {
                triggerAlert(new Alert(String.valueOf(patientId),
                        "Critical High Systolic Blood Pressure Alert", r.getTimestamp()));
            } else if (r.getMeasurementValue() < 90) {
                triggerAlert(new Alert(String.valueOf(patientId),
                        "Critical Low Systolic Blood Pressure Alert", r.getTimestamp()));
            }
        }
        for (PatientRecord r : diastolicRecords) {
            if (r.getMeasurementValue() > 120) {
                triggerAlert(new Alert(String.valueOf(patientId),
                        "Critical High Diastolic Blood Pressure Alert", r.getTimestamp()));
            } else if (r.getMeasurementValue() < 60) {
                triggerAlert(new Alert(String.valueOf(patientId),
                        "Critical Low Diastolic Blood Pressure Alert", r.getTimestamp()));
            }
        }
    }

    private void checkLowSaturation(int patientId, List<PatientRecord> records) {
        for (PatientRecord r : records) {
            if (r.getMeasurementValue() < 92) {
                triggerAlert(new Alert(String.valueOf(patientId),
                        "Low Blood Saturation Alert", r.getTimestamp()));
            }
        }
    }

    private void checkRapidSaturationDrop(int patientId, List<PatientRecord> records) {
        long windowMs = 600000L;
        for (int i = 0; i < records.size(); i++) {
            for (int j = i + 1; j < records.size(); j++) {
                PatientRecord r1 = records.get(i);
                PatientRecord r2 = records.get(j);
                if (r2.getTimestamp() - r1.getTimestamp() <= windowMs) {
                    double drop = r1.getMeasurementValue() - r2.getMeasurementValue();
                    if (drop >= 5) {
                        triggerAlert(new Alert(String.valueOf(patientId),
                                "Rapid Blood Saturation Drop Alert", r2.getTimestamp()));
                        return;
                    }
                }
            }
        }
    }

    private void checkHypotensiveHypoxemia(int patientId, List<PatientRecord> systolicRecords,
                                            List<PatientRecord> saturationRecords) {
        boolean lowSystolic = systolicRecords.stream()
                .anyMatch(r -> r.getMeasurementValue() < 90);
        boolean lowSaturation = saturationRecords.stream()
                .anyMatch(r -> r.getMeasurementValue() < 92);

        if (lowSystolic && lowSaturation) {
            triggerAlert(new Alert(String.valueOf(patientId),
                    "Hypotensive Hypoxemia Alert", System.currentTimeMillis()));
        }
    }

    private void checkECGAbnormalPeak(int patientId, List<PatientRecord> records) {
        int windowSize = 10;
        if (records.size() <= windowSize) return;

        for (int i = windowSize; i < records.size(); i++) {
            double sum = 0;
            for (int j = i - windowSize; j < i; j++) {
                sum += records.get(j).getMeasurementValue();
            }
            double avg = sum / windowSize;
            if (avg > 0 && records.get(i).getMeasurementValue() > 2.0 * avg) {
                triggerAlert(new Alert(String.valueOf(patientId),
                        "Abnormal ECG Peak Alert", records.get(i).getTimestamp()));
            }
        }
    }

    private void checkTriggeredAlert(int patientId, List<PatientRecord> records) {
        for (PatientRecord r : records) {
            if (r.getMeasurementValue() == 1.0) {
                triggerAlert(new Alert(String.valueOf(patientId),
                        "Manual Alert Triggered", r.getTimestamp()));
            }
        }
    }

    private List<PatientRecord> filterByType(List<PatientRecord> records, String type) {
        return records.stream()
                .filter(r -> r.getRecordType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        System.out.println("[ALERT] Patient " + alert.getPatientId()
                + " | Condition: " + alert.getCondition()
                + " | Time: " + alert.getTimestamp());
    }
}
