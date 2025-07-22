package com.company.MultiModule.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentTest {

    @Test
    void testStudentBuilderAndFields() {
        Student student = new Student.StudentBuilder()
                .name("Alice")
                .email("alice@example.com")
                .password("pass123".toCharArray())
                .borrowLimit(5)
                .build();

        assertNotNull(student.getId());
        assertEquals("Alice", student.getName());
        assertEquals("alice@example.com", student.getEmail());
        assertEquals(5, student.getBorrowLimit());
        assertEquals("User{id='" + student.getId() + "', name='Alice', email='alice@example.com'} [Student, borrowLimit=5]",
                student.toString());
    }

    @Test
    void testStudentEqualityAndHashCode() {
        Student s1 = new Student.StudentBuilder()
                .name("Test")
                .email("t@example.com")
                .password("123".toCharArray())
                .build();

        Student s2 = new Student.StudentBuilder()
                .name("Test")
                .email("t@example.com")
                .password("123".toCharArray())
                .build();

        assertNotEquals(s1, s2); // different IDs
        assertEquals(s1, s1);    // same object
        assertEquals(s1.hashCode(), s1.hashCode());
    }
}
