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

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = "DEV_LOG";

  private DatabaseOpenHelper      mDbOpenHelper;
  private HashMap<String, String> mHashMap;

  private Toolbar                 mToolbar1;
  private AdView                  mAdView;

  //CardView Widgets/Views
  private Toolbar                 mToolbar2;
  private TextView mTextView1;
  private TextView                mTextView2;
  private TextView                mTextView3;
  private Button                  mButton1;
  private Button                  mButton2;

  private FloatingActionButton    mFab;

  @Override
  public boolean onCreateOptionsMenu (Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_favourites:
        startActivity(new Intent() {{
          setClass(MainActivity.this, FavouritesActivity.class);
        }});
        return true;

      case R.id.action_about:
        startActivity(new Intent() {{
          setClass(MainActivity.this, AboutActivity.class);
        }});
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mToolbar1     = (Toolbar)              findViewById(R.id.toolbar);
    mAdView       = (AdView)               findViewById(R.id.ad_view);
    mToolbar2     = (Toolbar)              findViewById(R.id.card_view_toolbar);
    mTextView1    = (TextView)             findViewById(R.id.text_view_title);
    mTextView2    = (TextView)             findViewById(R.id.text_view_subtitle);
    mTextView3    = (TextView)             findViewById(R.id.text_view_supporting_text);
    mButton1      = (Button)               findViewById(R.id.btn_like);
    mButton2      = (Button)               findViewById(R.id.btn_share);
    mFab          = (FloatingActionButton) findViewById(R.id.btn_generate);
    mDbOpenHelper = new DatabaseOpenHelper(getApplicationContext(), DatabaseContract.DATABASE_NAME,
        DatabaseContract.DATABASE_VERSION);
    mHashMap      = new HashMap<String, String>();

    setSupportActionBar(mToolbar1);

    mAdView.loadAd(new AdRequest.Builder().addTestDevice("B2237171B30BD9744A213A70313165F0").build());

    mToolbar2.inflateMenu(R.menu.menu_card_view_toolbar);

    mFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick (View v) {
        try {
          mDbOpenHelper.openDatabase(SQLiteDatabase.OPEN_READONLY);
        } catch (IOException e) {
          Log.d(TAG, "Unable to copy database.");
          //TO-DO: Handle. Display error, maybe.
        } catch (SQLiteException e) {
          Log.d(TAG, "Unable to open database.");
          //TO-DO: Handle. Display error, maybe.
        }

        Cursor cursor;

        cursor = mDbOpenHelper.db.rawQuery(DatabaseContract.Table1.QUERY_RANDOM_ROW, null);
        if (cursor.moveToFirst()) {
          mHashMap.put(MapKeys.ID     , cursor.getString(0));
          mHashMap.put(MapKeys.CHAPTER, cursor.getString(2));
          mHashMap.put(MapKeys.VERSE  , cursor.getString(3));
          mHashMap.put(MapKeys.TEXT   , cursor.getString(4));
        }

        cursor = mDbOpenHelper.db.query(
            DatabaseContract.Table2.TABLE_NAME,
            new String[] { DatabaseContract.Table2.COLUMN_NAME_1 },
            DatabaseContract.Table2.COLUMN_NAME_0 + " = ?",
            new String[] { cursor.getString(1) },
            null,
            null,
            null);
        if (cursor.moveToFirst()) {
          mHashMap.put(MapKeys.TITLE, cursor.getString(0));
        }

        CardViewHelper helper = new CardViewHelper(MainActivity.this, mHashMap);

        helper.setToolbar (mToolbar2);
        helper.setTextView(mTextView1, CardViewHelper.TEXT_VIEW_TITLE);
        helper.setTextView(mTextView2, CardViewHelper.TEXT_VIEW_SUBTITLE);
        helper.setTextView(mTextView3, CardViewHelper.TEXT_VIEW_TEXT);
        helper.setButton  (mButton2  , CardViewHelper.BUTTON_SHARE);

        cursor = mDbOpenHelper.db.query(
            DatabaseContract.Table3.TABLE_NAME,
            null,
            DatabaseContract.Table3.COLUMN_NAME_0 + " = ?",
            new String[] { mHashMap.get(MapKeys.ID) },
            null,
            null,
            null);

        mButton1.setText(getString(cursor.moveToFirst() ?  R.string.btn_dislike
            : R.string.btn_like));

        cursor.close();
        mDbOpenHelper.close();
      }
    });
    mFab.performClick();

    mButton1.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        try {
          mDbOpenHelper.openDatabase(SQLiteDatabase.OPEN_READWRITE);
        } catch (IOException e) {
          Log.d(TAG, "Unable to copy database.");
          //TO-DO: Handle. Display error, maybe.
        } catch (SQLiteException e) {
          Log.d(TAG, "Unable to open database.");
          //TO-DO: Handle. Display error, maybe.
        }

        final String ID = mHashMap.get(MapKeys.ID);

        if (mButton1.getText().equals(getString(R.string.btn_like))) {

          ContentValues cv = new ContentValues();
          cv.put(DatabaseContract.Table3.COLUMN_NAME_0, Integer.parseInt(ID));
          mDbOpenHelper.db.insert(DatabaseContract.Table3.TABLE_NAME, null, cv);

          mButton1.setText(getString(R.string.btn_dislike));
        } else {
          mDbOpenHelper.db.delete(
              DatabaseContract.Table3.TABLE_NAME,
              DatabaseContract.Table3.COLUMN_NAME_0 + " = ?",
              new String[] { ID });

          mButton1.setText(getString(R.string.btn_like));
        }

        mDbOpenHelper.close();
      }
    });
  }
}