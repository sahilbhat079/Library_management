package com.company.MultiModule.services;


import com.company.MultiModule.Repository.CsvStorage;
import com.company.MultiModule.exceptions.UserNotFound;
import com.company.MultiModule.models.Librarian;
import com.company.MultiModule.models.Student;
import com.company.MultiModule.models.User;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {
    private static final String USER_CSV_FILE = "data/users.csv";

    private static final UserService instance = new UserService();
    private final Map<String, User> users = new HashMap<>();

    private UserService() {
        //  Add dummy Librarian
        Librarian librarian = new Librarian.LibrarianBuilder()
                .name("admin")
                .email("lib@library.com")
                .password("admin123")
                .employeeCode("LIB-001")
                .build();
        users.put(librarian.getId(), librarian);

        //  Add dummy Student
        Student student = new Student.StudentBuilder()
                .name("sahil")
                .email("sahil@student.com")
                .password("pass123")
                .borrowLimit(3)
                .build();
        users.put(student.getId(), student);

        // Debug output
        System.out.println("Dummy users loaded:");
        users.values().forEach(u ->
                System.out.println("  Username: " + u.getName() + ", Password: " + u.getPassword() + ", Role: " + u.getClass().getSimpleName()));
    }

    public static UserService getInstance() {
        return instance;
    }

    /**
     * Authenticate user by name and password
     */
    public User login(String name, String password) throws UserNotFound {
        return users.values().stream()
                .filter(u -> u.getName().equalsIgnoreCase(name) && u.getPassword().equals(password))
                .findFirst()
                .orElseThrow(() -> new UserNotFound(name));
    }

    /**
     * Add new user (librarian-only action)
     */
    public void addUser(User user) {
        users.put(user.getId(), user);
    }

    /**
     * Find user by system ID
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




    // for the report
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
                    continue; // skip unknown type
                }

                lines.add(String.join(",",
                        user.getId(),
                        escape(user.getName()),
                        escape(user.getEmail()),
                        escape(user.getPassword()),
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
                String password = unescape(tokens[3]);
                String type = tokens[4];
                String extra = unescape(tokens[5]);

                User user;

                if ("std".equalsIgnoreCase(type)) {
                    user = new Student.StudentBuilder()
                            .name(name)
                            .email(email)
                            .password(password)
                            .borrowLimit(Integer.parseInt(extra))
                            .build();
                } else if ("lib".equalsIgnoreCase(type)) {
                    user = new Librarian.LibrarianBuilder()
                            .name(name)
                            .email(email)
                            .password(password)
                            .employeeCode(extra)
                            .build();
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




    private String escape(String s) {
        return s.replace(",", "%2C"); // basic CSV escaping
    }

    private String unescape(String s) {
        return s.replace("%2C", ",");
    }



}
