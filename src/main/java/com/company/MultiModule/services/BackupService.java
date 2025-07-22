package com.company.MultiModule.services;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

public class BackupService {
    private static final String USER_CSV_FILE = "data/users.csv";
    private static final String BOOK_CSV_FILE = "data/books.csv";
    private static final String BACKUP_DIR = "data/backup";
    private static final long BACKUP_INTERVAL_MINUTES = 5;

    //auto colasable
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static BackupService instance;

    private BackupService() {}

    public static BackupService getInstance() {
        if (instance == null) {
            instance = new BackupService();
        }
        return instance;
    }

    public void startPeriodicBackup() {
        scheduler.scheduleAtFixedRate(this::backupFiles, 0, BACKUP_INTERVAL_MINUTES, TimeUnit.MINUTES);
        System.out.println(" Periodic backup started (every " + BACKUP_INTERVAL_MINUTES + " min).");
    }

    private void backupFiles() {
        try {
            Files.createDirectories(Paths.get(BACKUP_DIR));

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new Date());

            copyFile(USER_CSV_FILE, BACKUP_DIR + "/users_" + timestamp + ".csv");
            copyFile(BOOK_CSV_FILE, BACKUP_DIR + "/books_" + timestamp + ".csv");

            System.out.println(" Backup completed at " + timestamp);
        } catch (IOException e) {
            System.out.println(" Backup failed: " + e.getMessage());
        }
    }

    private void copyFile(String sourcePath, String destPath) throws IOException {
        Path source = Paths.get(sourcePath);
        Path dest = Paths.get(destPath);
        if (Files.exists(source)) {
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void backupNow() {
        backupFiles(); // just reuse the existing private logic
    }


    public void shutdown() {
        scheduler.shutdown();
        System.out.println(" Backup service stopped.");
    }



}
