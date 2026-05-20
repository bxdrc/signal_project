package com.cardio_generator;

import com.data_management.DataStorage;

/**
 * Entry point dispatcher for the CHMS application.
 * Selects which component to run based on the first command-line argument.
 *
 * <p>Usage:
 * <pre>
 *   java -jar app.jar DataStorage    → runs DataStorage main
 *   java -jar app.jar               → runs HealthDataSimulator main (default)
 * </pre>
 */
public class Main {

    /**
     * Dispatches execution to either {@link DataStorage} or {@link HealthDataSimulator}
     * depending on the first command-line argument.
     *
     * @param args command-line arguments; if args[0] equals "DataStorage",
     *             runs DataStorage, otherwise runs HealthDataSimulator
     * @throws Exception if the selected main method throws an exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("DataStorage")) {
            DataStorage.main(new String[]{});
        } else {
            HealthDataSimulator.main(args);
        }
    }
}
