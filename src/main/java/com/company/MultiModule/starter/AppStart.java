package com.company.MultiModule.starter;

import com.company.MultiModule.exceptions.LibraryException;
import com.company.MultiModule.exceptions.UserNotFound;
import com.company.MultiModule.models.Book;
import com.company.MultiModule.models.Student;
import com.company.MultiModule.models.User;
import com.company.MultiModule.models.Librarian;
import com.company.MultiModule.services.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AppStart {

    // ANSI escape codes for colored output
    public static final String GREEN = "\u001B[32m";
    public static final String RED = "\u001B[31m";
    public static final String CYAN = "\u001B[36m";
    public static final String RESET = "\u001B[0m";

    private final Scanner scanner = new Scanner(System.in);

    private final BookService bookService = BookService.getInstance();
    private final BorrowService borrowService = BorrowService.getInstance();
    private final UserService userService = UserService.getInstance();

    private User loggedInUser;

    public void run() {
        System.out.println(CYAN + "\n========= Welcome to the Library Management System =========\n" + RESET);

        // Load persisted data
        bookService.loadFromCsv();
        userService.loadFromCsv();

        login();
        BackupService.getInstance().startPeriodicBackup();

        boolean exit = false;

        while (!exit) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> handleListBooks();
                case "2" -> handleBorrowBook();
                case "3" -> handleReturnBook();
                case "4" -> handleSearchBook();
                case "5" -> {
                    if (isLibrarian()) handleAddBook();
                    else System.out.println(RED + " Only librarians can add books." + RESET);
                }
                case "6" -> {
                    if (isLibrarian()) handleAddUser();
                    else System.out.println(RED + " Only librarians can add users." + RESET);
                }
                case "7" -> handleManualBackup();
                case "8" -> handleAdminReport();
                case "9" -> {
                    System.out.println(CYAN + "\n Exiting system. Goodbye!\n" + RESET);
                    BackupService.getInstance().shutdown();
                    userService.saveToCsv();
                    bookService.saveToCsv();
                    exit = true;
                }
                default -> System.out.println(RED + " Invalid option. Try again." + RESET);
            }
        }
    }

    private void login() {
        System.out.print("Username: ");
        String name = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            loggedInUser = userService.login(name, password);
            System.out.println(GREEN + " Login successful. Welcome, " + loggedInUser.getName() + "!" + RESET);
        } catch (UserNotFound e) {
            System.out.println(RED + " Login failed: " + e.getMessage() + RESET);
            System.exit(1);
        }
    }

    private boolean isLibrarian() {
        return loggedInUser instanceof Librarian;
    }

    private void printMenu() {
        System.out.println(CYAN + "\n------------------ MENU ------------------" + RESET);
        System.out.println("1. List all books");
        System.out.println("2. Borrow book");
        System.out.println("3. Return book");
        System.out.println("4. Search books");
        if (isLibrarian()) {
            System.out.println("5. Add book");
            System.out.println("6. Add user");
        }
        System.out.println("7. Manual backup");
        System.out.println("8. Generate admin report");
        System.out.println("9. Exit");
        System.out.print(CYAN + "Select an option: " + RESET);
    }

    private void handleListBooks() {
        List<Book> books = bookService.listAllBooks();

        if (books.isEmpty()) {
            System.out.println(RED + " No books available." + RESET);
            return;
        }

        System.out.println(GREEN + "\n========= AVAILABLE BOOKS =========" + RESET);
        int index = 1;
        for (Book book : books) {
            System.out.printf(GREEN + "\nBook #%d\n" + RESET, index++);
            System.out.println("  Title    : " + book.getTitle());
            System.out.println("  Author   : " + book.getAuthor());
            System.out.println("  Category : " + book.getCategory());
            System.out.println("  ISBN     : " + book.getIsbn());
        }
    }

    private void handleBorrowBook() {
        System.out.print("Enter ISBN to borrow: ");
        String isbn = scanner.nextLine().trim();

        try {
            Book book = bookService.findByIsbn(isbn);
            borrowService.borrowBook(loggedInUser, book.getId());
            System.out.println(GREEN + " Book borrowed successfully." + RESET);
        } catch (LibraryException e) {
            System.out.println(RED + " Failed to borrow book: " + e.getMessage() + RESET);
        }
    }

    private void handleReturnBook() {
        System.out.print("Enter ISBN to return: ");
        String isbn = scanner.nextLine().trim();

        try {
            Book book = bookService.findByIsbn(isbn);
            borrowService.returnBook(loggedInUser, book.getId());
            System.out.println(GREEN + " Book returned successfully." + RESET);
        } catch (LibraryException e) {
            System.out.println(RED + " Failed to return book: " + e.getMessage() + RESET);
        }
    }

    private void handleSearchBook() {
        System.out.print("Search by title or author: ");
        String keyword = scanner.nextLine();

        List<Book> results = bookService.search(keyword);
        if (results.isEmpty()) {
            System.out.println(RED + " No books found." + RESET);
        } else {
            System.out.println(GREEN + "\n========= SEARCH RESULTS =========" + RESET);
            int index = 1;
            for (Book book : results) {
                System.out.printf(GREEN + "\nResult #%d\n" + RESET, index++);
                System.out.println("  Title    : " + book.getTitle());
                System.out.println("  Author   : " + book.getAuthor());
                System.out.println("  Category : " + book.getCategory());
                System.out.println("  ISBN     : " + book.getIsbn());
            }
        }
    }

    private void handleAddBook() {
        System.out.println(CYAN + "\n========= Add a New Book =========" + RESET);

        System.out.print("Title     : ");
        String title = scanner.nextLine();

        System.out.print("Author    : ");
        String author = scanner.nextLine();

        System.out.print("Category  : ");
        String category = scanner.nextLine();

        System.out.print("ISBN      : ");
        String isbn = scanner.nextLine();

        Book book = new Book.Builder<>()
                .title(title)
                .author(author)
                .category(category)
                .isbn(isbn)
                .build();

        bookService.addBook(book);
        bookService.saveToCsv();

        System.out.println(GREEN + " Book added successfully." + RESET);
        System.out.println(" Book ID : " + book.getId());
        System.out.println(" ISBN    : " + book.getIsbn());
    }

    private void handleAddUser() {
        System.out.println(CYAN + "\n========= Add New User =========" + RESET);

        System.out.print("Role [std/lib]     : ");
        String role = scanner.nextLine().trim().toLowerCase();

        System.out.print("Name               : ");
        String name = scanner.nextLine();

        System.out.print("Email              : ");
        String email = scanner.nextLine();

        System.out.print("Password           : ");
        String password = scanner.nextLine();

        User newUser;

        switch (role) {
            case "std" -> {
                System.out.print("Borrow Limit       : ");
                int limit = Integer.parseInt(scanner.nextLine());
                newUser = new Student.StudentBuilder()
                        .name(name)
                        .email(email)
                        .password(password)
                        .borrowLimit(limit)
                        .build();
            }
            case "lib" -> {
                System.out.print("Employee Code      : ");
                String code = scanner.nextLine();
                newUser = new Librarian.LibrarianBuilder()
                        .name(name)
                        .email(email)
                        .password(password)
                        .employeeCode(code)
                        .build();
            }
            default -> {
                System.out.println(RED + " Invalid role. Use 'std' or 'lib' only." + RESET);
                return;
            }
        }

        userService.addUser(newUser);
        System.out.println(GREEN + " User added successfully!" + RESET);
        System.out.println(" User ID : " + newUser.getId());
    }

    private void handleManualBackup() {
        BackupService.getInstance().backupNow();
        System.out.println(GREEN + " Manual backup completed." + RESET);
    }

    private void handleAdminReport() {
        if (!isLibrarian()) {
            System.out.println(RED + " Only librarians can generate reports." + RESET);
            return;
        }

        ReportService.getInstance().generateReport(
                bookService.listAllBooks(),
                new ArrayList<>(userService.getAllUsers().values())
        );

        System.out.println(GREEN + " Admin report generated." + RESET);
    }
}
