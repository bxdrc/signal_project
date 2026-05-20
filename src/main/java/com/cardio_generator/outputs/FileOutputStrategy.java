package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Writes patient data records to label-specific text files under a base directory.
 *
 * <p>Each distinct label (e.g., "HeartRate", "Alert") maps to its own {@code .txt} file
 * inside {@code baseDirectory}. Files are created on first write and appended to on
 * subsequent writes. The label-to-path mapping is cached in a {@link ConcurrentHashMap}
 * for thread-safe, lock-free lookups.
 *
 * <p>Renamed from {@code fileOutputStrategy} to {@code FileOutputStrategy} to follow
 * PascalCase class naming per Google Java Style Guide §5.2.1.
 */
public class FileOutputStrategy implements OutputStrategy {

    /**
     * The root directory under which per-label output files are created.
     * Renamed from {@code BaseDirectory} to {@code baseDirectory} per Google Java Style Guide §5.2.7.
     */
    private String baseDirectory;

    /**
     * Cache mapping label names to their resolved file paths.
     * Renamed from {@code file_map} to {@code fileMap} per Google Java Style Guide §5.2.7.
     */
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    /**
     * Constructs a {@code FileOutputStrategy} that writes files into the given directory.
     *
     * @param baseDirectory path to the directory where output files will be written;
     *                      created automatically if it does not exist
     */
    public FileOutputStrategy(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     * Appends a formatted patient data record to the file corresponding to {@code label}.
     *
     * <p>The output line format is:
     * {@code Patient ID: <id>, Timestamp: <ts>, Label: <label>, Data: <data>}
     *
     * @param patientId the unique identifier of the patient
     * @param timestamp the time of the measurement in milliseconds since the Unix epoch
     * @param label     the measurement type (e.g., "HeartRate"); determines the output filename
     * @param data      the measurement value as a string
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Renamed from FilePath to filePath to follow camelCase naming convention
        // for local variables per Google Java Style Guide §5.2.7
        String filePath = fileMap.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}
