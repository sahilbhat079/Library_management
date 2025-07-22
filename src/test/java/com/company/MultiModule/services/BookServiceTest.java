package com.company.MultiModule.services;

import com.company.MultiModule.exceptions.LibraryException;
import com.company.MultiModule.models.Book;
import org.junit.jupiter.api.*;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookServiceTest {

    private static final BookService bookService = BookService.getInstance();
    private static final String TEMP_ISBN = "TEST-123";

    @BeforeEach
    void setup() {
        // Clean previous test data
        bookService.clearTestData();
    }

    @Test
    @Order(1)
    void testAddAndFindBook() {
        Book book = new Book.Builder<>()
                .title("Test Book")
                .author("Test Author")
                .category("Testing")
                .isbn(TEMP_ISBN)
                .build();

        bookService.addBook(book);

        Book found = bookService.findById(book.getId());
        assertNotNull(found);
        assertEquals("Test Book", found.getTitle());
    }

    @Test
    @Order(2)
    void testSearchBookByTitle() {
        bookService.addBook(new Book.Builder<>()
                .title("Java for Testers")
                .author("Test Author")
                .category("Programming")
                .isbn("TEST-456")
                .build());

        List<Book> results = bookService.search("Java");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(b -> b.getTitle().contains("Java")));
    }

    @Test
    @Order(3)
    void testAvailabilityCheckAndSet() {
        Book book = new Book.Builder<>()
                .title("Concurrent Java")
                .author("Concurrency Guru")
                .category("Java")
                .isbn("TEST-789")
                .build();

        bookService.addBook(book);
        assertTrue(bookService.isAvailable(book.getId()));

        bookService.setAvailability(book.getId(), false);
        assertFalse(bookService.isAvailable(book.getId()));
    }

    @Test
    @Order(4)
    void testFindByIsbn() throws LibraryException {
        String isbn = "TEST-FOUND";
        Book book = new Book.Builder<>()
                .title("Find Me")
                .author("Seeker")
                .category("Mystery")
                .isbn(isbn)
                .build();

        bookService.addBook(book);

        Book found = bookService.findByIsbn(isbn);
        assertNotNull(found);
        assertEquals("Find Me", found.getTitle());
    }

    @Test
    @Order(5)
    void testSaveAndLoadCsv() {
        Book book = new Book.Builder<>()
                .title("Persist Me")
                .author("File Writer")
                .category("Persistence")
                .isbn("TEST-PERSIST")
                .build();

        bookService.addBook(book);
        bookService.saveToCsv();

        // Clear and reload
        bookService.clearTestData();
        assertTrue(bookService.listAllBooks().stream()
                .noneMatch(b -> b.getIsbn().equals("TEST-PERSIST")));

        bookService.loadFromCsv();
        assertTrue(bookService.listAllBooks().stream()
                .anyMatch(b -> b.getIsbn().equals("TEST-PERSIST")));
    }

    @Test
    @Order(6)
    void testReflectionInjection() throws Exception {
        Book book = new Book.Builder<>()
                .title("Inject ID")
                .author("Reflectionist")
                .category("Advanced")
                .isbn("TEST-REFLECT")
                .build();

        Field idField = Book.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(book, "custom-id-123");

        assertEquals("custom-id-123", book.getId());
    }

    @Test
    @Order(7)
    void testExistsByIsbnAndExistsById() {
        Book book = new Book.Builder<>()
                .title("Exist Book")
                .author("Checker")
                .category("General")
                .isbn("TEST-EXISTS")
                .build();

        bookService.addBook(book);
        assertTrue(bookService.exists(book.getId()));
        assertTrue(bookService.existsByIsbn("TEST-EXISTS"));
    }
}
