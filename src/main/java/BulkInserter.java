import com.github.javafaker.Faker;

import java.sql.*;

public class BulkInserter {

    private final int totalUsers = 1000;
    private final int totalArticleTypes = 50;
    private final int totalArticles = 300000;
    private final int totalCategories = 10;
    private final int totalCategoryAdmins = 20;
    private final int totalTopics = 50000;
    private final int totalPosts = 5000000;
    private final int totalTopicWatchingUsers = 50000;

    private final String colNameId = "id";
    private final String colNameName = "name";
    private final String colNameText = "text";
    private final String colNameCreateDateTime = "create_date_time";
    private final String colNameUpdateDateTime = "update_date_time";
    private final String colNameUserId = "user_id";
    private final String colNameUrl = "url";


    private final String connectionUrl;
    private final Faker faker;

    public BulkInserter(String connectionUrl) {
        this.connectionUrl = connectionUrl;
        this.faker = new Faker();
    }

    public void run() throws ClassNotFoundException {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        try (Connection connection = DriverManager.getConnection(connectionUrl)) {
            bulkInsert(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bulkInsert(Connection connection) throws SQLException {
        int expectedTotalInserted = totalUsers + totalArticleTypes + totalArticles + totalCategories + totalCategoryAdmins + totalTopics + totalPosts + totalTopicWatchingUsers;
        System.out.println("Start bulk insert. Expected total insert rows: " + expectedTotalInserted);

        connection.setAutoCommit(false);
        //int totalInserted = 0;


        Timestamp createdTime = new Timestamp(System.currentTimeMillis());

        //TODO add images insert
        //insertImages(connection);
        insertUsers(connection, createdTime);
        insertArticleTypes(connection, createdTime);
        insertArticles(connection, createdTime);
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

    private void insertUsers(Connection connection, Timestamp createdTime) throws SQLException {
        String tableName = "users";
        String query = createQuery(
                tableName,
                colNameId,
                colNameCreateDateTime, colNameUpdateDateTime,
                "about_us", "enabled", "token_expired",
                "role",
                "post_count", "topic_count", "article_count",
                "first_name", "last_name", "username", "email", "password", "about_me"
        );
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        executeUsersBatch(connection, tableName, preparedStatement, createdTime);
    }

    private void prepareStatementUsers(PreparedStatement preparedStatement, Timestamp startTime, long i) throws SQLException {
        prepareStatementIdCreatedUpdated(preparedStatement, startTime, i);
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
        preparedStatement.setString(13, "user" + i); //username
        preparedStatement.setString(14, "email" + i); //email
        //TODO hashpwd in app is hint
        preparedStatement.setString(15, "pwd" + i); //password
        preparedStatement.setString(16, "about_me"); //about_me

    }

    private void executeUsersBatch(Connection connection, String tableName, PreparedStatement preparedStatement, Timestamp createdTime) throws SQLException {
        long start = startTableBatch(tableName);

        long totalInserted = 0;
        for (long i = 0; i < totalUsers; ++i) {
            prepareStatementUsers(preparedStatement, createdTime, i);
            totalInserted = executeBatch(connection, tableName, preparedStatement, totalInserted, start, i);
        }

        endTableBatch(connection, tableName, preparedStatement, start);
    }

    private void insertArticleTypes(Connection connection, Timestamp createdTime) throws SQLException {
        String tableName = "article_type";
        String query = createQuery(
                tableName,
                colNameId,
                colNameCreateDateTime, colNameUpdateDateTime,
                colNameName);
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        executeArticleTypeBatch(connection, tableName, preparedStatement, createdTime);
    }

    private void prepareStatementArticleType(PreparedStatement preparedStatement, Timestamp createdTime, long i) throws SQLException {
        prepareStatementIdCreatedUpdated(preparedStatement, createdTime, i);
        preparedStatement.setString(4, "article" + i); //name
    }

    private void executeArticleTypeBatch(Connection connection, String tableName, PreparedStatement preparedStatement, Timestamp createdTime) throws SQLException {
        long start = startTableBatch(tableName);

        long totalInserted = 0;
        for (long i = 0; i < totalArticleTypes; ++i) {
            prepareStatementArticleType(preparedStatement, createdTime, i);
            totalInserted = executeBatch(connection, tableName, preparedStatement, totalInserted, start, i);
        }

        endTableBatch(connection, tableName, preparedStatement, start);
    }

    private void insertArticles(Connection connection, Timestamp createdTime) throws SQLException {
        String tableName = "article";
        String query = createQuery(
                tableName,
                colNameId,
                colNameCreateDateTime, colNameUpdateDateTime,
                colNameUserId, "article_type_id",
                colNameName, colNameUrl, colNameText);
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        executeArticleBatch(connection, tableName, preparedStatement, createdTime);
    }

    private void prepareStatementArticle(Timestamp createdTime, PreparedStatement preparedStatement, long i) throws SQLException {
        prepareStatementIdCreatedUpdated(preparedStatement, createdTime, i);
        preparedStatement.setLong(4, i); //user_id
        preparedStatement.setLong(5, i); //article_type_id
        preparedStatement.setString(6, "colName"); //name
        preparedStatement.setString(7, "colURL"); //url
        preparedStatement.setString(8, "colText"); //text
    }

    private void executeArticleBatch(Connection connection, String tableName, PreparedStatement preparedStatement, Timestamp createdTime) throws SQLException {
        long start = startTableBatch(tableName);

        long totalInserted = 0;
        for (long i = 0; i < totalArticles; ++i) {
            prepareStatementArticle(createdTime, preparedStatement, i);
            totalInserted = executeBatch(connection, tableName, preparedStatement, totalInserted, start, i);
        }

        endTableBatch(connection, tableName, preparedStatement, start);
    }

    private long executeBatch(Connection connection, String tableName, PreparedStatement preparedStatement, long totalInserted, long start, long i) throws SQLException {
        preparedStatement.addBatch();
        if (i % 10000 == 0) {
            totalInserted += preparedStatement.executeBatch().length;
            connection.commit();
            long end = System.currentTimeMillis();
            System.out.println("Table: " + tableName + " Total time taken = " + (end - start) / 1000 + " s" + " total inserted " + totalInserted + " i =" + i);
        }
        return totalInserted;
    }

    private void prepareStatementIdCreatedUpdated(PreparedStatement preparedStatement, Timestamp createdTime, long i) throws SQLException {
        preparedStatement.setLong(1, i); //id
        preparedStatement.setTimestamp(2, createdTime); //create_date_time
        preparedStatement.setTimestamp(3, createdTime); //update_date_time
    }

    private long startTableBatch(String tableName) {
        System.out.println("Start inserting for Table: " + tableName);
        return System.currentTimeMillis();
    }

    private void endTableBatch(Connection connection, String tableName, PreparedStatement preparedStatement, long start) throws SQLException {
        long totalInserted = preparedStatement.executeBatch().length;
        preparedStatement.close();
        connection.commit();
        long end = System.currentTimeMillis();
        System.out.println("End inserting for Table: " + tableName + " Total time taken = " + (end - start) / 1000 + " s" + " total inserted " + totalInserted);
    }

    private String createQuery(String tableName, String... columns) {
        String columnsString = String.join(",", columns);
        String valuesString = createValues(columns.length);

        String query = "INSERT INTO " + tableName + "(" + columnsString + ") " +
                "VALUES" + "(" + valuesString + ")";
        System.out.println("Query: " + query);

        return query;
    }

    private String createValues(int size) {
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (values.length() == 0) {
                values.append("?");
            } else {
                values.append(",?");
            }
        }
        return values.toString();
    }
}
