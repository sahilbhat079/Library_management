package com.company.MultiModule.services;


import com.company.MultiModule.exceptions.*;
import com.company.MultiModule.models.Book;
import com.company.MultiModule.models.Student;
import com.company.MultiModule.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class BorrowServiceTest {

    private final BorrowService borrowService = BorrowService.getInstance();
    private final BookService bookService = BookService.getInstance();

    private Student student;
    private Book book;

    @BeforeEach
    void setUp() {
        student = new Student.StudentBuilder()
                .name("Test Student")
                .email("student@example.com")
                .password("1234")
                .borrowLimit(1)
                .build();

        book = new Book.Builder<>()
                .title("Effective Java")
                .author("Joshua Bloch")
                .isbn("ISBN123")
                .category("Programming")
                .build();

        bookService.addBook(book);
    }

    @Test
    void testSuccessfulBorrow() throws LibraryException {
        borrowService.borrowBook(student, book.getId());
        assertFalse(bookService.findById(book.getId()).isAvailable());
    }

    @Test
    void testBorrowExceedLimit() throws LibraryException {
        Book secondBook = new Book.Builder<>()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("ISBN456")
                .category("Programming")
                .build();
        bookService.addBook(secondBook);

        borrowService.borrowBook(student, book.getId());

        LibraryException ex = assertThrows(BorrowLimitExceed.class, () -> {
            borrowService.borrowBook(student, secondBook.getId());
        });

        assertEquals("User '" + student.getId() + "' has exceeded the borrow limit of 1 books.", ex.getMessage());
    }

    @Test
    void testReturnAndNotify() throws Exception {
        borrowService.borrowBook(student, book.getId());

        Student another = new Student.StudentBuilder()
                .name("Another Student")
                .email("another@example.com")
                .password("1234")
                .borrowLimit(1)
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<?> future = executor.submit(() -> {
            try {
                borrowService.borrowBook(another, book.getId());
            } catch (LibraryException e) {
                fail("Thread should borrow after return");
            }
        });

        // Wait briefly and return the book
        TimeUnit.MILLISECONDS.sleep(500);
        borrowService.returnBook(student, book.getId());

        // Wait for thread to complete
        future.get(2, TimeUnit.SECONDS);

        executor.shutdown();
    }

    @Test
    void testReturnWithoutBorrowing() {
        Student random = new Student.StudentBuilder()
                .name("Random")
                .email("random@example.com")
                .password("123")
                .borrowLimit(1)
                .build();

        assertThrows(BookNotBorrowedByUser.class, () -> {
            borrowService.returnBook(random, book.getId());
        });
    }
}
