package com.kanok.inserter.service;

import com.github.javafaker.Faker;
import com.kanok.inserter.model.User;
import fabricator.Contact;
import fabricator.Fabricator;
import io.codearte.jfairy.Fairy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import static com.kanok.inserter.constants.ImportConstants.TOTAL_ARTICLE_TYPE;
import static com.kanok.inserter.constants.ImportConstants.TOTAL_USER;

public class FakeDataService {

    //faker.lorem().fixedString(UtilsService.splittableRandom.nextInt(500, 5000)
    //String name = faker.medical().diseaseName();
    private final SplittableRandom random;
    private final Faker faker;
    private final Fairy fairy;

    public FakeDataService() {
        this.random = new SplittableRandom();
        this.faker = new Faker();
        this.fairy = Fairy.create();
    }

    public List<User> createUsers() {
        List<User> users = new ArrayList<>();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        for (int i = 0; i < TOTAL_USER; i++) {
            Contact person = Fabricator.contact();
            String firstName = person.firstName();
            String lastName = person.lastName();
            String email = person.eMail();
            String username = email.substring(0, email.lastIndexOf("@"));
            String encodePassword = bCryptPasswordEncoder.encode(username);
            String aboutMe = "Address: " + person.address() + " Born: " + person.birthday(random.nextInt(100)) + " Company: " + person.company() + " Phone number: " + person.phoneNumber();
            users.add(new User(firstName, lastName, username, email, encodePassword, aboutMe));
        }
        return users;
    }

    public List<String> createArticleTypes() {
        List<String> articleTypes = new ArrayList<>();
        for (int i = 0; i < TOTAL_ARTICLE_TYPE; i++) {

        }
        return articleTypes;
    }
}
