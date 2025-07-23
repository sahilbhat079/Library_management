package com.company.MultiModule.Repository;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class CsvStorage {

    public static void save(String filePath, List<String> lines) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());

        Path tempFile = Files.createTempFile(path.getParent(), "temp-", ".csv");
        Files.write(tempFile, lines);

        int retryCount = 3;
        while (retryCount-- > 0) {
            try {
                Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING);
                return;
            } catch (IOException e) {
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }
        }

        throw new IOException("Failed to save file after multiple retries");
    }

    public static List<String> load(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) return Collections.emptyList();
        return Files.readAllLines(path);
    }
}
