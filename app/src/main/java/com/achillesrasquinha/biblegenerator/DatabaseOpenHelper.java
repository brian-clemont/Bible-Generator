package com.achillesrasquinha.biblegenerator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private Context        mContext;
    private String         mDbPath;
    private String         mDbName;
    public  SQLiteDatabase db;

    public DatabaseOpenHelper(Context context, String mDbName) {
        super(context, mDbName, null, 1);
        this.mContext = context;
        this.mDbPath  = context.getApplicationInfo().dataDir + "/databases/";
        this.mDbName  = mDbName + ".db";
    }

    private boolean exists() {
        SQLiteDatabase db = null;

        try {
            db = SQLiteDatabase.openDatabase(mDbPath + mDbName, null, SQLiteDatabase.OPEN_READONLY);
        }
        catch (SQLiteException e) {

        }

        if(db != null) {
            db.close();
        }

        return db != null ? true : false;
    }

    private void copy() throws IOException {
        InputStream  lInputStream  = mContext.getAssets().open(mDbName);
        OutputStream lOutputStream = new FileOutputStream(mDbPath + mDbName);
        byte[]       lByteBuffer   = new byte[1024];
        int          length;

        while ((length = lInputStream.read(lByteBuffer)) > 0) {
            lOutputStream.write(lByteBuffer, 0, length);
        }

        lInputStream .close();
        lOutputStream.flush();
        lOutputStream.close();
    }

    public void create() throws IOException {
        if(!exists()) {

            this.getWritableDatabase();

            try {
                copy();
            }
            catch (IOException e) {

            }
        }
    }

    public void open() throws SQLException {
        this.db = SQLiteDatabase.openDatabase(mDbPath + mDbName, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {
        if(this.db != null)
            db.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
