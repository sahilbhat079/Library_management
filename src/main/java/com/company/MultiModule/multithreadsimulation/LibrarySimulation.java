package com.company.MultiModule.multithreadsimulation;

import com.company.MultiModule.models.Book;
import com.company.MultiModule.models.Student;
import com.company.MultiModule.models.User;
import com.company.MultiModule.services.BookService;
import com.company.MultiModule.services.BorrowService;
import com.company.MultiModule.services.UserService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LibrarySimulation {

    private final BookService bookService = BookService.getInstance();
    private final BorrowService borrowService = BorrowService.getInstance();
    private final UserService userService = UserService.getInstance();

    public void run() throws Exception {
        setupData();

        Runnable task1 = () -> simulateBorrowReturn(userService.findByEmail("alice@example.com"), "111");
        Runnable task2 = () -> simulateBorrowWithReturn(userService.findByEmail("bob@example.com"), "111");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(task2); // Bob borrows and returns after 3 seconds
        Thread.sleep(500);      // Delay so Alice tries to borrow next
        executor.submit(task1); // Alice will wait if book is unavailable

        executor.shutdown();
        executor.awaitTermination(20, TimeUnit.SECONDS);
    }

    private void setupData() {
        Book book1 = new Book.Builder<>()
                .title("Clean Code")
                .author("Robert Martin")
                .category("Programming")
                .isbn("111")
                .build();

        Book book2 = new Book.Builder<>()
                .title("Effective Java")
                .author("Joshua Bloch")
                .category("Programming")
                .isbn("222")
                .build();

        bookService.addBook(book1);
        bookService.addBook(book2);

        User user1 = new Student.StudentBuilder()
                .name("Alice")
                .email("alice@example.com")
                .password("123".toCharArray())
                .borrowLimit(1)
                .build();

        User user2 = new Student.StudentBuilder()
                .name("Bob")
                .email("bob@example.com")
                .password("456".toCharArray())
                .borrowLimit(1)
                .build();

        userService.addUser(user1);
        userService.addUser(user2);
    }

    private void simulateBorrowReturn(User user, String isbn) {
        try {
            Book book = bookService.findByIsbn(isbn);
            System.out.printf("%s attempting to borrow \"%s\"%n", user.getName(), book.getTitle());

            borrowService.borrowBook(user, book.getId());
            System.out.printf("%s successfully borrowed \"%s\"%n", user.getName(), book.getTitle());

            Thread.sleep(2000); // simulate reading

            borrowService.returnBook(user, book.getId());
            System.out.printf("%s returned \"%s\"%n", user.getName(), book.getTitle());
        } catch (Exception e) {
            System.out.printf("%s failed: %s%n", user.getName(), e.getMessage());
        }
    }

    private void simulateBorrowWithReturn(User user, String isbn) {
        try {
            Book book = bookService.findByIsbn(isbn);
            System.out.printf("%s attempting to borrow \"%s\"%n", user.getName(), book.getTitle());

            borrowService.borrowBook(user, book.getId());
            System.out.printf("%s successfully borrowed \"%s\"%n", user.getName(), book.getTitle());

            Thread.sleep(3000); // simulate reading
            borrowService.returnBook(user, book.getId());
            System.out.printf("%s returned \"%s\"%n", user.getName(), book.getTitle());
        } catch (Exception e) {
            System.out.printf("%s failed: %s%n", user.getName(), e.getMessage());
        }
    }
}
