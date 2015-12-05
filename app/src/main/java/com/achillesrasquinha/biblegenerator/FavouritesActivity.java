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
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class FavouritesActivity extends AppCompatActivity implements CardViewAdapter.OnViewClickListener {
  private DatabaseOpenHelper mDbOpenHelper;
  private ArrayList<String>  mArrayList;

  private Toolbar            mToolbar;
  private AdRequest.Builder  mAdRequestBuilder;
  private AdView             mAdView;

  private RecyclerView       mRecyclerView;
  private CardViewAdapter    mCardViewAdapter;

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

  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_favourites);

    mToolbar = (Toolbar) findViewById(R.id.toolbar);
    mAdView  = (AdView)  findViewById(R.id.ad_view);

    setSupportActionBar(mToolbar);

    mAdRequestBuilder = new AdRequest.Builder();
    if (DEV_MODE) {

    }




    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

    AdView lAdView = (AdView) findViewById(R.id.ad_view);
    lAdView.loadAd(new AdRequest.Builder().addTestDevice("B2237171B30BD9744A213A70313165F0")
        .build());

    mDbOpenHelper = new DatabaseOpenHelper(this, DatabaseContract.DATABASE_NAME,
        DatabaseContract.DATABASE_VERSION);

    SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
    Cursor cursor;

    cursor = db.rawQuery("SELECT * " + " FROM " + DatabaseContract.Table1.TABLE_NAME + " WHERE " +
        DatabaseContract.Table1.COLUMN_NAME_0 + " IN (SELECT " +
        DatabaseContract.Table3.COLUMN_NAME_0 + " FROM " + DatabaseContract.Table3.TABLE_NAME +
        ")", null);

    ArrayList<String> mArrayList = new ArrayList<String>();
    if (cursor.moveToFirst()) {
      do {
        mArrayList.add(cursor.getString(0));
      }
      while (cursor.moveToNext());
    }

    RecyclerView rv = (RecyclerView) findViewById(R.id.recycler_view);
    rv.setLayoutManager(new LinearLayoutManager(this));
    rv.setAdapter(new CardViewAdapter(this, mArrayList));


    mCardViewAdapter.setOnViewClickListener(this);
  }

  @Override
  public void onMenuItemClick(Menu item, int position) {

  }

  @Override
  public void onClick(View view, int position) {

  }
}