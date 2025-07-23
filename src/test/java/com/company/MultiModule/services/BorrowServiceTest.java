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
//        borrowService.clearAll(); // added method in BorrowService
//        bookService.clearBooks(); // added method in BookService

        student = new Student.StudentBuilder()
                .name("Test Student")
                .email("student@example.com")
                .password("1234".toCharArray())
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
        assertTrue(borrowService.getBorrowedBooks(student.getId()).contains(book.getId()));
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

        assertEquals("User 'Test Student' has exceeded the borrow limit of 1 books.", ex.getMessage());
    }

    @Test
    void testReturnAndNotify() throws Exception {
        borrowService.borrowBook(student, book.getId());

        Student another = new Student.StudentBuilder()
                .name("Another Student")
                .email("another@example.com")
                .password("1234".toCharArray())
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

        TimeUnit.MILLISECONDS.sleep(500);
        borrowService.returnBook(student, book.getId());
        future.get(2, TimeUnit.SECONDS);

        assertTrue(borrowService.getBorrowedBooks(another.getId()).contains(book.getId()));
        executor.shutdown();
    }


    @Test
    void testReturnWithoutBorrowing() {
        Student random = new Student.StudentBuilder()
                .name("Random")
                .email("random@example.com")
                .password("123".toCharArray())
                .borrowLimit(1)
                .build();

        assertThrows(BookNotBorrowedByUser.class, () -> {
            borrowService.returnBook(random, book.getId());
        });
    }

    @Test
    void testBookNotFound() {
        LibraryException ex = assertThrows(BookNotFound.class, () -> {
            borrowService.borrowBook(student, UUID.randomUUID().toString());
        });

        assertTrue(ex.getMessage().contains("Book with ISBN not found"));
    }



    @Test
    void testBookUnavailableTimeout() throws Exception {
        // First student borrows the book
        borrowService.borrowBook(student, book.getId());

        // Another student tries to borrow the same book, which should timeout
        Student waiter = new Student.StudentBuilder()
                .name("Waiter Student")
                .email("waiter@example.com")
                .password("wait123".toCharArray())
                .borrowLimit(1)
                .build();

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<?> future = executor.submit(() -> {
            BookUnavailableException ex = assertThrows(BookUnavailableException.class, () -> {
                borrowService.borrowBook(waiter, book.getId()); // should timeout
            });

            // Updated to match new message
            assertTrue(ex.getMessage().contains("Book"));
            assertTrue(ex.getMessage().contains(book.getTitle()));
            assertTrue(ex.getMessage().contains("not available"));
        });

        future.get(10, TimeUnit.SECONDS); // Wait max 10 seconds for the test to complete
        executor.shutdown();
    }







}
