package com.company.MultiModule.services;

import com.company.MultiModule.models.User;
import com.company.MultiModule.models.Student;
import com.company.MultiModule.models.Book;
import com.company.MultiModule.exceptions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class BorrowService {

    public static final String RESET = "\u001B[0m";
    public static final String RED   = "\u001B[91m";
    public static final String GREEN = "\u001B[92m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";

    private static final BorrowService instance = new BorrowService();

    private final Map<String, Set<String>> borrowMap = new ConcurrentHashMap<>();
    private final BookService bookService = BookService.getInstance();

    private final Map<String, ReentrantLock> bookLocks = new ConcurrentHashMap<>();
    private final Map<String, Condition> bookConditions = new ConcurrentHashMap<>();

    private BorrowService() {}

    public static BorrowService getInstance() {
        return instance;
    }

    public void borrowBook(User user, String bookId) throws LibraryException {
        ReentrantLock lock = bookLocks.computeIfAbsent(bookId, id -> new ReentrantLock());
        Condition condition = bookConditions.computeIfAbsent(bookId, id -> lock.newCondition());

        lock.lock();
        try {
            if (!(user instanceof Student student)) {
                throw new LibraryException("Only students can borrow books.");
            }

            Book book = bookService.findById(bookId);
            if (book == null) {
                throw new BookNotFound("Book with ISBN not found: " + bookId);
            }

            long timeout = 5; // seconds
            while (!book.isAvailable()) {
                System.out.println(YELLOW + user.getName() + RESET + " is waiting for the book " +
                        BLUE + "\"" + book.getTitle() + "\"" + RESET + " to become available...");
                try {
                    boolean available = condition.await(timeout, TimeUnit.SECONDS);
                    if (!available) {
                        throw new BookUnavailableException(book.getTitle());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new LibraryException("Thread interrupted while waiting for book.");
                }
            }

            Set<String> borrowed = borrowMap.computeIfAbsent(user.getId(), k -> new HashSet<>());

            if (borrowed.size() >= student.getBorrowLimit()) {
                throw new BorrowLimitExceed(student.getName(), student.getBorrowLimit());
            }

            borrowed.add(bookId);
            bookService.setAvailability(bookId, false);
            System.out.printf("%s successfully borrowed \"%s\".%n", user.getName(), book.getTitle());

        } finally {
            lock.unlock();
        }
    }


    public void returnBook(User user, String bookId) throws LibraryException {
        ReentrantLock lock = bookLocks.computeIfAbsent(bookId, id -> new ReentrantLock());
        Condition condition = bookConditions.computeIfAbsent(bookId, id -> lock.newCondition());

        lock.lock();
        try {
            Book book = bookService.findById(bookId);
            if (book == null) {
                throw new BookNotFound("Book not found: " + bookId);
            }

            Set<String> borrowed = borrowMap.get(user.getId());
            if (borrowed == null || !borrowed.contains(bookId)) {
                throw new BookNotBorrowedByUser(user.getName(), book.getIsbn());
            }

            borrowed.remove(bookId);
            if (borrowed.isEmpty()) {
                borrowMap.remove(user.getId());
            }

            bookService.setAvailability(bookId, true);
            System.out.printf("%s returned \"%s\" and notified others.%n", user.getName(), book.getTitle());

            condition.signalAll();

        } finally {
            lock.unlock();
        }
    }

    public Map<String, Set<String>> getBorrowMap() {
        return borrowMap;
    }

    public Set<String> getBorrowedBooks(String userId) {
        return borrowMap.getOrDefault(userId, Set.of());
    }



    //load and save
    public void saveToCsv() {
        List<String> lines = new ArrayList<>();
        lines.add("user_id,book_id"); // header

        for (Map.Entry<String, Set<String>> entry : borrowMap.entrySet()) {
            String userId = entry.getKey();
            for (String bookId : entry.getValue()) {
                lines.add(userId + "," + bookId);
            }
        }

        try {
            Files.write(Paths.get("data/borrow.csv"), lines);
            System.out.println(GREEN + "Borrow records saved successfully." + RESET);
        } catch (IOException e) {
            System.out.println(RED + "Error saving borrow records: " + e.getMessage() + RESET);
        }
    }

    public void loadFromCsv() {
        Path path = Paths.get("data/borrow.csv");
        if (!Files.exists(path)) return;

        try {
            List<String> lines = Files.readAllLines(path);
            for (int i = 1; i < lines.size(); i++) { // Skip header
                String[] parts = lines.get(i).split(",", 2);
                if (parts.length < 2) continue;

                String userId = parts[0];
                String bookId = parts[1];

                borrowMap.computeIfAbsent(userId, k -> new HashSet<>()).add(bookId);

                // Also mark the book as unavailable
                bookService.setAvailability(bookId, false);
            }

            System.out.println(GREEN + "Borrow records loaded from CSV." + RESET);
        } catch (IOException e) {
            System.out.println(RED + "Error loading borrow records: " + e.getMessage() + RESET);
        }
    }




}
