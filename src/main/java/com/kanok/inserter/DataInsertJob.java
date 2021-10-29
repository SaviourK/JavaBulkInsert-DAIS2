package com.kanok.inserter;

import com.kanok.inserter.model.User;
import com.kanok.inserter.service.FakeDataService;
import com.kanok.inserter.service.RetrieveDbDataService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.kanok.inserter.constants.ImportConstants.*;
import static com.kanok.inserter.service.UtilsService.*;

public class DataInsertJob {

    public void run(String connectionUrl) {
        try (Connection connection = DriverManager.getConnection(connectionUrl)) {
            bulkInsert(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO stop indexing and constraints?
    private void bulkInsert(Connection connection) throws SQLException {
        int expectedTotalInserted = TOTAL_USER + TOTAL_ARTICLE_TYPE + TOTAL_ARTICLE + TOTAL_CATEGORY + TOTAL_CATEGORY_ADMIN + TOTAL_TOPIC + TOTAL_POST + TOTAL_TOPIC_WATCHING_USER;
        System.out.println("Start bulk insert. Expected total inserted rows: " + expectedTotalInserted);

        long start = System.currentTimeMillis();
        Timestamp insertTime = new Timestamp(start);
        int totalInserted = 0;

        connection.setAutoCommit(false);

        RetrieveDbDataService dbDataService = new RetrieveDbDataService(connection);
        FakeDataService fakeDataService = new FakeDataService();
        BulkInserter bulkInserter;

        //TODO add images insert
        //insertImages(connection);

        if (IMPORT_USER) {
            List<User> listUsers = fakeDataService.createFakeUsers();
            // Insert users START
            bulkInserter = new BulkInserter(TABLE_NAME_USERS, insertTime, TOTAL_USER, connection) {
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
                    preparedStatement.setInt(7, getRole(id)); // role
                    preparedStatement.setLong(8, 0L); // post_count
                    preparedStatement.setLong(9, 0L); // topic_count
                    preparedStatement.setLong(10, 0L); // article_count
                    User u = listUsers.get((int) id - 1);
                    preparedStatement.setString(11, u.getFirstName()); //first_name
                    preparedStatement.setString(12, u.getLastName()); //last_name
                    preparedStatement.setString(13, u.getUsername()); //username
                    preparedStatement.setString(14, u.getEmail()); //email
                    preparedStatement.setString(15, u.getHashedPassword()); //password
                    preparedStatement.setString(16, u.getAboutMe()); //about_me
                }
            };
            totalInserted += bulkInserter.insert();
            // Insert users END
        }

        List<Long> admins = new ArrayList<>();
        List<Long> adminSpecialist = new ArrayList<>();
        List<Long> allUsers = new ArrayList<>();
        dbDataService.fillUsersLists(admins, adminSpecialist, allUsers);

        if (IMPORT_ARTICLE_TYPE) {
            // Insert article_type START
            bulkInserter = new BulkInserter(TABLE_NAME_ARTICLE_TYPE, insertTime, TOTAL_ARTICLE_TYPE, connection) {
                @Override
                public String createQuery() {
                    return prepareQuery(
                            COL_NAME_ID, COL_NAME_CREATE_DATE_TIME, COL_NAME_UPDATE_DATE_TIME,
                            COL_NAME_NAME);
                }

                @Override
                protected void setStatementData(PreparedStatement preparedStatement, long id) throws SQLException {
                    setStatementsIdCreatedUpdated(preparedStatement, id);
                    preparedStatement.setString(4, "type" + id); //name
                }
            };
            totalInserted += bulkInserter.insert();
            // Insert article_type END
        }


        List<Long> articleTypes = new ArrayList<>();
        dbDataService.fillIdsListFromTable(articleTypes, TABLE_NAME_ARTICLE_TYPE);

        // Insert article START
        if (IMPORT_ARTICLE) {
            bulkInserter = new BulkInserter(TABLE_NAME_ARTICLE, insertTime, TOTAL_ARTICLE, connection) {
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
                    preparedStatement.setLong(4, getRandomIdFromList(adminSpecialist)); //users_id
                    preparedStatement.setLong(5, getRandomIdFromList(articleTypes)); //article_type_id
                    String name = "article" + id;
                    preparedStatement.setString(6, name); //name
                    preparedStatement.setString(7, makeFriendlyUrl(name)); //url
                    //TODO MAKE TEXT
                    preparedStatement.setString(8, "TEXT"); //text
                }
            };
            totalInserted += bulkInserter.insert();
            // Insert article END
        }

        if (IMPORT_CATEGORY) {
            // Insert category START
            bulkInserter = new BulkInserter(TABLE_NAME_CATEGORY, insertTime, TOTAL_CATEGORY, connection) {
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
                    preparedStatement.setLong(4, getRandomIdFromList(admins)); //users_id
                    String name = "category" + id;
                    preparedStatement.setString(5, name);
                    preparedStatement.setString(6, makeFriendlyUrl(name));
                }
            };
            totalInserted += bulkInserter.insert();
            // Insert category END
        }

        List<Long> categories = new ArrayList<>();
        dbDataService.fillIdsListFromTable(categories, TABLE_NAME_CATEGORY);

        if (IMPORT_CATEGORY_ADMIN) {
            // Insert category_admin START
            bulkInserter = new BulkInserter(TABLE_NAME_CATEGORY_ADMIN, insertTime, TOTAL_CATEGORY_ADMIN, connection) {
                @Override
                public String createQuery() {
                    return prepareQuery(
                            COL_NAME_CATEGORY_ID, COL_NAME_USER_ID);
                }

                @Override
                protected void setStatementData(PreparedStatement preparedStatement, long id) throws SQLException {
                    preparedStatement.setLong(1, getRandomIdFromList(categories));
                    preparedStatement.setLong(2, getRandomIdFromList(adminSpecialist));
                }
            };
            totalInserted += bulkInserter.insert();
            // Insert category_admin END
        }

        if (IMPORT_TOPIC) {
            // Insert topic START
            bulkInserter = new BulkInserter(TABLE_NAME_TOPIC, insertTime, TOTAL_TOPIC, connection) {
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
                    preparedStatement.setLong(4, getRandomIdFromList(categories));
                    preparedStatement.setLong(5, getRandomIdFromList(allUsers));
                    String name = "topic" + id;
                    //TODO Longer and diff topic topic text
                    preparedStatement.setString(6, name);
                    preparedStatement.setString(7, name);
                }
            };
            totalInserted += bulkInserter.insert();
            // Insert topic END
        }

        List<Long> topics = new ArrayList<>();
        dbDataService.fillIdsListFromTable(topics, TABLE_NAME_TOPIC);

        if (IMPORT_POST) {
            // Insert post START
            bulkInserter = new BulkInserter(TABLE_NAME_POST, insertTime, TOTAL_POST, connection) {
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
                    preparedStatement.setLong(4, getRandomIdFromList(topics));
                    preparedStatement.setLong(5, getRandomIdFromList(allUsers));
                    preparedStatement.setString(6, "POST" + id);
                }
            };
            totalInserted += bulkInserter.insert();
            // Insert post END
        }

        if (IMPORT_TOPIC_WATCHING_USER) {
            // Insert topic_watching_user START
            bulkInserter = new BulkInserter(TABLE_NAME_TOPIC_WATCHING_USER, insertTime, TOTAL_TOPIC_WATCHING_USER, connection) {
                @Override
                public String createQuery() {
                    return prepareQuery(
                            COL_NAME_TOPIC_ID, COL_NAME_USER_ID);
                }

                @Override
                protected void setStatementData(PreparedStatement preparedStatement, long id) throws SQLException {
                    preparedStatement.setLong(1, getRandomIdFromList(topics));
                    preparedStatement.setLong(2, getRandomIdFromList(allUsers));
                }
            };
            totalInserted += bulkInserter.insert();
            // Insert topic_watching_user END
        }

        //TODO UPDATE USERS COUNT POSTs, TOPICs, ARTICLES
        connection.close();
        long end = System.currentTimeMillis();
        System.out.println("End bulk insert. Total time taken: " + (end - start) / 1000 + " s" + " Total inserted: " + totalInserted);
    }
}
