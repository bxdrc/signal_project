package data_management.alerts;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AlertGenerator}.
 * Tests all alert conditions: blood pressure trend, critical thresholds,
 * blood saturation, hypotensive hypoxemia, ECG peaks, and triggered alerts.
 */
class AlertGeneratorTest {

    private DataStorage storage;
    private AlertGenerator generator;
    private ByteArrayOutputStream outContent;

    /**
     * Sets up a fresh DataStorage and AlertGenerator before each test,
     * and captures console output so we can verify alerts were triggered.
     */
    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
        generator = new AlertGenerator(storage);
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    // ── Blood Pressure Trend Tests ────────────────────────────────────────────

    @Test
    void testSystolicIncreasingTrendTriggersAlert() {
        storage.addPatientData(1, 110, "SystolicPressure", 1000L);
        storage.addPatientData(1, 125, "SystolicPressure", 2000L);
        storage.addPatientData(1, 140, "SystolicPressure", 3000L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Systolic Blood Pressure Increasing Trend Alert"));
    }

    @Test
    void testSystolicDecreasingTrendTriggersAlert() {
        storage.addPatientData(1, 150, "SystolicPressure", 1000L);
        storage.addPatientData(1, 135, "SystolicPressure", 2000L);
        storage.addPatientData(1, 120, "SystolicPressure", 3000L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Systolic Blood Pressure Decreasing Trend Alert"));
    }

    @Test
    void testDiastolicIncreasingTrendTriggersAlert() {
        storage.addPatientData(1, 70, "DiastolicPressure", 1000L);
        storage.addPatientData(1, 85, "DiastolicPressure", 2000L);
        storage.addPatientData(1, 100, "DiastolicPressure", 3000L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Diastolic Blood Pressure Increasing Trend Alert"));
    }

    @Test
    void testNoTrendAlertWhenChangeIsSmall() {
        storage.addPatientData(1, 110, "SystolicPressure", 1000L);
        storage.addPatientData(1, 115, "SystolicPressure", 2000L);
        storage.addPatientData(1, 120, "SystolicPressure", 3000L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertFalse(outContent.toString().contains("Trend Alert"));
    }

    // ── Blood Pressure Critical Threshold Tests ───────────────────────────────

    @Test
    void testHighSystolicCriticalAlert() {
        storage.addPatientData(1, 185, "SystolicPressure", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Critical High Systolic Blood Pressure Alert"));
    }

    @Test
    void testLowSystolicCriticalAlert() {
        storage.addPatientData(1, 85, "SystolicPressure", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Critical Low Systolic Blood Pressure Alert"));
    }

    @Test
    void testHighDiastolicCriticalAlert() {
        storage.addPatientData(1, 125, "DiastolicPressure", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Critical High Diastolic Blood Pressure Alert"));
    }

    @Test
    void testLowDiastolicCriticalAlert() {
        storage.addPatientData(1, 55, "DiastolicPressure", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Critical Low Diastolic Blood Pressure Alert"));
    }

    @Test
    void testNormalBloodPressureNoAlert() {
        storage.addPatientData(1, 120, "SystolicPressure", 1000L);
        storage.addPatientData(1, 80, "DiastolicPressure", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertFalse(outContent.toString().contains("Blood Pressure Alert"));
    }

    // ── Blood Saturation Tests ────────────────────────────────────────────────

    @Test
    void testLowSaturationAlert() {
        storage.addPatientData(1, 91, "Saturation", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Low Blood Saturation Alert"));
    }

    @Test
    void testSaturationAt92DoesNotAlert() {
        storage.addPatientData(1, 92, "Saturation", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertFalse(outContent.toString().contains("Low Blood Saturation Alert"));
    }

    @Test
    void testRapidSaturationDropAlert() {
        long now = System.currentTimeMillis();
        storage.addPatientData(1, 98, "Saturation", now);
        storage.addPatientData(1, 93, "Saturation", now + 300000L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Rapid Blood Saturation Drop Alert"));
    }

    @Test
    void testRapidDropOutsideWindowDoesNotAlert() {
        long now = System.currentTimeMillis();
        storage.addPatientData(1, 98, "Saturation", now);
        storage.addPatientData(1, 93, "Saturation", now + 700000L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertFalse(outContent.toString().contains("Rapid Blood Saturation Drop Alert"));
    }

    // ── Hypotensive Hypoxemia Tests ───────────────────────────────────────────

    @Test
    void testHypotensiveHypoxemiaAlert() {
        storage.addPatientData(1, 85, "SystolicPressure", 1000L);
        storage.addPatientData(1, 90, "Saturation", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Hypotensive Hypoxemia Alert"));
    }

    @Test
    void testHypotensiveHypoxemiaNotTriggeredIfOnlyOneLow() {
        storage.addPatientData(1, 85, "SystolicPressure", 1000L);
        storage.addPatientData(1, 95, "Saturation", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertFalse(outContent.toString().contains("Hypotensive Hypoxemia Alert"));
    }

    // ── ECG Tests ─────────────────────────────────────────────────────────────

    @Test
    void testECGAbnormalPeakAlert() {
        for (int i = 0; i < 10; i++) {
            storage.addPatientData(1, 1.0, "ECG", (long) i * 100);
        }
        storage.addPatientData(1, 10.0, "ECG", 1100L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Abnormal ECG Peak Alert"));
    }

    @Test
    void testECGNormalReadingsNoAlert() {
        for (int i = 0; i < 15; i++) {
            storage.addPatientData(1, 1.0, "ECG", (long) i * 100);
        }

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertFalse(outContent.toString().contains("Abnormal ECG Peak Alert"));
    }

    @Test
    void testECGNotEnoughDataNoAlert() {
        storage.addPatientData(1, 5.0, "ECG", 1000L);
        storage.addPatientData(1, 5.0, "ECG", 2000L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertFalse(outContent.toString().contains("Abnormal ECG Peak Alert"));
    }

    // ── Triggered Alert Tests ─────────────────────────────────────────────────

    @Test
    void testManualAlertTriggered() {
        storage.addPatientData(1, 1.0, "Alert", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Manual Alert Triggered"));
    }

    @Test
    void testManualAlertUntriggeredNoAlert() {
        storage.addPatientData(1, 0.0, "Alert", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        generator.evaluateData(patient);

        assertFalse(outContent.toString().contains("Manual Alert Triggered"));
    }
}
