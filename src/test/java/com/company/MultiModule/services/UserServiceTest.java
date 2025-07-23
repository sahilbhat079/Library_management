package com.company.MultiModule.services;

import com.company.MultiModule.exceptions.UserNotFound;
import com.company.MultiModule.models.Librarian;
import com.company.MultiModule.models.Student;
import com.company.MultiModule.models.User;
import org.junit.jupiter.api.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private static final String TEST_EMAIL = "csv@test.com";

    @BeforeEach
    void setUp() {
        userService = UserService.getInstance();

        // Clean old users
        userService.getAllUsers().clear();
    }

    @AfterEach
    void tearDown() {
        // Clean CSV file after each test
        File file = new File("data/users.csv");
        if (file.exists()) {
            file.delete();
        }

        userService.getAllUsers().clear();
    }

    @Test
    void testLoginSuccess() throws UserNotFound {
        Student student = new Student.StudentBuilder()
                .name("sahil")
                .email("sahil@student.com")
                .password("pass123".toCharArray())
                .borrowLimit(2)
                .build();
        userService.addUser(student);

        User user = userService.login("sahil", "pass123".toCharArray());
        assertNotNull(user);
        assertEquals("sahil", user.getName());
    }

    @Test
    void testLoginFailure() {
        assertThrows(UserNotFound.class, () -> userService.login("wrong", "password".toCharArray()));
    }

    @Test
    void testAddAndFindUserById() throws UserNotFound {
        Student student = new Student.StudentBuilder()
                .name("Test Student")
                .email("test@student.com")
                .password("test123".toCharArray())
                .borrowLimit(2)
                .build();

        userService.addUser(student);

        User found = userService.findUserById(student.getId());
        assertEquals("Test Student", found.getName());
    }

    @Test
    void testFindUserById_NotFound() {
        assertThrows(UserNotFound.class, () -> userService.findUserById("non-existent-id"));
    }

    @Test
    void testFindByEmail() {
        Student student = new Student.StudentBuilder()
                .name("Email Test")
                .email("sahil@student.com")
                .password("email123".toCharArray())
                .borrowLimit(2)
                .build();

        userService.addUser(student);

        User user = userService.findByEmail("sahil@student.com");
        assertNotNull(user);
        assertEquals("Email Test", user.getName());
    }

    @Test
    void testFindByEmailNotFound() {
        assertNull(userService.findByEmail("not@exist.com"));
    }

    @Test
    void testGetAllUsers() {
        Student student = new Student.StudentBuilder()
                .name("Dummy")
                .email("dummy@x.com")
                .password("123".toCharArray())
                .borrowLimit(1)
                .build();
        userService.addUser(student);

        Map<String, User> users = userService.getAllUsers();
        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
    }

    @Test
    void testSaveAndLoadFromCsv() {
        // Add a dummy user
        Student dummy = new Student.StudentBuilder()
                .name("CSV Test")
                .email(TEST_EMAIL)
                .password("test123".toCharArray())
                .borrowLimit(3)
                .build();
        userService.addUser(dummy);

        // Save to CSV
        userService.saveToCsv();
        File file = new File("data/users.csv");
        assertTrue(file.exists(), "CSV file should exist");

        // Clear in-memory and reload from file
        userService.getAllUsers().clear();
        userService.loadFromCsv();

        User loaded = userService.findByEmail(TEST_EMAIL);
        assertNotNull(loaded);
        assertEquals("CSV Test", loaded.getName());
    }
}
