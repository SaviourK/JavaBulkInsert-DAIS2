package com.kanok.inserter;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.SplittableRandom;

public class DataInsertJob {

    private static final int TOTAL_USER = 1000;
    private static final int TOTAL_ARTICLE_TYPE = 50;
    private static final int TOTAL_ARTICLE = 300000;
    private static final int TOTAL_CATEGORY = 10;
    private static final int TOTAL_CATEGORY_ADMIN = 20;
    private static final int TOTAL_TOPIC = 50000;
    private static final int TOTAL_POST = 5000000;
    private static final int TOTAL_TOPIC_WATCHING_USER = 50000;

    public void run(String connectionUrl) {
        try (Connection connection = DriverManager.getConnection(connectionUrl)) {
            bulkInsert(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO stop indexing and constraints?
    private void bulkInsert(Connection connection) throws SQLException, NoSuchAlgorithmException {
        int expectedTotalInserted = TOTAL_USER + TOTAL_ARTICLE_TYPE + TOTAL_ARTICLE + TOTAL_CATEGORY + TOTAL_CATEGORY_ADMIN + TOTAL_TOPIC + TOTAL_POST + TOTAL_TOPIC_WATCHING_USER;
        System.out.println("Start bulk insert. Expected total inserted rows: " + expectedTotalInserted);

        long start = System.currentTimeMillis();
        Timestamp insertTime = new Timestamp(start);
        int totalInserted = 0;

        connection.setAutoCommit(false);

        //TODO add images insert
        //insertImages(connection);
        Faker faker = new Faker();
        SplittableRandom splittableRandom = new SplittableRandom();

        // Insert users START
        BulkInserter bulkInserter = new BulkInserter("users", insertTime, TOTAL_USER, connection) {
            @Override
            public String createQuery() {
                return prepareQuery(
                        COL_NAME_ID, COL_NAME_CREATE_DATE_TIME, COL_NAME_UPDATE_DATE_TIME,
                        "about_us", "enabled", "token_expired",
                        "role",
                        "post_count", "topic_count", "article_count",
                        "first_name", "last_name", "username", "email", "password", "about_me");
            }

            @Override
            protected void setStatementData(PreparedStatement preparedStatement, long id) throws SQLException {
                setStatementsIdCreatedUpdated(preparedStatement, id);
                preparedStatement.setBoolean(4, false);
                preparedStatement.setBoolean(5, true);
                preparedStatement.setBoolean(6, true);
                int role;
                if (id <= 5) {
                    // ROLE ADMIN
                    role = 1;
                } else if (id <= 50) {
                    // ROLE SPECIALISTS
                    role = 2;
                } else {
                    // ROLE USER
                    role = 3;
                }
                preparedStatement.setInt(7, role); // role
                preparedStatement.setLong(8, 0L); // post_count
                preparedStatement.setLong(9, 0L); // topic_count
                preparedStatement.setLong(10, 0L); // article_count
                Name name = faker.name();
                preparedStatement.setString(11, name.firstName()); //first_name
                preparedStatement.setString(12, name.lastName()); //last_name
                preparedStatement.setString(13, name.username()); //username
                preparedStatement.setString(14, name.lastName() + "." + name.firstName() + "@gmail.com"); //email
                preparedStatement.setString(15,  encoder.encode(name.firstName() + name.lastName())); //password
                preparedStatement.setString(16, "about_me"); //about_me
            }
        };
        bulkInserter.setFaker(faker);
        bulkInserter.setSplittableRandom(splittableRandom);
        bulkInserter.setEncoder(new BCryptPasswordEncoder());
        totalInserted += bulkInserter.insert();
        // Insert users END
        // Insert article_type START
        bulkInserter = new BulkInserter("article_type", insertTime, TOTAL_ARTICLE_TYPE, connection) {
            @Override
            public String createQuery() {
                return prepareQuery(
                        COL_NAME_ID, COL_NAME_CREATE_DATE_TIME, COL_NAME_UPDATE_DATE_TIME,
                        COL_NAME_NAME);
            }

            @Override
            protected void setStatementData(PreparedStatement preparedStatement, long id) throws SQLException {
                setStatementsIdCreatedUpdated(preparedStatement, id);
                preparedStatement.setString(4, "article" + id); //name
            }
        };
        totalInserted += bulkInserter.insert();
        // Insert article_type END

        // Insert article START
        bulkInserter = new BulkInserter("article", insertTime, TOTAL_ARTICLE, connection) {
            @Override
            public String createQuery() {
                return prepareQuery(
                        COL_NAME_ID, COL_NAME_CREATE_DATE_TIME, COL_NAME_UPDATE_DATE_TIME,
                        COL_NAME_USER_ID, "article_type_id",
                        COL_NAME_NAME, COL_NAME_URL, COL_NAME_TEXT);
            }

            @Override
            protected void setStatementData(PreparedStatement preparedStatement, long id) throws SQLException {
                setStatementsIdCreatedUpdated(preparedStatement, id);
                //TODO set random USERID
                preparedStatement.setLong(4, 1); //users_id
                //TODO set random article type
                preparedStatement.setLong(5, 1); //article_type_id
                preparedStatement.setString(6, "colName" + id); //name
                //TODO create URL
                preparedStatement.setString(7, "colURL" + id); //url
                preparedStatement.setString(8, "colTexta"); //text
            }
        };
        totalInserted += bulkInserter.insert();
        // Insert article END

        // Insert category START
        bulkInserter = new BulkInserter("category", insertTime, TOTAL_CATEGORY, connection) {
            @Override
            public String createQuery() {
                return prepareQuery(
                        COL_NAME_ID, COL_NAME_CREATE_DATE_TIME, COL_NAME_UPDATE_DATE_TIME,
                        COL_NAME_USER_ID,
                        COL_NAME_NAME, COL_NAME_URL);
            }

            @Override
            protected void setStatementData(PreparedStatement preparedStatement, long id) throws SQLException {
                setStatementsIdCreatedUpdated(preparedStatement, id);
                //TODO set random USERID
                preparedStatement.setLong(4, 1);
                preparedStatement.setString(5, "category_name" + id);
                //TODO createurl by name
                preparedStatement.setString(6, "url" + id);
            }
        };
        totalInserted += bulkInserter.insert();
        // Insert category END

        // Insert category_admin START
        bulkInserter = new BulkInserter("category_admin", insertTime, TOTAL_CATEGORY_ADMIN, connection) {
            @Override
            public String createQuery() {
                return prepareQuery(
                        COL_NAME_CATEGORY_ID, COL_NAME_USER_ID);
            }

            @Override
            protected void setStatementData(PreparedStatement preparedStatement, long id) throws SQLException {
                //TODO set random category_id
                preparedStatement.setLong(1, 1);
                //TODO set random user_id
                preparedStatement.setLong(2, 1);
            }
        };
        totalInserted += bulkInserter.insert();
        // Insert category_admin END

        // Insert topic START
        bulkInserter = new BulkInserter("topic", insertTime, TOTAL_TOPIC, connection) {
            @Override
            public String createQuery() {
                return prepareQuery(
                        COL_NAME_ID, COL_NAME_CREATE_DATE_TIME, COL_NAME_UPDATE_DATE_TIME,
                        COL_NAME_CATEGORY_ID, COL_NAME_USER_ID,
                        COL_NAME_NAME, COL_NAME_TEXT);
            }

            @Override
            protected void setStatementData(PreparedStatement preparedStatement, long id) throws SQLException {
                setStatementsIdCreatedUpdated(preparedStatement, id);
                //TODO set random category_id
                preparedStatement.setLong(4, 1);
                //TODO set random user_id
                preparedStatement.setLong(5, 1);
                preparedStatement.setString(6, "topic" + id);
                preparedStatement.setString(7, "topic_text" + id);
            }
        };
        totalInserted += bulkInserter.insert();
        // Insert topic END

        // Insert post START
        bulkInserter = new BulkInserter("post", insertTime, TOTAL_POST, connection) {
            @Override
            public String createQuery() {
                return prepareQuery(
                        COL_NAME_ID, COL_NAME_CREATE_DATE_TIME, COL_NAME_UPDATE_DATE_TIME,
                        COL_NAME_TOPIC_ID, COL_NAME_USER_ID,
                        COL_NAME_TEXT);
            }

            @Override
            protected void setStatementData(PreparedStatement preparedStatement, long id) throws SQLException {
                setStatementsIdCreatedUpdated(preparedStatement, id);
                //TODO set random topic_id
                preparedStatement.setLong(4, 1);
                //TODO set random user_id
                preparedStatement.setLong(5, 1);
                preparedStatement.setString(6, "post_text" + id);
            }
        };
        totalInserted += bulkInserter.insert();
        // Insert post END

        // Insert topic_watching_user START
        bulkInserter = new BulkInserter("topic_watching_user", insertTime, TOTAL_TOPIC_WATCHING_USER, connection) {
            @Override
            public String createQuery() {
                return prepareQuery(
                        COL_NAME_TOPIC_ID, COL_NAME_USER_ID);
            }

            @Override
            protected void setStatementData(PreparedStatement preparedStatement, long id) throws SQLException {
                //TODO set random topic_id
                preparedStatement.setLong(1, 1);
                //TODO set random user_id
                preparedStatement.setLong(2, 1);
            }
        };
        totalInserted += bulkInserter.insert();
        // Insert topic_watching_user END


        //preparedStatement.close();
        //TODO UPDATE USERS COUNT POSTs, TOPICs, ARTICLES
        connection.close();
        long end = System.currentTimeMillis();
        System.out.println("End bulk insert. Total time taken: " + (end - start) / 1000 + " s" + " Total inserted: " + totalInserted);
    }

}
