package com.kanok.inserter;

public class App {

    public static void main(String[] args) {
        String connectionUrl = "jdbc:sqlserver://localhost;databaseName=laryngektomie;user=admin;password=admin";

        DataInsertJob dataInsertJob = new DataInsertJob();
        dataInsertJob.run(connectionUrl);
    }
}
