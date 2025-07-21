package com.company.MultiModule.models;


import java.io.Serializable;

public class Student extends User   {
    private final int borrowLimit;

    private Student(StudentBuilder builder) {
        super(builder);
        this.borrowLimit = builder.borrowLimit;
    }

    public int getBorrowLimit() { return borrowLimit; }

    @Override
    public String toString() {
        return super.toString() + " [Student, borrowLimit=" + borrowLimit + "]";
    }

    public static class StudentBuilder extends UserBuilder<StudentBuilder> {
        private int borrowLimit = 3; // default

        public StudentBuilder borrowLimit(int borrowLimit) {
            this.borrowLimit = borrowLimit;
            return this;
        }

        @Override
        protected StudentBuilder self() {
            return this;  // Downcast to subclass type
        }

        @Override
        public Student build() {
            return new Student(this);
        }
    }
}