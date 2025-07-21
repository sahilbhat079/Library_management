package com.company.MultiModule.Repository;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CsvStorage {

    public static void save(String filePath, List<String> lines) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        Files.write(path, lines);
    }

    public static List<String> load(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) return Collections.emptyList();
        return Files.readAllLines(path);
    }
}
