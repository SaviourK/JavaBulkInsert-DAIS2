package com.kanok.inserter;

import com.github.javafaker.Faker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.SplittableRandom;

public abstract class BulkInserter {

    protected static final String COL_NAME_ID = "id";
    protected static final String COL_NAME_CREATE_DATE_TIME = "create_date_time";
    protected static final String COL_NAME_UPDATE_DATE_TIME = "update_date_time";
    protected static final String COL_NAME_NAME = "name";
    protected static final String COL_NAME_TEXT = "text";
    protected static final String COL_NAME_URL = "url";
    protected static final String COL_NAME_USER_ID = "user_id";
    protected static final String COL_NAME_CATEGORY_ID = "category_id";
    protected static final String COL_NAME_TOPIC_ID = "topic_id";

    protected final String tableName;
    protected final Timestamp insertTime;
    private final long insertNum;
    private final Connection connection;

    protected Faker faker;
    protected SplittableRandom splittableRandom;
    protected BCryptPasswordEncoder encoder;

    protected BulkInserter(String tableName, Timestamp insertTime, long insertNum, Connection connection) {
        this.tableName = tableName;
        this.connection = connection;
        this.insertTime = insertTime;
        this.insertNum = insertNum;
    }

    public int insert() throws SQLException {
        String query = createQuery();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        return executeTableBatch(preparedStatement);
    }

    protected abstract String createQuery() throws SQLException;

    protected abstract void setStatementData(PreparedStatement preparedStatement, long id) throws SQLException;

    protected int executeTableBatch(PreparedStatement preparedStatement) throws SQLException {
        long start = startTableBatch();

        int totalInserted = 0;
        for (long i = 1; i < insertNum + 1; i++) {
            setStatementData(preparedStatement, i);
            totalInserted += executeBatch(preparedStatement, totalInserted, start, i);
        }

        totalInserted += endTableBatch(preparedStatement, start);
        return totalInserted;
    }

    protected int executeBatch(PreparedStatement preparedStatement, int totalInserted, long start, long i) throws SQLException {
        preparedStatement.addBatch();
        if (i % 10000 == 0) {
            totalInserted += preparedStatement.executeBatch().length;
            connection.commit();
            long end = System.currentTimeMillis();
            System.out.println("Table: " + tableName + " Total time taken = " + (end - start) / 1000 + " s" + " total inserted " + totalInserted + " i =" + i);
        }
        return totalInserted;
    }

    protected long startTableBatch() {
        System.out.println("Start inserting for Table: " + tableName);
        return System.currentTimeMillis();
    }

    protected int endTableBatch(PreparedStatement preparedStatement, long start) throws SQLException {
        int totalInserted = preparedStatement.executeBatch().length;
        preparedStatement.close();
        connection.commit();
        long end = System.currentTimeMillis();
        System.out.println("End inserting for Table: " + tableName + " Total time taken = " + (end - start) / 1000 + " s" + " total inserted " + totalInserted);
        return totalInserted;
    }

    protected String prepareQuery(String... columns) {
        String columnsString = String.join(",", columns);
        String valuesString = createValues(columns.length);

        String query = "INSERT INTO " + tableName + "(" + columnsString + ") " +
                "VALUES" + "(" + valuesString + ")";
        System.out.println("Query: " + query);

        return query;
    }

    protected String createValues(int size) {
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

    protected void setStatementsIdCreatedUpdated(PreparedStatement preparedStatement, long id) throws SQLException {
        preparedStatement.setLong(1, id); //id
        preparedStatement.setTimestamp(2, insertTime); //create_date_time
        preparedStatement.setTimestamp(3, insertTime); //update_date_time
    }

    public void setFaker(Faker faker) {
        this.faker = faker;
    }

    public void setSplittableRandom(SplittableRandom splittableRandom) {
        this.splittableRandom = splittableRandom;
    }

    public void setEncoder(BCryptPasswordEncoder encoder) {
        this.encoder = encoder;
    }
}
