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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.ViewHolder> {
  private static final String  TAG = "DEV_LOG";
  
  public interface OnViewClickListener {
    boolean onMenuItemClick(MenuItem item, int position);
    void    onClick(View view, int position);
  }

  private DatabaseOpenHelper  mDbOpenHelper;
  public  ArrayList<String>   dataset;

  private OnViewClickListener mListener;

  public class ViewHolder extends RecyclerView.ViewHolder implements
      Toolbar.OnMenuItemClickListener, View.OnClickListener {
    public Toolbar  mToolbar;
    public TextView mTextView1;
    public TextView mTextView2;
    public TextView mTextView3;
    public Button   mButton1;
    public Button   mButton2;

    public ViewHolder (View view) {
      super(view);

      mToolbar   = (Toolbar ) view.findViewById(R.id.card_view_toolbar);
      mTextView1 = (TextView) view.findViewById(R.id.text_view_title);
      mTextView2 = (TextView) view.findViewById(R.id.text_view_subtitle);
      mTextView3 = (TextView) view.findViewById(R.id.text_view_supporting_text);
      mButton1   = (Button)   view.findViewById(R.id.btn_like);
      mButton2   = (Button)   view.findViewById(R.id.btn_share);

      mToolbar.inflateMenu(R.menu.menu_card_view_toolbar);
      mButton1.setText(R.string.btn_dislike);

      mToolbar.setOnMenuItemClickListener(this);
      mButton1.setOnClickListener(this);
      mButton2.setOnClickListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
      return mListener.onMenuItemClick(item, getAdapterPosition());
    }

    @Override
    public void onClick(View view) {
      mListener.onClick(view, getAdapterPosition());
    }
  }

  public CardViewAdapter (Context context, ArrayList<String> list) {
    dataset       = list;
    mDbOpenHelper = new DatabaseOpenHelper(context, DatabaseContract.DATABASE_NAME,
        DatabaseContract.DATABASE_VERSION);
  }

  @Override
  public CardViewAdapter.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
    View       view = LayoutInflater.from(parent.getContext())
                          .inflate(R.layout.card_view, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder (ViewHolder vh, int position) {
    //From the DOCS: "RecyclerView will not call this method again if the position of the item 
    //                changes in the data set unless the item itself is invalidated or the new
    //                position cannot be determined. For this reason, you should only use the 
    //                position parameter while acquiring the related data item inside this method 
    //                and should not keep a copy of it."
    
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
        new String[] { dataset.get(position) },
        null,
        null,
        null);
    if(cursor.moveToFirst()) {
      vh.mTextView2.setText("Chapter " + cursor.getString(2) + ", Verse " + cursor.getString(3));
      vh.mTextView3.setText(cursor.getString(4));
    }

    cursor = mDbOpenHelper.db.query(
        DatabaseContract.Table2.TABLE_NAME,
        new String[] {DatabaseContract.Table2.COLUMN_NAME_1},
        DatabaseContract.Table2.COLUMN_NAME_0 + " = ?",
        new String[] { cursor.getString(1) },
        null,
        null,
        null);
    if (cursor.moveToFirst()) {
      vh.mTextView1.setText(cursor.getString(0));
    }

    cursor.close();
    mDbOpenHelper.close();
  }

  @Override
  public int getItemCount () {
    return dataset.size();
  }

  //Handling click-events on the Activity side.
  public void setOnViewClickListener(OnViewClickListener listener) {
    mListener = listener;
  }

  public void remove(int position) {
    dataset.remove(position);
    notifyItemRemoved(position);
  }
}