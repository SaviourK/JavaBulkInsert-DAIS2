package com.kanok.inserter.service;

import com.kanok.inserter.model.UserCount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RetrieveDataService {

    private final Connection connection;

    public RetrieveDataService(Connection connection) {
        this.connection = connection;
    }

    public void fillUsersLists(List<Long> admins, List<Long> adminSpecialist, List<Long> allUsers) throws SQLException {
        System.out.println("Fetching users ids START");
        long startFetch = System.currentTimeMillis();

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT role, id FROM users")) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int role = resultSet.getInt(1);
                long id = resultSet.getLong(2);
                if (role == (1)) {
                    admins.add(id);
                    adminSpecialist.add(id);
                    allUsers.add(id);
                } else if (role == 2) {
                    adminSpecialist.add(id);
                    allUsers.add(id);
                } else {
                    allUsers.add(id);
                }
            }
        }

        long endFetch = System.currentTimeMillis();
        System.out.println("Fetching users ids END. Total time taken: " + (endFetch - startFetch) + " ms");
    }

    public List<Long> createIdsListFromTable(String tableName) throws SQLException {
        System.out.println("Fetching " + tableName + " ids START");
        long startFetch = System.currentTimeMillis();

        List<Long> ids = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM " + tableName)) {
            ResultSet resultSet = preparedStatement.executeQuery();


            while (resultSet.next()) {
                ids.add(resultSet.getLong(1));
            }
        }

        long endFetch = System.currentTimeMillis();
        System.out.println("Fetching " + tableName + " ids END. Total time taken: " + (endFetch - startFetch) + " ms");
        return ids;
    }

    public List<UserCount> createUserCount() throws SQLException {
        System.out.println("Fetching users count START");
        long startFetch = System.currentTimeMillis();

        String sql = "select u.id, \n" +
                "(select count(t.id) \n" +
                "from topic t \n" +
                "where u.id = t.user_id) as topic_count,\n" +
                "(select count(p.id) \n" +
                "from post p \n" +
                "where u.id = p.user_id) as post_count,\n" +
                "(select count(a.id) \n" +
                "from article a \n" +
                "where u.id = a.user_id) as article_count\n" +
                "from users u \n" +
                "order by u.id";

        List<UserCount> userCounts = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                userCounts.add(new UserCount(
                        resultSet.getLong(1),
                        resultSet.getInt(2),
                        resultSet.getInt(3),
                        resultSet.getInt(4)
                ));
            }

        }
        long endFetch = System.currentTimeMillis();
        System.out.println("Fetching users count END. Total time taken: " + (endFetch - startFetch) + " ms");
        return userCounts;
    }
}
