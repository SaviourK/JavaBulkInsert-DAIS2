package com.kanok.inserter;

import com.kanok.inserter.model.*;
import com.kanok.inserter.service.FakeDataService;
import com.kanok.inserter.service.RetrieveDataService;
import com.kanok.inserter.service.UpdateDataService;

import java.security.NoSuchAlgorithmException;
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

    private void bulkInsert(Connection connection) throws SQLException, NoSuchAlgorithmException {
        int expectedTotalInserted = TOTAL_USER + TOTAL_ARTICLE_TYPE + TOTAL_ARTICLE + TOTAL_CATEGORY + TOTAL_CATEGORY_ADMIN + TOTAL_TOPIC + TOTAL_POST + TOTAL_TOPIC_WATCHING_USER;
        System.out.println("Start bulk insert. Batch size: " + BATCH_SIZE + " Expected total inserted rows: " + expectedTotalInserted);

        long start = System.currentTimeMillis();
        Timestamp insertTime = new Timestamp(start);
        int totalInserted = 0;

        connection.setAutoCommit(false);

        RetrieveDataService dbDataService = new RetrieveDataService(connection);
        FakeDataService fakeDataService = new FakeDataService(HASH_TYPE_SHA3_256);
        BulkInserter bulkInserter;

        if (IMPORT_IMAGE) {
            List<Image> imageList = fakeDataService.createImages();
            // Insert image START
            bulkInserter = new BulkInserter(TABLE_NAME_IMAGE, insertTime, TOTAL_IMAGE, connection) {
                @Override
                public String createQuery() {
                    return prepareQuery(
                            COL_NAME_ID, COL_NAME_CREATE_DATE_TIME, COL_NAME_UPDATE_DATE_TIME,
                            "data", "file_name", "file_type");
                }

                @Override
                protected void setStatementData(PreparedStatement preparedStatement, int id) throws SQLException {
                    setStatementsIdCreatedUpdated(preparedStatement, id);
                    Image i = imageList.get(id - 1);
                    preparedStatement.setBytes(4, i.getData());
                    preparedStatement.setString(5, i.getFileName());
                    preparedStatement.setString(6, i.getFileType());
                }
            };
            totalInserted += bulkInserter.insert();
            // Insert image END
        }

        List<Long> images = dbDataService.createIdsListFromTable(TABLE_NAME_IMAGE);

        if (IMPORT_USER) {
            List<User> listUsers = fakeDataService.createUsers();
            // Insert users START
            bulkInserter = new BulkInserter(TABLE_NAME_USERS, insertTime, TOTAL_USER, connection) {
                @Override
                public String createQuery() {
                    return prepareQuery(
                            COL_NAME_ID, COL_NAME_CREATE_DATE_TIME, COL_NAME_UPDATE_DATE_TIME,
                            "about_us", "enabled", "token_expired",
                            "role",
                            "post_count", "topic_count", "article_count",
                            "image_id",
                            "first_name", "last_name", "username", "email", "password", "about_me");
                }

                @Override
                protected void setStatementData(PreparedStatement preparedStatement, int id) throws SQLException {
                    setStatementsIdCreatedUpdated(preparedStatement, id);
                    User u = listUsers.get(id - 1);
                    preparedStatement.setBoolean(4, u.isAboutUs());
                    preparedStatement.setBoolean(5, u.isEnabled());
                    preparedStatement.setBoolean(6, true);
                    preparedStatement.setInt(7, getRole(id));
                    preparedStatement.setLong(8, 0L);
                    preparedStatement.setLong(9, 0L);
                    preparedStatement.setLong(10, 0L);
                    preparedStatement.setLong(11, getRandomIdFromList(images));
                    preparedStatement.setString(12, u.getFirstName());
                    preparedStatement.setString(13, u.getLastName());
                    preparedStatement.setString(14, u.getUsername());
                    preparedStatement.setString(15, u.getEmail());
                    preparedStatement.setString(16, u.getHashedPassword());
                    preparedStatement.setString(17, u.getAboutMe());
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
            List<String> articleTypes = fakeDataService.createArticleTypes();
            // Insert article_type START
            bulkInserter = new BulkInserter(TABLE_NAME_ARTICLE_TYPE, insertTime, TOTAL_ARTICLE_TYPE, connection) {
                @Override
                public String createQuery() {
                    return prepareQuery(
                            COL_NAME_ID, COL_NAME_CREATE_DATE_TIME, COL_NAME_UPDATE_DATE_TIME,
                            COL_NAME_NAME);
                }

                @Override
                protected void setStatementData(PreparedStatement preparedStatement, int id) throws SQLException {
                    setStatementsIdCreatedUpdated(preparedStatement, id);
                    preparedStatement.setString(4, articleTypes.get(id - 1));
                }
            };
            totalInserted += bulkInserter.insert();
            // Insert article_type END
        }

        List<Long> articleTypes = dbDataService.createIdsListFromTable(TABLE_NAME_ARTICLE_TYPE);

        if (IMPORT_ARTICLE) {
            List<Article> articles = fakeDataService.createArticles();
            // Insert article START
            bulkInserter = new BulkInserter(TABLE_NAME_ARTICLE, insertTime, TOTAL_ARTICLE, connection) {
                @Override
                public String createQuery() {
                    return prepareQuery(
                            COL_NAME_ID, COL_NAME_CREATE_DATE_TIME, COL_NAME_UPDATE_DATE_TIME,
                            COL_NAME_USER_ID, "article_type_id",
                            COL_NAME_NAME, COL_NAME_URL, COL_NAME_TEXT);
                }

                @Override
                protected void setStatementData(PreparedStatement preparedStatement, int id) throws SQLException {
                    setStatementsIdCreatedUpdated(preparedStatement, id);
                    preparedStatement.setLong(4, getRandomIdFromList(adminSpecialist));
                    preparedStatement.setLong(5, getRandomIdFromList(articleTypes));
                    Article a = articles.get(id - 1);
                    preparedStatement.setString(6, a.getName());
                    preparedStatement.setString(7, a.getUrl());
                    preparedStatement.setString(8, a.getText());
                }
            };
            totalInserted += bulkInserter.insert();
            // Insert article END
        }

        if (IMPORT_ARTICLE_IMAGES) {
            // Insert article_images START
            bulkInserter = new BulkInserter(TABLE_NAME_ARTICLE_IMAGES, insertTime, TOTAL_ARTICLE, connection) {
                @Override
                public String createQuery() {
                    return prepareQuery(
                            "article_id", "images_id");
                }

                @Override
                protected void setStatementData(PreparedStatement preparedStatement, int id) throws SQLException {
                    preparedStatement.setLong(1, id);
                    preparedStatement.setLong(2, id);
                }
            };
            totalInserted += bulkInserter.insert();
            // Insert article_images END
        }

        if (IMPORT_CATEGORY) {
            List<Category> categories = fakeDataService.createCategories();
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
                protected void setStatementData(PreparedStatement preparedStatement, int id) throws SQLException {
                    setStatementsIdCreatedUpdated(preparedStatement, id);
                    preparedStatement.setLong(4, getRandomIdFromList(admins)); //users_id
                    Category c = categories.get(id - 1);
                    preparedStatement.setString(5, c.getName());
                    preparedStatement.setString(6, c.getUrl());
                }
            };
            totalInserted += bulkInserter.insert();
            // Insert category END
        }

        List<Long> categories = dbDataService.createIdsListFromTable(TABLE_NAME_CATEGORY);

        if (IMPORT_CATEGORY_ADMIN) {
            // Insert category_admin START
            bulkInserter = new BulkInserter(TABLE_NAME_CATEGORY_ADMIN, insertTime, TOTAL_CATEGORY_ADMIN, connection) {
                @Override
                public String createQuery() {
                    return prepareQuery(
                            COL_NAME_CATEGORY_ID, COL_NAME_USER_ID);
                }

                @Override
                protected void setStatementData(PreparedStatement preparedStatement, int id) throws SQLException {
                    preparedStatement.setLong(1, getRandomIdFromList(categories));
                    preparedStatement.setLong(2, getRandomIdFromList(adminSpecialist));
                }
            };
            totalInserted += bulkInserter.insert();
            // Insert category_admin END
        }

        List<String> texts = fakeDataService.createText(2000, 10, 1000);

        if (IMPORT_TOPIC) {
            List<String> topics = fakeDataService.createTopics();
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
                protected void setStatementData(PreparedStatement preparedStatement, int id) throws SQLException {
                    setStatementsIdCreatedUpdated(preparedStatement, id);
                    preparedStatement.setLong(4, getRandomIdFromList(categories));
                    preparedStatement.setLong(5, getRandomIdFromList(allUsers));
                    preparedStatement.setString(6, topics.get(id - 1));
                    preparedStatement.setString(7, getRandomStringFromList(texts));
                }
            };
            totalInserted += bulkInserter.insert();
            // Insert topic END
        }

        List<Long> topics = dbDataService.createIdsListFromTable(TABLE_NAME_TOPIC);

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
                protected void setStatementData(PreparedStatement preparedStatement, int id) throws SQLException {
                    setStatementsIdCreatedUpdated(preparedStatement, id);
                    preparedStatement.setLong(4, getRandomIdFromList(topics));
                    preparedStatement.setLong(5, getRandomIdFromList(allUsers));
                    preparedStatement.setString(6, getRandomStringFromList(texts));
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
                protected void setStatementData(PreparedStatement preparedStatement, int id) throws SQLException {
                    preparedStatement.setLong(1, getRandomIdFromList(topics));
                    preparedStatement.setLong(2, getRandomIdFromList(allUsers));
                }
            };
            totalInserted += bulkInserter.insert();
            // Insert topic_watching_user END
        }

        postProcessing(dbDataService, connection);

        connection.close();
        long end = System.currentTimeMillis();
        System.out.println("End bulk insert. Total time taken: " + (end - start) + " ms" + " Total inserted: " + totalInserted);
    }

    private void postProcessing(RetrieveDataService retrieveDataService, Connection connection) throws SQLException {
        UpdateDataService updateDataService = new UpdateDataService(connection);
        if (POSTPROCESS_USER) {
            List<UserCount> userCounts = retrieveDataService.createUserCount();
            updateDataService.updateUsers(userCounts);
        }
    }
}
