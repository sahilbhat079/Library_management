package com.company.MultiModule.services;

import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BackupServiceTest {

    private static final String BACKUP_DIR = "data/backup";
    private static final String USER_CSV_FILE = "data/users.csv";
    private static final String BOOK_CSV_FILE = "data/books.csv";

    private final BackupService backupService = BackupService.getInstance();

    @BeforeAll
    void setup() throws Exception {
        // Ensure dummy CSVs exist for test
        Files.createDirectories(Paths.get("data"));
        Files.writeString(Paths.get(USER_CSV_FILE), "id,name,email,password\n1,John,john@example.com,1234");
        Files.writeString(Paths.get(BOOK_CSV_FILE), "id,isbn,title,author,category,available\n1,ISBN001,Test,Author,Fiction,true");
    }

    @Test
    void testManualBackupCreatesFiles() throws Exception {
        backupService.backupNow();

        try (Stream<Path> paths = Files.list(Paths.get(BACKUP_DIR))) {
            boolean userBackupExists = paths.anyMatch(p -> p.getFileName().toString().startsWith("users_"));
            assertTrue(userBackupExists, "User backup file should be created.");
        }

        try (Stream<Path> paths = Files.list(Paths.get(BACKUP_DIR))) {
            boolean bookBackupExists = paths.anyMatch(p -> p.getFileName().toString().startsWith("books_"));
            assertTrue(bookBackupExists, "Book backup file should be created.");
        }
    }

    @AfterAll
    void cleanup() throws Exception {
        // Delete test files
        Files.deleteIfExists(Paths.get(USER_CSV_FILE));
        Files.deleteIfExists(Paths.get(BOOK_CSV_FILE));

        // Clean up backup directory
        if (Files.exists(Paths.get(BACKUP_DIR))) {
            try (Stream<Path> files = Files.list(Paths.get(BACKUP_DIR))) {
                files.forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception ignored) {}
                });
            }
            Files.deleteIfExists(Paths.get(BACKUP_DIR));
        }
    }
}
