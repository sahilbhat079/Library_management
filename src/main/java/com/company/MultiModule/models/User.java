package com.company.MultiModule.models;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public abstract class User  {
    private final String id;
    private final String name;
    private final String email;
    private final String password;

    protected User(UserBuilder<?> builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.email = builder.email;
        this.password = builder.password;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }  // 🔐 Expose carefully (maybe not in real apps)

    @Override
    public String toString() {
        return String.format("User{id='%s', name='%s', email='%s'}", id, name, email);
        // 🔒 Avoid showing password
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

    // Generic Builder
    public static abstract class UserBuilder<T extends UserBuilder<T>> {
        private final String id = UUID.randomUUID().toString();
        private String name;
        private String email;
        private String password;

        public T name(String name) {
            this.name = name;
            return self();
        }

        public T email(String email) {
            this.email = email;
            return self();
        }

        public T password(String password) {
            this.password = password;
            return self();
        }

        protected abstract T self();
        public abstract User build();
    }
}
