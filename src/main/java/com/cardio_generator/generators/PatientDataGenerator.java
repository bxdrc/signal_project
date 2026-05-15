package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Defines the interface for generating simulated patient health data.
 * Each implementation is responsible for generating a specific type
 * of health data (e.g., ECG, blood pressure, blood saturation) and
 * sending it to the provided output strategy.
 */
public interface PatientDataGenerator {

    /**
     * Generates simulated health data for a specific patient and sends
     * it to the specified output strategy.
     *
     * @param patientId      the unique identifier of the patient for whom data is generated
     * @param outputStrategy the strategy used to output the generated data
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}