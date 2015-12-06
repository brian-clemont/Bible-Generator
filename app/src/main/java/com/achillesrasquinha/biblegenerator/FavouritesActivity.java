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

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class FavouritesActivity extends AppCompatActivity implements 
    CardViewAdapter.OnViewClickListener {
  private static final boolean DEV_MODE = true;
  private static final String  TAG      = "DEV_LOG";

  private CoordinatorLayout       mCoordinatorLayout;
  private Toolbar                 mToolbar;
  private AdRequest.Builder       mAdRequestBuilder;
  private AdView                  mAdView;
  private RecyclerView            mRecyclerView;
  
  private DatabaseOpenHelper      mDbOpenHelper;
  
  private CardViewAdapter         mCardViewAdapter;
  
  private CardViewHelper          mCardViewHelper;
  private HashMap<String, String> mHashMap;

  private MaterialDialog.Builder  mDialogBuilder;

  @Override
  public boolean onCreateOptionsMenu (Menu menu) {
    getMenuInflater().inflate(R.menu.menu_favourites, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected (MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_about:
        startActivity(new Intent() {{
          setClass(FavouritesActivity.this, AboutActivity.class);
        }});
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_favourites);
    
    mCoordinatorLayout = (CoordinatorLayout)    findViewById(R.id.coordinator_layout);
    mToolbar           = (Toolbar)      findViewById(R.id.toolbar);
    mAdView            = (AdView)       findViewById(R.id.ad_view);
    mRecyclerView      = (RecyclerView) findViewById(R.id.recycler_view);
    mDbOpenHelper      = new DatabaseOpenHelper(this, DatabaseContract.DATABASE_NAME,
        DatabaseContract.DATABASE_VERSION);
    mHashMap           = new HashMap<>();
    mCardViewHelper    = new CardViewHelper(this, mCoordinatorLayout);
    mDialogBuilder     = new MaterialDialog.Builder(this)
        .title(R.string.dialog_title_remove_from_favourites)
        .positiveText(R.string.btn_dialog_remove)
        .negativeText(R.string.btn_dialog_cancel);

    setSupportActionBar(mToolbar);

    mAdRequestBuilder = new AdRequest.Builder();
    if (DEV_MODE) {
        mAdRequestBuilder.addTestDevice("B2237171B30BD9744A213A70313165F0");
    }

    mAdView.loadAd(mAdRequestBuilder.build());

    try {
      mDbOpenHelper.openDatabase(SQLiteDatabase.OPEN_READONLY);
    } catch(IOException e) {
      //Do nothing, has been handled during MainActivity.onCreate
    } catch(SQLiteException e) {
      Log.d(TAG, "Unable to open database after it exists.");
      //TO-DO: Handle. Display dialog error, maybe.
    }


    ArrayList<String> list   = new ArrayList<>();
    Cursor            cursor = mDbOpenHelper.db.query(
        DatabaseContract.Table3.TABLE_NAME,
        new String[] { DatabaseContract.Table3.COLUMN_NAME_0 },
        null,
        null,
        null,
        null,
        null,
        null);
    if (cursor.moveToFirst()) {
      do {
        list.add(cursor.getString(0));
      }
      while (cursor.moveToNext());
    }

    mCardViewAdapter = new CardViewAdapter(this, list);
    mCardViewAdapter.setOnViewClickListener(this);

    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    mRecyclerView.setAdapter(mCardViewAdapter);

    cursor.close();
    mDbOpenHelper.close();

    //adding a dummy value to MapKeys.ID
    mHashMap.put(MapKeys.ID, "-1");
  }

  @Override
  public boolean onMenuItemClick(MenuItem item, int position) {
    if (updateDataset(position)) {
      mCardViewHelper.setDataset(mHashMap);
      return mCardViewHelper.onMenuItemClick(item);
    } else {
      Log.d(TAG, "Updating hash map for " + position + " was unsuccessful.");
      return false;
    }
  }

  @Override
  public void onClick(View view, int position) {
    final int POSITION = position;
    switch(view.getId()) {
      case R.id.btn_share:
        if (updateDataset(POSITION)) {
          mCardViewHelper.setDataset(mHashMap);
          mCardViewHelper.onClick(view);
        }

        break;

      case R.id.btn_like:
        mDialogBuilder
          .onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, DialogAction which) {
              try {
                mDbOpenHelper.openDatabase(SQLiteDatabase.OPEN_READWRITE);
              } catch(IOException e) {
                //Do nothing, has been handled during MainActivity.onCreate
              } catch(SQLiteException e) {
                Log.d(TAG, "Unable to open database after it exists.");
                //TO-DO: Handle. Display dialog error, maybe.
              }

              if (DEV_MODE) {
                if(mDbOpenHelper.getWritableDatabase() != null) {
                  Log.d(TAG, "Database accessible within callback.");
                }
              }

              mDbOpenHelper.db.delete(
                  DatabaseContract.Table3.TABLE_NAME,
                  DatabaseContract.Table3.COLUMN_NAME_0 + " = ?",
                  new String[] { mCardViewAdapter.dataset.get(POSITION) });

              mCardViewAdapter.remove(POSITION);
            }
          })
          .show();
        break;
    }
  }

  public boolean updateDataset(int position) {
    final String ID = mCardViewAdapter.dataset.get(position);

    if (mHashMap.get(MapKeys.ID).equals(ID)) {
      return true;
    }

    try {
      mDbOpenHelper.openDatabase(SQLiteDatabase.OPEN_READONLY);
    } catch(IOException e) {
      //Do nothing, has been handled during MainActivity.onCreate
    } catch(SQLiteException e) {
      Log.d(TAG, "Unable to open database after it exists.");
      //TO-DO: Handle. Display dialog error, maybe.
    }

    Cursor cursor;

    cursor = mDbOpenHelper.db.query(
        DatabaseContract.Table1.TABLE_NAME,
        null,
        DatabaseContract.Table1.COLUMN_NAME_0 + " = ?",
        new String[] { ID },
        null,
        null,
        null);
    if (cursor.moveToFirst()) {
      mHashMap.put(MapKeys.ID     , cursor.getString(0));
      mHashMap.put(MapKeys.CHAPTER, cursor.getString(2));
      mHashMap.put(MapKeys.VERSE  , cursor.getString(3));
      mHashMap.put(MapKeys.TEXT   , cursor.getString(4));
    } else {
      //TO-DO: check whether adapter position equals position within dataset.
      return false;
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

    cursor.close();
    mDbOpenHelper.close();

    return true;
  }
}