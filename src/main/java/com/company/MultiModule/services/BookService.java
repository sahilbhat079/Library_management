package com.company.MultiModule.services;

import com.company.MultiModule.Repository.CsvStorage;
import com.company.MultiModule.exceptions.LibraryException;
import com.company.MultiModule.models.Book;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class BookService {

    private static final String BOOK_CSV_FILE = "data/books.csv";


    private static final BookService instance = new BookService();
    private final Map<String, Book> books = new HashMap<>();

    private BookService() {
        // Dummy data
        Book book = new Book.Builder<>()
                .title("Clean Code")
                .author("Robert C. Martin")
                .category("Programming")
                .isbn("1234567")
                .build();
        books.put(book.getId(), book);
    }

    public static BookService getInstance() {
        return instance;
    }

    public List<Book> listAllBooks() {
        return new ArrayList<>(books.values());
    }

    public List<Book> search(String keyword) {
        String k = keyword.toLowerCase();
        return books.values().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(k)
                        || b.getAuthor().toLowerCase().contains(k))
                .collect(Collectors.toList());
    }

    public void addBook(Book book) {
        books.put(book.getId(), book);
    }

    public Book findById(String id) {
        return books.get(id);
    }

    public boolean isAvailable(String id) {
        Book b = books.get(id);
        return b != null && b.isAvailable();
    }

    public void setAvailability(String id, boolean available) {
        Book b = books.get(id);
        if (b != null) b.setAvailable(available);
    }

    public boolean existsByIsbn(String isbn) {
        return books.values().stream()
                .anyMatch(b -> b.getIsbn().equalsIgnoreCase(isbn));
    }

    public Book findByIsbn(String isbn) throws LibraryException {
        return books.values().stream()
                .filter(b -> b.getIsbn().equalsIgnoreCase(isbn))
                .findFirst()
                .orElseThrow(() -> new LibraryException("Book with ISBN not found: " + isbn));
    }


    public boolean exists(String id) {
        return books.containsKey(id);
    }


    // file utility



    public void saveToCsv() {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("id,isbn,title,author,category,available");

            for (Book b : books.values()) {
                lines.add(String.join(",",
                        b.getId(),
                        escape(b.getIsbn()),
                        escape(b.getTitle()),
                        escape(b.getAuthor()),
                        escape(b.getCategory()),
                        String.valueOf(b.isAvailable())
                ));
            }

            CsvStorage.save(BOOK_CSV_FILE, lines);
            System.out.println(" Books saved to CSV.");
        } catch (IOException e) {
            System.out.println(" Failed to save books to CSV: " + e.getMessage());
        }
    }

    public void loadFromCsv() {
        try {
            List<String> lines = CsvStorage.load(BOOK_CSV_FILE);
            if (lines.size() <= 1) return; // no data

            books.clear();
            for (int i = 1; i < lines.size(); i++) {
                String[] tokens = lines.get(i).split(",", -1);
                if (tokens.length < 6) continue;

                Book book = new Book.Builder<>()
                        .isbn(unescape(tokens[1]))
                        .title(unescape(tokens[2]))
                        .author(unescape(tokens[3]))
                        .category(unescape(tokens[4]))
                        .available(Boolean.parseBoolean(tokens[5]))
                        .build();

                // Inject the original ID using reflection
                Field idField = Book.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(book, tokens[0]);

                books.put(book.getId(), book);
            }

            System.out.println(" Books loaded from CSV.");
        } catch (Exception e) {
            System.out.println(" Failed to load books from CSV: " + e.getMessage());
        }
    }



    private String escape(String s) {
        return s.replace(",", "%2C"); // basic CSV escaping
    }

    private String unescape(String s) {
        return s.replace("%2C", ",");
    }

}
