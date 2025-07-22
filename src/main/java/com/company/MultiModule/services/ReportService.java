package com.company.MultiModule.services;

import com.company.MultiModule.models.Book;
import com.company.MultiModule.models.User;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;

public class ReportService {

    private static final ReportService instance = new ReportService();

    private static final String RESET = "\u001B[0m";
    private static final String HEADER = "\u001B[95m";
    private static final String TITLE = "\u001B[94m";
    private static final String SECTION = "\u001B[96m";
    private static final String FIELD = "\u001B[92m";
    private static final String VALUE = "\u001B[93m";
    private static final String LINE = "\u001B[90m";

    private static final String REPORT_DIR = "data/reports";

    private ReportService() {
        try {
            Files.createDirectories(Paths.get(REPORT_DIR));
        } catch (IOException e) {
            System.out.println("Failed to create reports directory: " + e.getMessage());
        }
    }

    public static ReportService getInstance() {
        return instance;
    }

    public void generateBookReport(List<Book> books) {
        System.out.println();
        System.out.println(HEADER + "========== BOOK REPORT ==========" + RESET + "\n");

        List<String> lines = new ArrayList<>();
        lines.add("id,isbn,title,author,category,available");

        for (Book book : books) {
            printObjectDetails(book);
            lines.add(String.join(",",
                    book.getId(),
                    escape(book.getIsbn()),
                    escape(book.getTitle()),
                    escape(book.getAuthor()),
                    escape(book.getCategory()),
                    String.valueOf(book.isAvailable())
            ));
            System.out.println(LINE + "------------------------------" + RESET);
        }

        writeReport("book_report.csv", lines);
    }

    public void generateUserReport(List<User> users) {
        System.out.println();
        System.out.println(HEADER + "========== USER REPORT ==========" + RESET + "\n");

        List<String> lines = new ArrayList<>();
        lines.add("id,name,email,password,class");

        for (User user : users) {
            printObjectDetails(user);
            lines.add(String.join(",",
                    user.getId(),
                    escape(user.getName()),
                    escape(user.getEmail()),
                    escape(String.valueOf(user.getPassword())),
                    user.getClass().getSimpleName()
            ));
            System.out.println(LINE + "------------------------------" + RESET);
        }

        writeReport("user_report.csv", lines);
    }

    private void printObjectDetails(Object obj) {
        Class<?> clazz = obj.getClass();
        System.out.println(TITLE + "Class: " + clazz.getSimpleName() + RESET);

        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    System.out.printf("  %s%-20s%s : %s%s%s%n",
                            FIELD, field.getName(), RESET,
                            VALUE, value, RESET);
                } catch (IllegalAccessException e) {
                    System.out.printf("  %s%-20s%s : %s[access denied]%s%n",
                            FIELD, field.getName(), RESET, VALUE, RESET);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private void writeReport(String filename, List<String> lines) {
        try {
            Path file = Paths.get(REPORT_DIR, filename);
            Files.write(file, lines);
            System.out.println(GREEN + " Report saved to: " + file.toAbsolutePath() + RESET);
        } catch (IOException e) {
            System.out.println(RED + " Failed to save report: " + e.getMessage() + RESET);
        }
    }

    private String escape(String s) {
        return s.replace(",", "%2C");
    }

    // Colors for writeReport output
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
}
