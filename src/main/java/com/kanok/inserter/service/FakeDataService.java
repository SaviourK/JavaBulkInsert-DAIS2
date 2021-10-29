package com.kanok.inserter.service;

import com.github.javafaker.Faker;
import com.kanok.inserter.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static com.kanok.inserter.constants.ImportConstants.TOTAL_USER;

public class FakeDataService {

    //faker.lorem().fixedString(UtilsService.splittableRandom.nextInt(500, 5000)
    //String name = faker.medical().diseaseName();
    private final Faker faker;

    public FakeDataService() {
        this.faker = new Faker();
    }

    public List<User> createFakeUsers() {
        List<User> users = new ArrayList<>();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        for (int i = 0; i < TOTAL_USER; i++) {
            String fullName = faker.name().fullName();
            String firstName = fullName.substring(fullName.indexOf(' '));
            String lastName = fullName.substring(fullName.lastIndexOf(' '));
            String username = faker.name().username();
            String email = firstName + "." + lastName + "@gmail.com";
            String encodePassword = bCryptPasswordEncoder.encode(username);
            String aboutMe = "about_me";
            users.add(new User(firstName, lastName, username, email, encodePassword, aboutMe));
        }
        return users;
    }
}
