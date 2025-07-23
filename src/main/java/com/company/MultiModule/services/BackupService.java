package com.company.MultiModule.services;

import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

public class BackupService {

    private static final String USER_CSV_FILE = "data/users.csv";
    private static final String BOOK_CSV_FILE = "data/books.csv";
    private static final String BORROW_CSV_FILE = "data/borrow.csv";
    private static final String BACKUP_DIR = "data/backup";
    private static final long BACKUP_INTERVAL_MINUTES = 5;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static volatile BackupService instance;
//    private volatile boolean started = false;

    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";

    private BackupService() {}

    // Double-checked locking singleton
    public static BackupService getInstance() {
        if (instance == null) {
            synchronized (BackupService.class) {
                if (instance == null) {
                    instance = new BackupService();
                }
            }
        }
        return instance;
    }

    /**
     * Starts automatic periodic backup at fixed intervals.
     */
    public void startPeriodicBackup() {
        scheduler.scheduleAtFixedRate(this::backupFiles, 0, BACKUP_INTERVAL_MINUTES, TimeUnit.MINUTES);
        System.out.println(CYAN + "[INFO] Periodic backup started (every " + BACKUP_INTERVAL_MINUTES + " minutes)." + RESET);
    }

    /**
     * Immediately backs up all CSV files to the backup directory.
     */
    public void backupNow() {
        backupFiles();
    }

    /**
     * Stops the scheduler and shuts down the backup thread.
     */
    public void shutdown() {
        scheduler.shutdown();
        System.out.println(CYAN + "[INFO] Backup service stopped." + RESET);
    }

    // Internal method to perform backup logic
    private void backupFiles() {
        try {
            Files.createDirectories(Paths.get(BACKUP_DIR));
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new Date());

            copyFile(USER_CSV_FILE, BACKUP_DIR + "/users_" + timestamp + ".csv");
            copyFile(BOOK_CSV_FILE, BACKUP_DIR + "/books_" + timestamp + ".csv");
            copyFile(BORROW_CSV_FILE, BACKUP_DIR + "/borrow_" + timestamp + ".csv");

            System.out.println(GREEN + "[SUCCESS] Backup completed at " + timestamp + RESET);
        } catch (IOException e) {
            System.err.println(RED + "[ERROR] Backup failed: " + e.getMessage() + RESET);
        }
    }

    private void copyFile(String sourcePath, String destPath) throws IOException {
        Path source = Paths.get(sourcePath);
        Path dest = Paths.get(destPath);
        if (Files.exists(source)) {
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
