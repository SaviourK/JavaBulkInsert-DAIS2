package com.kanok.inserter;

import com.github.javafaker.Faker;

import java.sql.*;
import java.util.SplittableRandom;

public class DataInsertJob {

    private static final int TOTAL_USERS = 1000;
    private static final int TOTAL_ARTICLE_TYPES = 50;
    private static final int TOTAL_ARTICLES = 300000;
    private static final int TOTAL_CATEGORIES = 10;
    private static final int TOTAL_CATEGORY_ADMINS = 20;
    private static final int TOTAL_TOPICS = 50000;
    private static final int TOTAL_POSTS = 5000000;
    private static final int TOTAL_TOPIC_WATCHING_USERS = 50000;


    private final String connectionUrl;
    private final Faker faker;
    private final SplittableRandom splittableRandom;

    public DataInsertJob(String connectionUrl) {
        this.connectionUrl = connectionUrl;
        this.faker = new Faker();
        this.splittableRandom = new SplittableRandom();
    }

    public void run() throws ClassNotFoundException {
        //Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        try (Connection connection = DriverManager.getConnection(connectionUrl)) {
            bulkInsert(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bulkInsert(Connection connection) throws SQLException {
        int expectedTotalInserted = TOTAL_USERS + TOTAL_ARTICLE_TYPES + TOTAL_ARTICLES + TOTAL_CATEGORIES + TOTAL_CATEGORY_ADMINS + TOTAL_TOPICS + TOTAL_POSTS + TOTAL_TOPIC_WATCHING_USERS;
        System.out.println("Start bulk insert. Expected total insert rows: " + expectedTotalInserted);

        connection.setAutoCommit(false);
        //int totalInserted = 0;
        Timestamp insertTime = new Timestamp(System.currentTimeMillis());

        //TODO add images insert
        //insertImages(connection);

        // Insert users START
        BulkInserter bulkInserter = new BulkInserter("users", insertTime, TOTAL_USERS, connection) {
            @Override
            public String createQuery() {
                return prepareQuery(
                        tableName,
                        COL_NAME_ID,
                        COL_NAME_CREATE_DATE_TIME, COL_NAME_UPDATE_DATE_TIME,
                        "about_us", "enabled", "token_expired",
                        "role",
                        "post_count", "topic_count", "article_count",
                        "first_name", "last_name", "username", "email", "password", "about_me");
            }

            @Override
            protected void setStatementData(PreparedStatement preparedStatement, long id) throws SQLException {
                prepareStatementIdCreatedUpdated(preparedStatement, id);
                preparedStatement.setBoolean(4, false);
                preparedStatement.setBoolean(5, true);
                preparedStatement.setBoolean(6, true);
                //TODO 5 admins, 50 specialists, rest users
                preparedStatement.setInt(7, 1); // role
                preparedStatement.setLong(8, 0L); // post_count
                preparedStatement.setLong(9, 0L); // topic_count
                preparedStatement.setLong(10, 0L); // article_count
                preparedStatement.setString(11, "first"); //first_name
                preparedStatement.setString(12, "last"); //last_name
                preparedStatement.setString(13, "user" + id); //username
                preparedStatement.setString(14, "email" + id); //email
                //TODO hashpwd in app is hint
                preparedStatement.setString(15, "pwd" + id); //password
                preparedStatement.setString(16, "about_me"); //about_me
            }

        };
        bulkInserter.insert();
        // Insert users END

        // Insert article_type START
        bulkInserter = new BulkInserter("article_type", insertTime, TOTAL_ARTICLE_TYPES, connection) {
            @Override
            public String createQuery() {
                return prepareQuery(
                        tableName,
                        COL_NAME_ID,
                        COL_NAME_CREATE_DATE_TIME, COL_NAME_UPDATE_DATE_TIME,
                        COL_NAME_NAME);
            }

            @Override
            protected void setStatementData(PreparedStatement preparedStatement, long id) throws SQLException {
                prepareStatementIdCreatedUpdated(preparedStatement, id);
                preparedStatement.setString(4, "article" + id); //name
            }
        };
        bulkInserter.insert();
        // Insert article_type END

        // Insert article START
        bulkInserter = new BulkInserter("article", insertTime, TOTAL_ARTICLES, connection) {
            @Override
            public String createQuery() {
                return prepareQuery(
                        tableName,
                        COL_NAME_ID,
                        COL_NAME_CREATE_DATE_TIME, COL_NAME_UPDATE_DATE_TIME,
                        COL_NAME_USER_ID, "article_type_id",
                        COL_NAME_NAME, COL_NAME_URL, COL_NAME_TEXT);
            }

            @Override
            protected void setStatementData(PreparedStatement preparedStatement, long id) throws SQLException {
                prepareStatementIdCreatedUpdated(preparedStatement, id);
                preparedStatement.setLong(4, id); //user_id
                preparedStatement.setLong(5, id); //article_type_id
                preparedStatement.setString(6, "colName"); //name
                preparedStatement.setString(7, "colURL"); //url
                preparedStatement.setString(8, "colText"); //text
            }
        };
        bulkInserter.insert();
        // Insert article END

        //insertCategories(connection);
        //insertCategoryAdmins(connection);
        //insertTopics(connection);
        //insertTopicWatchingUsers(connection);
        //insertPosts(connection);

        //preparedStatement.close();
        //TODO UPDATE USERS COUNT POSTs, TOPICs, ARTICLES
        connection.close();
        System.out.println("End bulk insert.");
    }

}
