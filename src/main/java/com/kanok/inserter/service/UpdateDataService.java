package com.kanok.inserter.service;

import com.kanok.inserter.model.UserCount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UpdateDataService {

    private final Connection connection;

    public UpdateDataService(Connection connection) {
        this.connection = connection;
    }

    public void updateUsers(List<UserCount> userCounts) throws SQLException {
        String sql = "update users set topic_count=?, post_count=?, article_count=? where id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (UserCount uc : userCounts) {
                preparedStatement.setInt(1, uc.getTopicCount());
                preparedStatement.setInt(2, uc.getPostCount());
                preparedStatement.setInt(3, uc.getArticleCount());
                preparedStatement.setLong(4, uc.getUserId());
                preparedStatement.addBatch();
            }
            int totalUpdated = preparedStatement.executeBatch().length;
            connection.commit();
            System.out.println("Total updated users: " + totalUpdated);
        }
    }
}
