package com.company.MultiModule.services;

import com.company.MultiModule.models.Book;
import com.company.MultiModule.models.Student;
import com.company.MultiModule.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportServiceTest {

    private final ReportService reportService = ReportService.getInstance();

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testGenerateReport_PrintsCorrectly() {
        Book book = new Book.Builder<>()
                .title("Domain-Driven Design")
                .author("Eric Evans")
                .category("Software Engineering")
                .isbn("DDD123")
                .build();

        User user = new Student.StudentBuilder()
                .name("Test User")
                .email("test@example.com")
                .password("secret")
                .borrowLimit(1)
                .build();

        reportService.generateReport(List.of(book), List.of(user));

        String output = outContent.toString();

        // Basic assertions to ensure key parts exist in output
        assertTrue(output.contains("ADMIN REPORT"));
        assertTrue(output.contains("Books Report"));
        assertTrue(output.contains("Users Report"));
        assertTrue(output.contains("Domain-Driven Design"));
        assertTrue(output.contains("Test User"));
        assertTrue(output.contains("Reflection"));
    }

    @Test
    void testGenerateReport_WithEmptyLists() {
        reportService.generateReport(List.of(), List.of());
        String output = outContent.toString();

        assertTrue(output.contains("Books Report"));
        assertTrue(output.contains("Users Report"));
        assertTrue(output.contains("Report generated using Java Reflection."));
    }
}
