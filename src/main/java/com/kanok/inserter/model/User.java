package com.kanok.inserter.model;

public class User {

    private final String firstName;
    private final String lastName;
    private final String username;
    private final String email;
    private final String hashedPassword;
    private final String aboutMe;
    private final boolean aboutUs;
    private final boolean enabled;

    public User(String firstName, String lastName, String username, String email, String hashedPassword, String aboutMe, boolean aboutUs, boolean enabled) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.aboutMe = aboutMe;
        this.aboutUs = aboutUs;
        this.enabled = enabled;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public boolean isAboutUs() {
        return aboutUs;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
