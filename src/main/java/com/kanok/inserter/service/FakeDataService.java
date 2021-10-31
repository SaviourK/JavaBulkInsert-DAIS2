package com.kanok.inserter.service;

import com.github.javafaker.Faker;
import com.github.javafaker.Lorem;
import com.github.javafaker.Medical;
import com.kanok.inserter.model.Article;
import com.kanok.inserter.model.Category;
import com.kanok.inserter.model.User;
import fabricator.Contact;
import fabricator.Fabricator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import static com.kanok.inserter.constants.ImportConstants.*;
import static com.kanok.inserter.service.UtilsService.makeFriendlyUrl;

public class FakeDataService {

    private final String hashType;

    private final SplittableRandom random;
    private final Faker faker;

    private MessageDigest digest;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public FakeDataService(String hashType) {
        this.hashType = hashType;
        this.random = new SplittableRandom();
        this.faker = new Faker();
    }

    public List<User> createUsers() throws NoSuchAlgorithmException {
        long startTime = start(TABLE_NAME_USERS, TOTAL_USER);

        List<User> users = new ArrayList<>();
        for (int i = 0; i < TOTAL_USER; i++) {
            Contact person = Fabricator.contact();
            String firstName = person.firstName();
            String lastName = person.lastName();
            String email = person.eMail();
            String username = email.substring(0, email.lastIndexOf("@"));
            String encodePassword = hashPassword(username);
            String aboutMe = "Address: " + person.address() + " Born: " + person.birthday(random.nextInt(100)) + " Company: " + person.company() + " Phone number: " + person.phoneNumber();
            boolean aboutUs = geAboutAs(10);
            boolean enabled = geAboutAs(95);
            users.add(new User(firstName, lastName, username, email, encodePassword, aboutMe, aboutUs, enabled));
        }

        end(TABLE_NAME_USERS, TOTAL_USER, startTime);
        return users;
    }

    private boolean geAboutAs(int percent) {
        return random.nextInt(100) <= percent;
    }

    public List<String> createArticleTypes() {
        return ARTICLE_TYPE_NAMES;
    }

    public List<Article> createArticles() {
        long startTime = start(TABLE_NAME_ARTICLE, TOTAL_ARTICLE);

        List<Article> articles = new ArrayList<>();
        for (int i = 0; i < TOTAL_ARTICLE; i++) {
            String name = faker.company().name();
            String url = makeFriendlyUrl(name);
            String text = faker.lorem().fixedString(random.nextInt(1000, 8000));
            articles.add(new Article(name, url, text));
        }

        end(TABLE_NAME_ARTICLE, TOTAL_ARTICLE, startTime);
        return articles;
    }

    public List<Category> createCategories() {
        long startTime = start(TABLE_NAME_CATEGORY, TOTAL_CATEGORY);

        List<Category> categories = new ArrayList<>();
        for (String category : CATEGORY_NAMES) {
            String url = makeFriendlyUrl(category);
            categories.add(new Category(category, url));
        }

        end(TABLE_NAME_CATEGORY, TOTAL_CATEGORY, startTime);
        return categories;
    }

    public List<String> createTopics() {
        long startTime = start(TABLE_NAME_TOPIC, TOTAL_TOPIC);

        Medical medical = faker.medical();
        List<String> topics = new ArrayList<>();
        for (int i = 0; i < TOTAL_TOPIC; i++) {
            topics.add(medical.diseaseName());
        }

        end(TABLE_NAME_TOPIC, TOTAL_TOPIC, startTime);
        return topics;
    }

    public List<String> createText(int totalTexts, int minLength, int maxLength) {
        String text = "lorem ipsum text";
        long startTime = start(text, totalTexts);

        Lorem lorem = faker.lorem();
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < totalTexts; i++) {
            texts.add(lorem.fixedString(random.nextInt(minLength, maxLength)));
        }

        end(text, totalTexts, startTime);
        return texts;
    }

    private String hashPassword(String originalPassword) throws NoSuchAlgorithmException {
        if (hashType.equals(HASH_TYPE_SHA3_256)) {
            if (digest == null) {
                this.digest = MessageDigest.getInstance(HASH_TYPE_SHA3_256);
            }
            final byte[] hashBytes = digest.digest(
                    originalPassword.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } else if (hashType.equals(HASH_TYPE_BCRYPT)) {
            if (bCryptPasswordEncoder == null) {
                this.bCryptPasswordEncoder = new BCryptPasswordEncoder();
            }
            return bCryptPasswordEncoder.encode(originalPassword);
        } else {
            return originalPassword;
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private long start(String type, int total) {
        System.out.println("Creating fake " + type + " START. Total " + total);
        return System.currentTimeMillis();
    }

    private void end(String type, int total, long startTime) {
        long endTime = System.currentTimeMillis();
        System.out.println("Creating fake " + type + " END. Total " + total + ". Total time taken: " + (endTime - startTime) / 1000 + " s");
    }
}
