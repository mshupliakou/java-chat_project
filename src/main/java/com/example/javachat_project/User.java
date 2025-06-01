package com.example.javachat_project;

/**
 * Represents a user in the chat application.
 * Stores user information such as ID, name, login, and password.
 */
public class User {

    // Unique identifier for the user
    private long id;

    // User's first name
    private String name;

    // User's last name (note: renamed to follow Java naming conventions)
    private String lastName;

    // User's password (consider hashing it instead of storing plain text)
    private String password;

    // User's login name (used for authentication)
    private String login;

    /**
     * Constructs a User object with the specified fields.
     *
     * @param id        the user's ID
     * @param name      the user's first name
     * @param lastName  the user's last name
     * @param password  the user's password (plaintext — should be hashed in real apps)
     * @param login     the user's login name
     */
    public User(long id, String name, String lastName, String password, String login) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.password = password;
        this.login = login;
    }

    // Default no-args constructor (useful for serialization and frameworks like Hibernate)
    public User() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Returns the user's password.
     * ⚠️ WARNING: Exposing passwords in plain text is a security risk.
     * Consider removing this method or returning a masked/hashed value instead.
     */
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns a string representation of the user (excluding sensitive information like password).
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", login='" + login + '\'' +
                '}';
    }

    // Optionally override equals() and hashCode() if needed for user comparison or collections
}
