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
import android.support.design.widget.CoordinatorLayout;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
  private static final boolean DEV_MODE = true;
  private static final String  TAG      = "DEV_LOG";

  private DatabaseOpenHelper      mDbOpenHelper;
  private HashMap<String, String> mHashMap;

  private CoordinatorLayout       mCoordinatorLayout;

  private Toolbar                 mToolbar1;

  private AdRequest.Builder       mAdRequestBuilder;
  private AdView                  mAdView;

  //CardView Widgets/Views
  private Toolbar                 mToolbar2;
  private TextView                mTextView1;
  private TextView                mTextView2;
  private TextView                mTextView3;
  private Button                  mButton1;
  private Button                  mButton2;

  private CardViewHelper          mCardViewHelper;

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

    mCoordinatorLayout = (CoordinatorLayout)    findViewById(R.id.coordinator_layout);
    mToolbar1          = (Toolbar)              findViewById(R.id.toolbar);
    mAdView            = (AdView)               findViewById(R.id.ad_view);
    mToolbar2          = (Toolbar)              findViewById(R.id.card_view_toolbar);
    mTextView1         = (TextView)             findViewById(R.id.text_view_title);
    mTextView2         = (TextView)             findViewById(R.id.text_view_subtitle);
    mTextView3         = (TextView)             findViewById(R.id.text_view_supporting_text);
    mButton1           = (Button)               findViewById(R.id.btn_like);
    mButton2           = (Button)               findViewById(R.id.btn_share);
    mFab               = (FloatingActionButton) findViewById(R.id.btn_generate);
    mDbOpenHelper      = new DatabaseOpenHelper(getApplicationContext(),
        DatabaseContract.DATABASE_NAME, DatabaseContract.DATABASE_VERSION);
    mHashMap           = new HashMap<String, String>();
    mCardViewHelper    = new CardViewHelper(this, mCoordinatorLayout);

    try {
      mDbOpenHelper.openDatabase(SQLiteDatabase.OPEN_READONLY);
    } catch (IOException e) {
      Log.d(TAG, "Unable to copy database.");
      finish();
      System.exit(0);
      //TO-DO: Display dialog error, maybe.
    } catch (SQLiteException e) {
      Log.d(TAG, "Unable to open database.");
      finish();
      System.exit(0);
      //TO-DO: Display dialog error, maybe.
    }
    mDbOpenHelper.close();

    setSupportActionBar(mToolbar1);

    mAdRequestBuilder = new AdRequest.Builder();
    if (DEV_MODE) {
      mAdRequestBuilder.addTestDevice("B2237171B30BD9744A213A70313165F0");
    }

    mAdView.loadAd(mAdRequestBuilder.build());

    //CardView Widgets/Views
    mToolbar2.inflateMenu(R.menu.menu_card_view_toolbar);

    mFab.setOnClickListener(this);
    mFab.performClick();

    //CardViewHelper has an updated HashMap since FAB has been clicked once.
    //Hence no need to refresh each view until clicked.
    mToolbar2.setOnMenuItemClickListener(mCardViewHelper);
    mButton2.setOnClickListener(mCardViewHelper);
    mButton1.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    switch(view.getId()) {
      case R.id.btn_generate:
        try {
          mDbOpenHelper.openDatabase(SQLiteDatabase.OPEN_READONLY);
        } catch(IOException e) {
          //Do nothing, has been handled during onCreate
        } catch(SQLiteException e) {
          Log.d(TAG, "Unable to open database after it exists.");
          //TO-DO: Handle. Display dialog error, maybe.
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
            new String[] {DatabaseContract.Table2.COLUMN_NAME_1},
            DatabaseContract.Table2.COLUMN_NAME_0 + " = ?",
            new String[] {cursor.getString(1)},
            null,
            null,
            null);
        if (cursor.moveToFirst()) {
          mHashMap.put(MapKeys.TITLE, cursor.getString(0));
        }

        mCardViewHelper.setDataset(mHashMap);

        mTextView1.setText(mCardViewHelper.title);
        mTextView2.setText(mCardViewHelper.subtitle);
        mTextView3.setText(mCardViewHelper.text);

        cursor = mDbOpenHelper.db.query(
            DatabaseContract.Table3.TABLE_NAME,
            null,
            DatabaseContract.Table3.COLUMN_NAME_0 + " = ?",
            new String[] {mHashMap.get(MapKeys.ID)},
            null,
            null,
            null);

        mButton1.setText(cursor.moveToFirst() ? R.string.btn_dislike : R.string.btn_like);

        cursor.close();
        mDbOpenHelper.close();

        return;

      case R.id.btn_like:
        try {
          mDbOpenHelper.openDatabase(SQLiteDatabase.OPEN_READWRITE);
        } catch(IOException e) {
          //Do nothing, has been handled during onCreate
        } catch(SQLiteException e) {
          Log.d(TAG, "Unable to open database after it exists.");
          //TO-DO: Handle. Display dialog error, maybe.
        }

        final String ID = mHashMap.get(MapKeys.ID);

        if (mButton1.getText().equals(getString(R.string.btn_like))) {
          ContentValues cv = new ContentValues();
          cv.put(DatabaseContract.Table3.COLUMN_NAME_0, Integer.parseInt(ID));
          mDbOpenHelper.db.insert(DatabaseContract.Table3.TABLE_NAME, null, cv);

          mButton1.setText(R.string.btn_dislike);
        } else {
          mDbOpenHelper.db.delete(
              DatabaseContract.Table3.TABLE_NAME,
              DatabaseContract.Table3.COLUMN_NAME_0 + " = ?",
              new String[] {ID});

          mButton1.setText(R.string.btn_like);
        }

        mDbOpenHelper.close();

        return;
    }
  }
}