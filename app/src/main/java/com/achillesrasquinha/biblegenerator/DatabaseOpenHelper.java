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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
  private Context        mContext;
  private String         mDbPath;
  private String         mDbName;
  private int            mDbVersion;
  
  public  SQLiteDatabase db;

  public DatabaseOpenHelper(Context context, String dbName, int version) {
    super(context, dbName, null, version);
    mContext   = context;
    mDbPath    = context.getApplicationInfo().dataDir + "/databases/";
    mDbName    = dbName;
    mDbVersion = version;
  }

  public boolean exists() {
    SQLiteDatabase db = null;

    try {
      db = SQLiteDatabase.openDatabase(mDbPath + mDbName, null, SQLiteDatabase.OPEN_READONLY);
    }
    catch (SQLiteException e) {
      //database does not exist yet.
    }

    if (db != null) {
      db.close();
      return true;
    } else {
      return false;
    }
  }

  public void openDatabase(int flag) throws SQLiteException, IOException {
    if (!exists()) {
      if (flag == SQLiteDatabase.OPEN_READONLY) {
        this.getReadableDatabase();
      } else if (flag == SQLiteDatabase.OPEN_READWRITE) {
        this.getWritableDatabase();
      }

      try {
        InputStream  iStream = mContext.getAssets().open(mDbName);
        OutputStream oStream = new FileOutputStream(mDbPath + mDbName);
        byte[]       buffer  = new byte[1024];
        int          length;

        while ((length = iStream.read(buffer)) > 0) {
          oStream.write(buffer, 0, length);
        }

        iStream.close();
        oStream.flush();
        oStream.close();
      } catch (IOException e) {
        //TO-DO: check whether the streams close, possibility of memory leaks.
        throw e;
      }
    }

    try {
      if (flag == SQLiteDatabase.OPEN_READONLY) {
        db = SQLiteDatabase.openDatabase(mDbPath + mDbName, null,
            SQLiteDatabase.OPEN_READONLY);
      } else if (flag == SQLiteDatabase.OPEN_READWRITE) {
        db = SQLiteDatabase.openDatabase(mDbPath + mDbName, null,
            SQLiteDatabase.OPEN_READWRITE);
      }
    } catch (SQLiteException e) {
      throw e;
    }
  }

  @Override
  public synchronized void close() {
    if (db != null) {
      db.close();
    }
    super.close();
  }

  @Override
  public void onCreate(SQLiteDatabase db) {

  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }
}