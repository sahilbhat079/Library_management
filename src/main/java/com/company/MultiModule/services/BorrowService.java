package com.company.MultiModule.services;

import com.company.MultiModule.models.User;
import com.company.MultiModule.models.Student;
import com.company.MultiModule.models.Book;
import com.company.MultiModule.exceptions.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class BorrowService {

    private static final BorrowService instance = new BorrowService();
    private final Map<String, Set<String>> borrowMap = new HashMap<>();
    private final BookService bookService = BookService.getInstance();
    private final Map<String, ReentrantLock> bookLocks = new ConcurrentHashMap<>();


    private BorrowService() {}

    public static BorrowService getInstance() {
        return instance;
    }

    public synchronized void borrowBook(User user, String bookId) throws LibraryException {
        ReentrantLock lock = bookLocks.computeIfAbsent(bookId, id -> new ReentrantLock());

        lock.lock();
try{
        if (!(user instanceof Student student)) {
            throw new LibraryException("Only students can borrow books.");
        }

        Book book = bookService.findById(bookId);
        if (book == null) throw new BookNotFound(bookId);
        if (!book.isAvailable()) throw new BookAlreadyBorrowed(bookId);

        Set<String> borrowed = borrowMap.computeIfAbsent(user.getId(), k -> new HashSet<>());

        if (borrowed.size() >= student.getBorrowLimit()) {
            throw new BorrowLimitExceed(student.getId(), student.getBorrowLimit());
        }

        borrowed.add(bookId);
        bookService.setAvailability(bookId, false);}
finally {
    lock.unlock();
}
    }

    public synchronized void returnBook(User user, String bookId) throws LibraryException {
        ReentrantLock lock = bookLocks.computeIfAbsent(bookId, id -> new ReentrantLock());
        lock.lock();
        try {
        Book book = bookService.findById(bookId);
        if (book == null) throw new BookNotFound(bookId);

        Set<String> borrowed = borrowMap.getOrDefault(user.getId(), Set.of());

        if (!borrowed.contains(bookId)) {
            throw new BookNotBorrowedByUser(user.getId(), bookId);
        }

        borrowed.remove(bookId);
        bookService.setAvailability(bookId, true);
    } finally {
            lock.unlock();
        }

    }

    public Set<String> getBorrowedBooks(String userId) {
        return borrowMap.getOrDefault(userId, Set.of());
    }

}
