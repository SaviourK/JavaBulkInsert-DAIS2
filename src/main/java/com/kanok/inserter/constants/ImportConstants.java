package com.kanok.inserter.constants;

public class ImportConstants {

    private ImportConstants() {
    }

    // total imported instances
    public static final int TOTAL_USER = 1000;
    public static final int TOTAL_ARTICLE_TYPE = 50;
    public static final int TOTAL_ARTICLE = 300000;
    public static final int TOTAL_CATEGORY = 10;
    public static final int TOTAL_CATEGORY_ADMIN = 20;
    public static final int TOTAL_TOPIC = 50000;
    public static final int TOTAL_POST = 5000000;
    public static final int TOTAL_TOPIC_WATCHING_USER = 50000;

    // import switcher
    public static final boolean IMPORT_USER = true;
    public static final boolean IMPORT_ARTICLE_TYPE = false;
    public static final boolean IMPORT_ARTICLE = false;
    public static final boolean IMPORT_CATEGORY = false;
    public static final boolean IMPORT_CATEGORY_ADMIN = false;
    public static final boolean IMPORT_TOPIC = false;
    public static final boolean IMPORT_POST = false;
    public static final boolean IMPORT_TOPIC_WATCHING_USER = false;

    // table columns name
    public static final String COL_NAME_ID = "id";
    public static final String COL_NAME_CREATE_DATE_TIME = "create_date_time";
    public static final String COL_NAME_UPDATE_DATE_TIME = "update_date_time";
    public static final String COL_NAME_NAME = "name";
    public static final String COL_NAME_TEXT = "text";
    public static final String COL_NAME_URL = "url";
    public static final String COL_NAME_USER_ID = "user_id";
    public static final String COL_NAME_CATEGORY_ID = "category_id";
    public static final String COL_NAME_TOPIC_ID = "topic_id";

    // table names
    public static final String TABLE_NAME_USERS = "USERS";
    public static final String TABLE_NAME_ARTICLE_TYPE = "ARTICLE_TYPE";
    public static final String TABLE_NAME_ARTICLE = "ARTICLE";
    public static final String TABLE_NAME_CATEGORY = "CATEGORY";
    public static final String TABLE_NAME_CATEGORY_ADMIN = "CATEGORY_ADMIN";
    public static final String TABLE_NAME_TOPIC = "TOPIC";
    public static final String TABLE_NAME_POST = "POST";
    public static final String TABLE_NAME_TOPIC_WATCHING_USER = "TOPIC_WATCHING_USER";
}
