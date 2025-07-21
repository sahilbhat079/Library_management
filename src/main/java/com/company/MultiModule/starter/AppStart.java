package com.company.MultiModule.starter;

import com.company.MultiModule.exceptions.LibraryException;
import com.company.MultiModule.exceptions.UserNotFound;
import com.company.MultiModule.models.Book;
import com.company.MultiModule.models.Student;
import com.company.MultiModule.models.User;
import com.company.MultiModule.models.Librarian;
import com.company.MultiModule.services.BackupService;
import com.company.MultiModule.services.BookService;
import com.company.MultiModule.services.BorrowService;
import com.company.MultiModule.services.UserService;

import java.util.List;
import java.util.Scanner;

public class AppStart {
    // ANSI escape code for green text
    public static final String GREEN = "\u001B[32m";
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";


    private final Scanner scanner = new Scanner(System.in);
    //Singleton pattern
    private final BookService bookService = BookService.getInstance();
    private final BorrowService borrowService = BorrowService.getInstance();
    private final UserService userService = UserService.getInstance();


    private User loggedInUser;

    public void run() {
        System.out.println("📚 Welcome to the Library Management System!");



        // Load data before login
        bookService.loadFromCsv();
        userService.loadFromCsv();

        login();

        // Start periodic backup
        BackupService.getInstance().startPeriodicBackup();

        boolean exit = false;

        while (!exit) {
            printMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> handleListBooks();
                case "2" -> handleBorrowBook();
                case "3" -> handleReturnBook();
                case "4" -> handleSearchBook();
                case "5" -> {
                    if (isLibrarian()) handleAddBook();
                    else System.out.println(" Only librarians can add books.");
                }
                case "6" -> {
                    if (isLibrarian()) handleAddUser();
                    else System.out.println(" Only librarians can add users.");
                }
                case "7" -> handleManualBackup();
                case "8" -> handleAdminReport();
                case "9" -> {
                    System.out.println(" Exiting system. Goodbye!");
                    BackupService.getInstance().shutdown();
                    userService.saveToCsv();
                    bookService.saveToCsv();
                    exit = true;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void login() {
        System.out.print("Enter username: ");
        String name = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            loggedInUser = UserService.getInstance().login(name, password);
            System.out.println(" Login successful! Welcome, " + loggedInUser.getName());
        } catch (UserNotFound e) {
            System.out.println(" Login failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private boolean isLibrarian() {
        return loggedInUser instanceof Librarian;
    }

    private void printMenu() {
        System.out.println("\n\uD83D\uDCCB Menu:");
        System.out.println("1. List all books");
        System.out.println("2. Borrow book");
        System.out.println("3. Return book");
        System.out.println("4. Search books");
        if (isLibrarian()) {
            System.out.println("5. Add book");
            System.out.println("6. Add user");
        }
        System.out.println("7. Manual backup");
        System.out.println("8. Generate Admin Report");
        System.out.println("9. Exit");
        System.out.print("Select an option: ");
    }

    // === Placeholder handlers ===
    private void handleListBooks() {

        List<Book> books = bookService.listAllBooks();

        if (books.isEmpty()) {
            System.out.println("No books available.");
        } else {
            System.out.println(GREEN + "\n==========  AVAILABLE BOOKS  ==========" + RESET);
            int index = 1;
            for (Book book : books) {
                System.out.println(GREEN + "\nBook #" + index++ + RESET);
                System.out.println("  Title     : " + book.getTitle());
                System.out.println("  Author    : " + book.getAuthor());
                System.out.println("  Category  : " + book.getCategory());
                System.out.println("  ISBN      : " + book.getIsbn());
                System.out.println(GREEN + "----------------------------------------" + RESET);
            }
        }

    }

    private void handleBorrowBook() {
        System.out.print("Enter ISBN of the book to borrow: ");
        String isbn = scanner.nextLine().trim();

        try {
            Book book = bookService.findByIsbn(isbn); // get the book object first
            borrowService.borrowBook(loggedInUser, book.getId()); // use the book ID internally
            System.out.println(GREEN + "Book borrowed successfully." + RESET);
        } catch (LibraryException e) {
            System.out.println("Failed to borrow book: " + e.getMessage());
        }
    }

    private void handleReturnBook() {
        System.out.print("Enter ISBN of the book to return: ");
        String isbn = scanner.nextLine().trim();

        try {
            Book book = bookService.findByIsbn(isbn); // get book object from ISBN
            borrowService.returnBook(loggedInUser, book.getId()); // use internal ID
            System.out.println(GREEN + "Book returned successfully." + RESET);
        } catch (LibraryException e) {
            System.out.println("Failed to return book: " + e.getMessage());
        }
    }

    private void handleSearchBook() {
        System.out.print("Enter title or author to search: ");
        String keyword = scanner.nextLine();

        List<Book> found = bookService.search(keyword);
        if (found.isEmpty()) {
            System.out.println("No books found matching keyword: " + keyword);
        } else {
            System.out.println(GREEN + "\n=== Search Results ===" + RESET);
            int index = 1;
            for (Book book : found) {
                System.out.println(GREEN + "\nResult #" + index++ + RESET);
                System.out.println("  Title     : " + book.getTitle());
                System.out.println("  Author    : " + book.getAuthor());
                System.out.println("  ISBN      : " + book.getIsbn());
                System.out.println("  Category  : " + book.getCategory());
                System.out.println(GREEN + "-----------------------------" + RESET);
            }
        }
    }

    private void handleAddBook() {
        System.out.println("\n=== Add a New Book ===");

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

        System.out.println(GREEN + "✔ Book added successfully!" + RESET);
        System.out.println(GREEN + "   ↪ Book ID  : " + book.getId() + RESET);
        System.out.println(GREEN + "   ↪ ISBN     : " + book.getIsbn() + RESET);
    }

    private void handleAddUser() {
        System.out.println("\n====================");
        System.out.println("   Add New Library User");
        System.out.println("====================");

        System.out.println("\nSelect user role:");
        System.out.println("  [std] - Student");
        System.out.println("  [lib] - Librarian");
        System.out.print("\nEnter Role        : ");
        String role = scanner.nextLine().trim().toLowerCase();

        System.out.print("Enter Name        : ");
        String name = scanner.nextLine();
        System.out.print("Enter Email       : ");
        String email = scanner.nextLine();
        System.out.print("Enter Password    : ");
        String password = scanner.nextLine();

        User newUser;

        switch (role) {
            case "std" -> {
                System.out.print("Enter Borrow Limit: ");
                int limit = Integer.parseInt(scanner.nextLine());
                newUser = new Student.StudentBuilder()
                        .name(name)
                        .email(email)
                        .password(password)
                        .borrowLimit(limit)
                        .build();
            }
            case "lib" -> {
                System.out.print("Enter Emp. Code   : ");
                String code = scanner.nextLine();
                newUser = new Librarian.LibrarianBuilder()
                        .name(name)
                        .email(email)
                        .password(password)
                        .employeeCode(code)
                        .build();
            }
            default -> {
                System.out.println(RED + "\n Invalid role! Use 'std' or 'lib' only." + RESET);
                return;
            }
        }

        userService.addUser(newUser);
        System.out.println(GREEN + "\n User added successfully!" + RESET);
        System.out.println("Assigned User ID  : " + newUser.getId());
    }

    private void handleManualBackup() {
//        System.out.println("Performing manual backup...");
        BackupService.getInstance().backupNow();
    }
    private void handleAdminReport() {
        if (!(loggedInUser instanceof Librarian)) {
            System.out.println(" Only librarians can generate admin reports.");
            return;
        }

//        new AdminReportGenerator().generate();
    }


}
