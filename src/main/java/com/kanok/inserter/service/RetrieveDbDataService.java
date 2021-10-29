package com.kanok.inserter.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class RetrieveDbDataService {

    private final Connection connection;

    public RetrieveDbDataService(Connection connection) {
        this.connection = connection;
    }

    public void fillUsersLists(List<Long> admins, List<Long> adminSpecialist, List<Long> allUsers) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT role, id FROM users");
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
        preparedStatement.close();
    }

    public void fillIdsListFromTable(List<Long> ids, String tableName) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM " + tableName);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            ids.add(resultSet.getLong(1));
        }
        preparedStatement.close();
    }
}
