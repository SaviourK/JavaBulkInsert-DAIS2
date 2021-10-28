package com.kanok.inserter;

public class Exec {

    public static void main(String[] args) throws ClassNotFoundException {
        String connectionUrl = "jdbc:sqlserver://localhost;databaseName=laryngektomie;user=admin;password=admin";

        DataInsertJob dataInsertJob = new DataInsertJob();
        dataInsertJob.run(connectionUrl);
    }
}
