package com.company.MultiModule.services;

import com.company.MultiModule.models.Book;
import com.company.MultiModule.models.Student;
import com.company.MultiModule.models.User;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReportServiceTest {

    private ReportService reportService;
    private static final Path BOOK_REPORT_PATH = Paths.get("data/reports/book_report.csv");
    private static final Path USER_REPORT_PATH = Paths.get("data/reports/user_report.csv");

    @BeforeAll
    void setup() {
        reportService = ReportService.getInstance();
    }

    @AfterEach
    void cleanUp() throws IOException {
        Files.deleteIfExists(BOOK_REPORT_PATH);
        Files.deleteIfExists(USER_REPORT_PATH);
    }

    @Test
    void testGenerateBookReportCreatesCSV() throws IOException {
        Book book = new Book.Builder<>()
                .title("Effective Java")
                .author("Joshua Bloch")
                .isbn("123456")
                .category("Programming")
                .build();

        reportService.generateBookReport(List.of(book));

        assertTrue(Files.exists(BOOK_REPORT_PATH), "Book report CSV file should be created.");
        List<String> lines = Files.readAllLines(BOOK_REPORT_PATH);
        assertTrue(lines.size() >= 2, "Report should have header + data.");
        assertTrue(lines.get(1).contains("Effective Java"));
    }

    @Test
    void testGenerateUserReportCreatesCSV() throws IOException {
        User student = new Student.StudentBuilder()
                .name("Alice")
                .email("alice@example.com")
                .password("pass123".toCharArray())
                .borrowLimit(3)
                .build();

        reportService.generateUserReport(List.of(student));

        assertTrue(Files.exists(USER_REPORT_PATH), "User report CSV file should be created.");
        List<String> lines = Files.readAllLines(USER_REPORT_PATH);
        assertTrue(lines.size() >= 2, "Report should have header + user data.");
        assertTrue(lines.get(1).contains("Alice"));
    }
}
