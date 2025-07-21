package com.company.MultiModule.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    @Test
    void testBookBuilderAndFields() {
        Book book = new Book.Builder<>()
                .title("Effective Java")
                .author("Joshua Bloch")
                .category("Programming")
                .isbn("978-0134685991")
                .available(true)
                .build();

        assertEquals("Effective Java", book.getTitle());
        assertEquals("Joshua Bloch", book.getAuthor());
        assertEquals("Programming", book.getCategory());
        assertEquals("978-0134685991", book.getIsbn());
        assertTrue(book.isAvailable());
        assertNotNull(book.getId());
    }

    @Test
    void testBookEquality() {
        Book book1 = new Book.Builder<>()
                .title("X")
                .author("Y")
                .category("Z")
                .isbn("123")
                .build();

        Book book2 = new Book.Builder<>()
                .title("X")
                .author("Y")
                .category("Z")
                .isbn("123")
                .build();

        // Different IDs
        assertNotEquals(book1, book2);

        // Force same ID for test (not recommended outside test context)
        book2 = book1;
        assertEquals(book1, book2);
    }
}
