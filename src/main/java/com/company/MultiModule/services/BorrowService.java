package com.company.MultiModule.services;

import com.company.MultiModule.models.User;
import com.company.MultiModule.models.Student;
import com.company.MultiModule.models.Book;
import com.company.MultiModule.exceptions.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class BorrowService {

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
                System.out.printf("%s is waiting for \"%s\" to become available.%n", user.getName(), book.getTitle());
                try {
                    boolean available = condition.await(timeout, TimeUnit.SECONDS);
                    if (!available) {
                        throw new BookUnavailableException(book.getTitle(), timeout);
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


    public Set<String> getBorrowedBooks(String userId) {
        return borrowMap.getOrDefault(userId, Set.of());
    }


}
