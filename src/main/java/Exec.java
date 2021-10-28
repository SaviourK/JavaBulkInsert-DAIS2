public class Exec {

    public static void main(String[] args) throws ClassNotFoundException {
        String connectionUrl = "jdbc:sqlserver://localhost;databaseName=laryngektomie;user=admin;password=admin";

        BulkInserter bulkInserter = new BulkInserter(connectionUrl);
        bulkInserter.run();
    }
}
