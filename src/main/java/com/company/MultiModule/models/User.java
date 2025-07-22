package com.company.MultiModule.models;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public abstract class User {
    private final String id;
    private final String name;
    private final String email;
    private final char[] password;

    protected User(UserBuilder<?> builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.email = builder.email;
        this.password = builder.password != null ? builder.password.clone() : null;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    // Return a defensive copy
    public char[] getPassword() {
        return password != null ? password.clone() : null;
    }

    @Override
    public String toString() {
        return String.format("User{id='%s', name='%s', email='%s'}", id, name, email);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static abstract class UserBuilder<T extends UserBuilder<T>> {
        private final String id = UUID.randomUUID().toString();
        private String name;
        private String email;
        private char[] password;

        public T name(String name) {
            this.name = name;
            return self();
        }

        public T email(String email) {
            this.email = email;
            return self();
        }

        public T password(char[] password) {
            this.password = password != null ? password.clone() : null;
            return self();
        }

        protected abstract T self();
        public abstract User build();
    }

    // Allow zeroing out the password (utility method)
    public void clearPassword() {
        if (password != null) Arrays.fill(password, '\0');
    }
}
