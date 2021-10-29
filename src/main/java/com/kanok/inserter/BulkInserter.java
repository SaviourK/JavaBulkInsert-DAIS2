package com.kanok.inserter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public abstract class BulkInserter {

    protected final String tableName;
    protected final Timestamp insertTime;
    private final long totalInsert;
    private final Connection connection;

    protected BulkInserter(String tableName, Timestamp insertTime, long totalInsert, Connection connection) {
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

    protected abstract void setStatementData(PreparedStatement preparedStatement, long id) throws SQLException;

    protected int executeTableBatch(PreparedStatement preparedStatement) throws SQLException {
        long start = startTableBatch();

        int totalInserted = 0;
        for (long i = 1; i < totalInsert + 1; i++) {
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
}
