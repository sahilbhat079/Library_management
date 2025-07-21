package com.company.MultiModule.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LibrarianTest {

    @Test
    void testLibrarianBuilderAndFields() {
        Librarian librarian = new Librarian.LibrarianBuilder()
                .name("Bob")
                .email("bob@example.com")
                .password("libpass")
                .employeeCode("EMP101")
                .build();

        assertNotNull(librarian.getId());
        assertEquals("Bob", librarian.getName());
        assertEquals("bob@example.com", librarian.getEmail());
        assertEquals("EMP101", librarian.getEmployeeCode());
        assertTrue(librarian.toString().contains("Librarian, code=EMP101"));
    }

    @Test
    void testLibrarianEqualityAndHashCode() {
        Librarian l1 = new Librarian.LibrarianBuilder()
                .name("Bob")
                .email("bob@example.com")
                .password("123")
                .employeeCode("EMP1")
                .build();

        Librarian l2 = new Librarian.LibrarianBuilder()
                .name("Bob")
                .email("bob@example.com")
                .password("123")
                .employeeCode("EMP1")
                .build();

        assertNotEquals(l1, l2); // unique IDs
        assertEquals(l1, l1);
        assertEquals(l1.hashCode(), l1.hashCode());
    }
}
