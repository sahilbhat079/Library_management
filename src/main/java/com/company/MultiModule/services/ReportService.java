package com.company.MultiModule.services;

import com.company.MultiModule.models.Book;
import com.company.MultiModule.models.User;

import java.lang.reflect.Field;
import java.util.List;

public class ReportService {

    private static final ReportService instance = new ReportService();

    private ReportService() {}

    public static ReportService getInstance() {
        return instance;
    }

    public void generateReport(List<Book> books, List<User> users) {
        System.out.println();
        System.out.println("========== ADMIN REPORT ==========\n");

        System.out.println("------ Books Report ------");
        for (Book book : books) {
            printObjectDetails(book);
            System.out.println("------------------------------");
        }

        System.out.println("\n------ Users Report ------");
        for (User user : users) {
            printObjectDetails(user);
            System.out.println("------------------------------");
        }

        System.out.println("\nReport generated using Java Reflection.");
        System.out.println("======================================\n");
    }

    private void printObjectDetails(Object obj) {
        Class<?> clazz = obj.getClass();
        System.out.println("Class: " + clazz.getSimpleName());

        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    System.out.printf("  %-20s : %s%n", field.getName(), value);
                } catch (IllegalAccessException e) {
                    System.out.printf("  %-20s : [access denied]%n", field.getName());
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
}
