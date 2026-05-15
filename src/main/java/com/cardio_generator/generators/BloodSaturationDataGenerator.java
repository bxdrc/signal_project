package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated blood oxygen saturation data for patients.
 * Each patient is initialized with a baseline saturation value between 95% and 100%,
 * and subsequent values fluctuate slightly to simulate realistic physiological variation.
 * Values are kept within a healthy range of 90% to 100%.
 */
public class BloodSaturationDataGenerator implements PatientDataGenerator {

    private static final Random random = new Random();
    private int[] lastSaturationValues;

    /**
     * Constructs a {@code BloodSaturationDataGenerator} for the given number of patients.
     * Each patient is assigned an initial saturation value between 95% and 100%.
     *
     * @param patientCount the number of patients to generate saturation data for
     */
    public BloodSaturationDataGenerator(int patientCount) {
        lastSaturationValues = new int[patientCount + 1];

        // Initialize with baseline saturation values for each patient
        for (int i = 1; i <= patientCount; i++) {
            lastSaturationValues[i] = 95 + random.nextInt(6); // Initializes with a value between 95 and 100
        }
    }

    /**
     * Generates and outputs a simulated blood saturation reading for the specified patient.
     * The value fluctuates by -1, 0, or 1 from the previous reading and is clamped
     * between 90% and 100% to remain within a realistic range.
     *
     * @param patientId      the unique identifier of the patient
     * @param outputStrategy the strategy used to output the generated saturation data
     * @throws Exception if an error occurs during data generation or output
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            // Simulate blood saturation values
            int variation = random.nextInt(3) - 1; // -1, 0, or 1 to simulate small fluctuations
            int newSaturationValue = lastSaturationValues[patientId] + variation;

            // Ensure the saturation stays within a realistic and healthy range
            newSaturationValue = Math.min(Math.max(newSaturationValue, 90), 100);
            lastSaturationValues[patientId] = newSaturationValue;
            outputStrategy.output(patientId, System.currentTimeMillis(), "Saturation",
                    Double.toString(newSaturationValue) + "%");
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood saturation data for patient " + patientId);
            e.printStackTrace();
        }
    }
}