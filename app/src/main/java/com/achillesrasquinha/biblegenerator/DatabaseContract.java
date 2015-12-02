package com.achillesrasquinha.biblegenerator;

import android.provider.BaseColumns;

public final class DatabaseContract {
    public static final String DATABASE_NAME    = "bible.db";
    public static final int    DATABASE_VERSION = 1;

    public static abstract class Table1 implements BaseColumns {
        public static final String TABLE_NAME    = "bible_kjv";
        public static final String COLUMN_NAME_0 = "_id";
        public static final String COLUMN_NAME_1 = "book_id";
        public static final String COLUMN_NAME_2 = "chapter";
        public static final String COLUMN_NAME_3 = "verse";
        public static final String COLUMN_NAME_4 = "text";

        public static final String[] COLUMN_NAMES = {
            COLUMN_NAME_0,
            COLUMN_NAME_1,
            COLUMN_NAME_2,
            COLUMN_NAME_3,
            COLUMN_NAME_4
        };

        public static final int    ROW_COUNT     = 31103;
        public static final int    COLUMN_COUNT  = 5;

        public static final String QUERY_RANDOM_ROW =
            " SELECT * " +
            " FROM " + TABLE_NAME +
            " LIMIT 1 " +
            " OFFSET ABS(RANDOM()) % " + Integer.toString(ROW_COUNT);
    }

    public static abstract class Table2 implements BaseColumns {
        public static final String TABLE_NAME    = "bible_books";
        public static final String COLUMN_NAME_0 = "_id";
        public static final String COLUMN_NAME_1 = "book_name";

        public static final String[] COLUMN_NAMES = {
                COLUMN_NAME_0,
                COLUMN_NAME_1
        };
    }

    public static abstract class Table3 implements BaseColumns {
        public static final String TABLE_NAME    = "like";
        public static final String COLUMN_NAME_0 = "bible_kjv_id";
    }

    public DatabaseContract() { }
}
