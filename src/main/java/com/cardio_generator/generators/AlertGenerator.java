package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated alert events for patients in the cardiovascular monitoring system.
 *
 * <p>Each patient has a boolean alert state (pressed or resolved). On every call to
 * {@link #generate}, the generator either resolves an active alert with 90% probability
 * or triggers a new one using a Poisson-derived probability based on {@code lambda = 0.1}.
 */
public class AlertGenerator implements PatientDataGenerator {

    // Renamed from randomGenerator to RANDOM_GENERATOR: static final fields must use
    // UPPER_SNAKE_CASE per Google Java Style Guide §5.2.4
    public static final Random RANDOM_GENERATOR = new Random();

    // Renamed from AlertStates to alertStates: instance fields must use camelCase
    // per Google Java Style Guide §5.2.7
    private boolean[] alertStates; // false = resolved, true = pressed

    /**
     * Constructs an {@code AlertGenerator} for the given number of patients.
     *
     * @param patientCount the number of patients to track; indices 1..patientCount are used
     */
    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }

    /**
     * Generates one alert simulation step for the specified patient.
     *
     * <p>If the patient currently has an active alert, there is a 90% chance it will be
     * resolved and an {@code "Alert"/"resolved"} record output. Otherwise, a new alert
     * is triggered with probability {@code 1 - e^(-lambda)} and an
     * {@code "Alert"/"triggered"} record is output.
     *
     * @param patientId      the ID of the patient to evaluate
     * @param outputStrategy the strategy used to emit the alert record
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (RANDOM_GENERATOR.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                // Renamed from Lambda to lambda: local variables must use camelCase
                // per Google Java Style Guide §5.2.7
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = RANDOM_GENERATOR.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
