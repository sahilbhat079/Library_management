package com.company.MultiModule.services;

import com.company.MultiModule.Repository.CsvStorage;
import com.company.MultiModule.exceptions.UserNotFound;
import com.company.MultiModule.models.Librarian;
import com.company.MultiModule.models.Student;
import com.company.MultiModule.models.User;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class UserService {

    final String CYAN = "\u001B[36m";
    final String RESET = "\u001B[0m";
    final int BOX_WIDTH = 60;

    private static final String USER_CSV_FILE = "data/users.csv";
    private static final UserService instance = new UserService();
    private final Map<String, User> users = new HashMap<>();

    private UserService() {
        // Add dummy Librarian
        Librarian librarian = new Librarian.LibrarianBuilder()
                .name("admin")
                .email("lib@library.com")
                .password("admin123".toCharArray())
                .employeeCode("LIB-001")
                .build();
        users.put(librarian.getId(), librarian);

        // Add dummy Student
        Student student = new Student.StudentBuilder()
                .name("sahil")
                .email("sahil@student.com")
                .password("pass123".toCharArray())
                .borrowLimit(3)
                .build();
        users.put(student.getId(), student);

        // Debug output
        String title = "DUMMY USERS LOADED";
        printHeader(title);
        users.values().forEach(u -> {
            String info = String.format("Username: %s, Password: %s, Role: %s",
                    u.getName(), new String(u.getPassword()), u.getClass().getSimpleName());
            System.out.println(CYAN + "|" + padRight(info, BOX_WIDTH - 2) + "|" + RESET);
        });
        printFooter();
    }

    public static UserService getInstance() {
        return instance;
    }

    /**
     * Authenticate user by name and password
     */
    public User login(String name, char[] password) throws UserNotFound {
        return users.values().stream()
                .filter(u -> u.getName().equalsIgnoreCase(name) && Arrays.equals(u.getPassword(), password))
                .findFirst()
                .orElseThrow(() -> new UserNotFound(name));
    }



    /**
     * Add new user
     */
    public void addUser(User user) {
        users.put(user.getId(), user);
    }

    /**
     * Find user by ID
     */
    public User findUserById(String id) throws UserNotFound {
        User user = users.get(id);
        if (user == null) throw new UserNotFound(id);
        return user;
    }

    /**
     * Get all registered users
     */
    public Map<String, User> getAllUsers() {
        return users;
    }

    /**
     * Find by email
     */
    public User findByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    /**
     * Save all users to CSV
     */
    public void saveToCsv() {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("id,name,email,password,type,extra");

            for (User user : users.values()) {
                String type;
                String extra;

                if (user instanceof Student s) {
                    type = "std";
                    extra = String.valueOf(s.getBorrowLimit());
                } else if (user instanceof Librarian l) {
                    type = "lib";
                    extra = l.getEmployeeCode();
                } else {
                    continue; // Unknown type
                }

                lines.add(String.join(",",
                        user.getId(),
                        escape(user.getName()),
                        escape(user.getEmail()),
                        escape(new String(user.getPassword())),
                        type,
                        escape(extra)
                ));
            }

            CsvStorage.save(USER_CSV_FILE, lines);
            System.out.println(" Users saved to CSV.");
        } catch (IOException e) {
            System.out.println(" Failed to save users to CSV: " + e.getMessage());
        }
    }

    /**
     * Load users from CSV
     */
    public void loadFromCsv() {
        try {
            List<String> lines = CsvStorage.load(USER_CSV_FILE);
            if (lines.size() <= 1) return;

            users.clear();
            for (int i = 1; i < lines.size(); i++) {
                String[] tokens = lines.get(i).split(",", -1);
                if (tokens.length < 6) continue;

                String id = tokens[0];
                String name = unescape(tokens[1]);
                String email = unescape(tokens[2]);
                char[] password = unescape(tokens[3]).toCharArray();
                String type = tokens[4];
                String extra = unescape(tokens[5]);

                User user;
                if ("std".equalsIgnoreCase(type)) {
                    user = new Student.StudentBuilder()
                            .name(name).email(email).password(password)
                            .borrowLimit(Integer.parseInt(extra)).build();
                } else if ("lib".equalsIgnoreCase(type)) {
                    user = new Librarian.LibrarianBuilder()
                            .name(name).email(email).password(password)
                            .employeeCode(extra).build();
                } else {
                    continue;
                }

                // Inject ID via reflection
                Field idField = User.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(user, id);

                users.put(user.getId(), user);
            }

            System.out.println(" Users loaded from CSV.");
        } catch (Exception e) {
            System.out.println(" Failed to load users from CSV: " + e.getMessage());
        }
    }


    // --- Utility methods ---

    private String escape(String s) {
        return s.replace(",", "%2C");
    }

    private String unescape(String s) {
        return s.replace("%2C", ",");
    }

    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text + " ".repeat(Math.max(0, width - padding - text.length()));
    }

    private String padRight(String text, int width) {
        return text + " ".repeat(Math.max(0, width - text.length()));
    }

    private void printHeader(String title) {
        System.out.println(CYAN + "+" + "-".repeat(BOX_WIDTH - 2) + "+" + RESET);
        System.out.println(CYAN + "|" + centerText(title, BOX_WIDTH - 2) + "|" + RESET);
        System.out.println(CYAN + "+" + "-".repeat(BOX_WIDTH - 2) + "+" + RESET);
    }

    private void printFooter() {
        System.out.println(CYAN + "+" + "-".repeat(BOX_WIDTH - 2) + "+" + RESET);
    }
}
