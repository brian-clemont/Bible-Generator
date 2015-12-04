// Copyright 2015 Achilles Rasquinha

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

// http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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

        public static final String[] COLUMN_NAME = {
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

        public static final String[] COLUMN_NAME = {
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
