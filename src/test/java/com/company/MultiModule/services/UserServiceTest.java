package com.company.MultiModule.services;


import com.company.MultiModule.exceptions.UserNotFound;
import com.company.MultiModule.models.Librarian;
import com.company.MultiModule.models.Student;
import com.company.MultiModule.models.User;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = UserService.getInstance();
    }

    @Test
    void testLoginSuccess() throws UserNotFound {
        User user = userService.login("sahil", "pass123");
        assertNotNull(user);
        assertEquals("sahil", user.getName());
    }

    @Test
    void testLoginFailure() {
        assertThrows(UserNotFound.class, () -> userService.login("wrong", "password"));
    }

    @Test
    void testAddAndFindUserById() throws UserNotFound {
        Student student = new Student.StudentBuilder()
                .name("Test Student")
                .email("test@student.com")
                .password("test123")
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
        User user = userService.findByEmail("sahil@student.com");
        assertNotNull(user);
        assertEquals("sahil", user.getName());
    }

    @Test
    void testFindByEmailNotFound() {
        assertNull(userService.findByEmail("not@exist.com"));
    }

    @Test
    void testGetAllUsers() {
        Map<String, User> users = userService.getAllUsers();
        assertFalse(users.isEmpty());
    }

    @Test
    void testSaveAndLoadFromCsv() {
        userService.saveToCsv();
        File file = new File("data/users.csv");
        assertTrue(file.exists());

        userService.loadFromCsv();  // Should not crash or throw
        assertNotNull(userService.findByEmail("sahil@student.com"));
    }
}
