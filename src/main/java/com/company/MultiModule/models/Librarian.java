package com.company.MultiModule.models;


import java.io.Serializable;

public class Librarian extends User {
    private final String employeeCode;

    private Librarian(LibrarianBuilder builder) {
        super(builder);
        this.employeeCode = builder.employeeCode;
    }

    public String getEmployeeCode() { return employeeCode; }

    @Override
    public String toString() {
        return super.toString() + " [Librarian, code=" + employeeCode + "]";
    }

    public static class LibrarianBuilder extends UserBuilder<LibrarianBuilder> {
        private String employeeCode;

        public LibrarianBuilder employeeCode(String code) {
            this.employeeCode = code;
            return this;
        }

        @Override
        protected LibrarianBuilder self() {
            return this;
        }

        @Override
        public Librarian build() {
            return new Librarian(this);
        }
    }
}