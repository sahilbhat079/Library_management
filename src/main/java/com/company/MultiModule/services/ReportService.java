package com.company.MultiModule.services;

import com.company.MultiModule.exceptions.UserNotFound;
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

    // Colors for writeReport output
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";

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
                    if ("password".equals(field.getName()) && value instanceof char[])
                    {
                        value=new String((char[])value);
                    }
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



    public void generateBorrowReport() {
        System.out.println();
        System.out.println(HEADER + "========== BORROW REPORT ==========" + RESET + "\n");

        List<String> lines = new ArrayList<>();
        lines.add("user_id,user_name,user_email,book_id,book_title,book_isbn");

        BorrowService borrowService = BorrowService.getInstance();
        UserService userService = UserService.getInstance();
        BookService bookService = BookService.getInstance();

        Map<String, Set<String>> borrowMap = borrowService.getBorrowMap();

        if (borrowMap.isEmpty()) {
            System.out.println(RED + "No borrow records found." + RESET);
            return;
        }

        for (Map.Entry<String, Set<String>> entry : borrowMap.entrySet()) {
            String userId = entry.getKey();
            User user;
            try {
                user = userService.findUserById(userId);
            } catch (UserNotFound e) {
                System.out.println(RED + "User with ID '" + userId + "' not found." + RESET);
                continue;
            }

            Set<String> bookIds = entry.getValue();

            for (String bookId : bookIds) {
                Book book = bookService.findById(bookId);
                if (book == null) {
                    System.out.println(RED + "Book with ID '" + bookId + "' not found." + RESET);
                    continue;
                }

                // Console output
                System.out.println(FIELD + "User ID       : " + RESET + user.getId());
                System.out.println(FIELD + "User Name     : " + RESET + user.getName());
                System.out.println(FIELD + "User Email    : " + RESET + user.getEmail());
                System.out.println(FIELD + "Book ID       : " + RESET + book.getId());
                System.out.println(FIELD + "Book Title    : " + RESET + book.getTitle());
                System.out.println(FIELD + "Book ISBN     : " + RESET + book.getIsbn());
                System.out.println(LINE + "------------------------------" + RESET);

                // CSV line
                lines.add(String.join(",",
                        user.getId(),
                        escape(user.getName()),
                        escape(user.getEmail()),
                        book.getId(),
                        escape(book.getTitle()),
                        escape(book.getIsbn())
                ));
            }
        }

        writeReport("borrow_report.csv", lines);
    }





    private void writeReport(String filename, List<String> lines) {
        try {
            // Create report directory if it doesn't exist
            Path file = Paths.get(REPORT_DIR, filename);
            // Atomic save
            Files.write(file, lines);
            System.out.println(GREEN + " Report saved to: " + file.toAbsolutePath() + RESET);
        } catch (IOException e) {
            System.out.println(RED + " Failed to save report: " + e.getMessage() + RESET);
        }
    }

    private String escape(String s) {
        return s.replace(",", "%2C");
    }



}
