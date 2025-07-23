package com.company.MultiModule.Repository;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class CsvStorage {

    public static void save(String filePath, List<String> lines) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());

        // Atomic save
        Path tempFile = Files.createTempFile(path.getParent(), "temp-", ".csv");
        Files.write(tempFile, lines);
        Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public static List<String> load(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) return Collections.emptyList();
        return Files.readAllLines(path);
    }
}
