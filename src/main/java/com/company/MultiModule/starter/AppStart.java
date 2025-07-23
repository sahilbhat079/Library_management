package com.company.MultiModule.starter;

import com.company.MultiModule.exceptions.*;
import com.company.MultiModule.models.Book;
import com.company.MultiModule.models.Student;
import com.company.MultiModule.models.User;
import com.company.MultiModule.models.Librarian;
import com.company.MultiModule.services.*;

import java.util.*;

public class AppStart {

    // ANSI escape codes for colored output
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";

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
        borrowService.loadFromCsv();



        boolean quitApp = false;

        while (!quitApp) {
            login();

            // Start backup once the login in the system.
            BackupService.getInstance().startPeriodicBackup();
            boolean logout = false;

            while (!logout) {
                printMenu();
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> handleListBooks();

                    case "2" -> {
                        if (!isLibrarian()) handleBorrowBook();
                        else System.out.println(RED + " This option is not available for librarians." + RESET);
                    }

                    case "3" -> {
                        if (!isLibrarian()) handleReturnBook();
                        else System.out.println(RED + " This option is not available for librarians." + RESET);
                    }

                    case "4" -> handleSearchBook();

                    case "5" -> {
                        if (isLibrarian()) handleAddBook();
                        else System.out.println(RED + " Only librarians can add books." + RESET);
                    }

                    case "6" -> {
                        if (isLibrarian()) handleAddUser();
                        else System.out.println(RED + " Only librarians can add users." + RESET);
                    }

                    case "7" -> {
                        if (isLibrarian()) handleManualBackup();
                        else System.out.println(RED + " Only librarians can perform manual backup." + RESET);
                    }

                    case "8" -> {
                        if (isLibrarian()) handleAdminReport();
                        else System.out.println(RED + " Only librarians can generate reports." + RESET);
                    }

                    case "9" -> {
                        System.out.println(CYAN + "\nLogging out...\n" + RESET);
                        userService.saveToCsv();
                        bookService.saveToCsv();
                        borrowService.saveToCsv();
                        logout = true;
                    }

                    default -> System.out.println(RED + " Invalid option. Try again." + RESET);
                }
            }

            System.out.print(CYAN + "Do you want to login again? (yes/no): " + RESET);
            String again = scanner.nextLine().trim().toLowerCase();

            if (!again.equals("yes")) {
                quitApp = true;
                BackupService.getInstance().shutdown();
                System.out.println(GREEN + "\nSystem shutdown. Goodbye!" + RESET);
            }
        }
    }

    private void login() {
        while (true) {
            try {
                System.out.println(CYAN + "\n╔══════════════════════════════╗");
                System.out.println("║        USER LOGIN PORTAL     ║");
                System.out.println("╚══════════════════════════════╝" + RESET);

                System.out.print(BOLD + "Username : " + RESET);
                String name = scanner.nextLine().trim();

                System.out.print(BOLD + "Password : " + RESET);
                char[] password = scanner.nextLine().trim().toCharArray();

                loggedInUser = userService.login(name, password);
                System.out.println(GREEN + "\nLogin successful. Welcome, " + loggedInUser.getName() + "!" + RESET);
                break;

            } catch (UserNotFound e) {
                System.out.println(RED + "\nLogin failed: " + e.getMessage() + RESET);
            } catch (Exception e) {
                System.out.println(RED + "\nUnexpected error: " + e.getMessage() + RESET);
            }

            System.out.println(CYAN + "Please try again.\n" + RESET);
        }
    }


    private boolean isLibrarian() {
        return loggedInUser instanceof Librarian;
    }

    private void printMenu() {
        String role = isLibrarian() ? "Librarian" : "Student";

        System.out.println(CYAN + "\n------------------ MENU ------------------" + RESET);
        System.out.println("Logged in as: " + BOLD + loggedInUser.getName() + " (" + role + ")" + RESET);

        System.out.println("1. List all books");

        if (!isLibrarian()) {
            System.out.println("2. Borrow book");
            System.out.println("3. Return book");
        }

        System.out.println("4. Search books");

        if (isLibrarian()) {
            System.out.println("5. Add book");
            System.out.println("6. Add user");
            System.out.println("7. Manual backup");
            System.out.println("8. Generate admin report");
        }

        System.out.println("9. Exit");
        System.out.print(CYAN + "Select an option: " + RESET);
    }


    //book service
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

        if (isbn.isBlank()) {
            System.out.println(RED + " ISBN cannot be empty!" + RESET);
            return;
        }

        try {
            Book book = bookService.findByIsbn(isbn);

//            if(!book.isAvailable()){
//                System.out.println("Currently book is not available Borrowed By Someone else");
//                return;
//            }

            borrowService.borrowBook(loggedInUser, book.getId());
            System.out.println(GREEN + " Book borrowed successfully." + RESET);
        }
        catch (BookNotFound | BorrowLimitExceed e) {
            System.out.println(RED + e.getMessage() + RESET);
        }
        catch (LibraryException e) {
            System.out.println(RED + " Failed to borrow book: " + e.getMessage() + RESET);
        }
    }


    private void handleReturnBook() {
        System.out.print("Enter ISBN to return: ");
        String isbn = scanner.nextLine().trim();

        if (isbn.isBlank()) {
            System.out.println(RED + " ISBN cannot be empty!" + RESET);
            return;
        }

        try {
            Book book = bookService.findByIsbn(isbn);
            borrowService.returnBook(loggedInUser, book.getId());
            System.out.println(GREEN + " Book returned successfully." + RESET);
        }
        catch (BookNotFound bookNotFound){
            System.out.println(RED  + bookNotFound.getMessage()  + RESET);
        }
        catch (LibraryException e) {
            System.out.println(RED + " Failed to return book: " + e.getMessage() + RESET);
        }
    }

    private void handleSearchBook() {
        System.out.print("Search by title or author: ");
        String keyword = scanner.nextLine().trim();

        if (keyword.isBlank()) {
            System.out.println(RED + " Search keyword cannot be empty!" + RESET);
            return;
        }

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
        String title = scanner.nextLine().trim();
        if (title.isBlank()) {
            System.out.println(RED + " Title cannot be empty!" + RESET);
            return;
        }

        System.out.print("Author    : ");
        String author = scanner.nextLine().trim();
        if (author.isBlank()) {
            System.out.println(RED + " Author cannot be empty!" + RESET);
            return;
        }

        System.out.print("Category  : ");
        String category = scanner.nextLine().trim();
        if (category.isBlank()) {
            System.out.println(RED + " Category cannot be empty!" + RESET);
            return;
        }

        System.out.print("ISBN      : ");
        String isbn = scanner.nextLine().trim();
        if (isbn.isBlank()) {
            System.out.println(RED + " ISBN cannot be empty!" + RESET);
            return;
        }

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


    //user service
    private void handleAddUser() {
        System.out.println(CYAN + "\n========= Add New User =========" + RESET);

        System.out.print("Role [std/lib]     : ");
        String role = scanner.nextLine().trim().toLowerCase();
        if (!(role.equals("std") || role.equals("lib"))) {
            System.out.println(RED + " Invalid role. Use 'std' or 'lib' only." + RESET);
            return;
        }

        System.out.print("Name               : ");
        String name = scanner.nextLine().trim();
        if (name.isBlank()) {
            System.out.println(RED + " Name cannot be empty!" + RESET);
            return;
        }

        System.out.print("Email              : ");
        String email = scanner.nextLine().trim();
        if (email.isBlank()) {
            System.out.println(RED + " Email cannot be empty!" + RESET);
            return;
        }

        System.out.print("Password           : ");
        String passwordInput = scanner.nextLine();
        if (passwordInput.isBlank()) {
            System.out.println(RED + " Password cannot be empty!" + RESET);
            return;
        }
        char[] password = passwordInput.toCharArray();

        User newUser;

        switch (role) {
            case "std" -> {
                System.out.print("Borrow Limit       : ");
                String limitStr = scanner.nextLine().trim();
                if (limitStr.isBlank()) {
                    System.out.println(RED + " Borrow limit cannot be empty!" + RESET);
                    return;
                }

                int limit;
                try {
                    limit = Integer.parseInt(limitStr);
                } catch (NumberFormatException e) {
                    System.out.println(RED + " Invalid number for borrow limit." + RESET);
                    return;
                }

                newUser = new Student.StudentBuilder()
                        .name(name)
                        .email(email)
                        .password(password)
                        .borrowLimit(limit)
                        .build();
            }
            case "lib" -> {
                System.out.print("Employee Code      : ");
                String code = scanner.nextLine().trim();
                if (code.isBlank()) {
                    System.out.println(RED + " Employee code cannot be empty!" + RESET);
                    return;
                }

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



    // Backup and Report

    private void handleManualBackup() {
        BackupService.getInstance().backupNow();
        System.out.println(GREEN + " Manual backup completed." + RESET);
    }

//    private void handleAdminReport() {
//        if (!isLibrarian()) {
//            System.out.println(RED + " Only librarians can generate reports." + RESET);
//            return;
//        }
//
//        System.out.println(CYAN + "\n1. Generate Book Report");
//        System.out.println("2. Generate User Report");
//        System.out.print("Select: " + RESET);
//
//        String option = scanner.nextLine().trim();
//        ReportService reportService = ReportService.getInstance();
//
//        switch (option) {
//            case "1" -> reportService.generateBookReport(bookService.listAllBooks());
//            case "2" -> reportService.generateUserReport(new ArrayList<>(userService.getAllUsers().values()));
//            default -> System.out.println(RED + "Invalid choice." + RESET);
//        }
//    }



    private void handleAdminReport() {
        if (!isLibrarian()) {
            System.out.println(RED + " Only librarians can generate reports." + RESET);
            return;
        }

        Map<String, Runnable> reportOptions = new LinkedHashMap<>();
        ReportService reportService = ReportService.getInstance();

        reportOptions.put("1", () -> reportService.generateBookReport(bookService.listAllBooks()));
        reportOptions.put("2", () -> reportService.generateUserReport(new ArrayList<>(userService.getAllUsers().values())));
        reportOptions.put("3", reportService::generateBorrowReport);

        System.out.println(CYAN + "\n===== Admin Report Options =====" + RESET);
        System.out.println("1. Generate Book Report");
        System.out.println("2. Generate User Report");
        System.out.println("3. Generate Borrow Report");
        System.out.print("Select: ");

        String choice = scanner.nextLine().trim();

        Runnable selected = reportOptions.get(choice);
        if (selected != null) {
            selected.run();
        } else {
            System.out.println(RED + "Invalid choice." + RESET);
        }
    }



}
