package com.kanok.inserter;

import com.kanok.inserter.constants.ImportConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public abstract class BulkInserter {

    protected final String tableName;
    protected final Timestamp insertTime;
    private final int totalInsert;
    private final Connection connection;

    protected BulkInserter(String tableName, Timestamp insertTime, int totalInsert, Connection connection) {
        this.tableName = tableName;
        this.connection = connection;
        this.insertTime = insertTime;
        this.totalInsert = totalInsert;
    }

    public int insert() throws SQLException {
        String query = createQuery();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        return executeTableBatch(preparedStatement);
    }

    protected abstract String createQuery() throws SQLException;

    protected abstract void setStatementData(PreparedStatement preparedStatement, int id) throws SQLException;

    protected String prepareQuery(String... columns) {
        String columnsString = String.join(",", columns);
        String valuesString = createValues(columns.length);

        String query = "INSERT INTO " + tableName + "(" + columnsString + ") " +
                "VALUES" + "(" + valuesString + ")";
        System.out.println("Query: " + query);

        return query;
    }

    protected void setStatementsIdCreatedUpdated(PreparedStatement preparedStatement, long id) throws SQLException {
        preparedStatement.setLong(1, id); //id
        preparedStatement.setTimestamp(2, insertTime); //create_date_time
        preparedStatement.setTimestamp(3, insertTime); //update_date_time
    }

    private int executeTableBatch(PreparedStatement preparedStatement) throws SQLException {
        long start = startTableBatch();

        int totalInserted = 0;
        for (int i = 1; i < totalInsert + 1; i++) {
            setStatementData(preparedStatement, i);
            totalInserted += executeBatch(preparedStatement, totalInserted, start, i, totalInsert);
        }

        totalInserted += endTableBatch(preparedStatement, totalInserted, start, totalInsert);
        return totalInserted;
    }

    private int executeBatch(PreparedStatement preparedStatement, int totalInserted, long start, int i, int totalInsert) throws SQLException {
        preparedStatement.addBatch();
        int inserted = 0;
        if (i % ImportConstants.BATCH_SIZE == 0) {
            inserted = preparedStatement.executeBatch().length;
            connection.commit();
            long end = System.currentTimeMillis();
            System.out.println("Table: " + tableName + " Total time taken = " + (end - start) + " ms. Total inserted " + (totalInserted + inserted) + "/" + totalInsert);
        }
        return inserted;
    }

    private long startTableBatch() {
        System.out.println("Start inserting for Table: " + tableName);
        return System.currentTimeMillis();
    }

    private int endTableBatch(PreparedStatement preparedStatement, int totalInserted, long start, int totalInsert) throws SQLException {
        int inserted = preparedStatement.executeBatch().length;
        preparedStatement.close();
        connection.commit();
        long end = System.currentTimeMillis();
        System.out.println("End inserting for Table: " + tableName + " Total time taken = " + (end - start) + " ms. Total inserted " + (totalInserted + inserted) + "/" + totalInsert);
        return inserted;
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
}
