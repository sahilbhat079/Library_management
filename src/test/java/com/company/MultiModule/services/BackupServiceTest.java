package com.company.MultiModule.services;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BackupServiceTest {

    private static final String BACKUP_DIR = "data/backup";
    private static final String USER_CSV_FILE = "data/users.csv";
    private static final String BOOK_CSV_FILE = "data/books.csv";
    private static final String BORROW_CSV_FILE = "data/borrow.csv";

    private final BackupService backupService = BackupService.getInstance();

    @BeforeAll
    void setup() throws Exception {
        Files.createDirectories(Paths.get("data"));
        Files.writeString(Paths.get(USER_CSV_FILE), "id,name,email,password\n1,John,john@example.com,1234");
        Files.writeString(Paths.get(BOOK_CSV_FILE), "id,isbn,title,author,category,available\n1,ISBN001,Test,Author,Fiction,true");
        Files.writeString(Paths.get(BORROW_CSV_FILE), "userId,bookId,timestamp\n1,1,2025-07-23T12:00:00");
    }

    @Test
    void testManualBackupCreatesFiles() throws Exception {
        backupService.backupNow();

        assertTrue(backupFileExists("users_"), "User backup file should be created.");
        assertTrue(backupFileExists("books_"), "Book backup file should be created.");
        assertTrue(backupFileExists("borrow_"), "Borrow backup file should be created.");
    }

    private boolean backupFileExists(String prefix) throws IOException {
        try (Stream<Path> paths = Files.list(Paths.get(BACKUP_DIR))) {
            return paths.anyMatch(p -> p.getFileName().toString().startsWith(prefix));
        }
    }

    @AfterAll
    void cleanup() throws Exception {
        Files.deleteIfExists(Paths.get(USER_CSV_FILE));
        Files.deleteIfExists(Paths.get(BOOK_CSV_FILE));
        Files.deleteIfExists(Paths.get(BORROW_CSV_FILE));

        if (Files.exists(Paths.get(BACKUP_DIR))) {
            try (Stream<Path> files = Files.list(Paths.get(BACKUP_DIR))) {
                files.forEach(path -> {
                    try { Files.deleteIfExists(path); } catch (Exception ignored) {}
                });
            }
            Files.deleteIfExists(Paths.get(BACKUP_DIR));
        }
    }
}
