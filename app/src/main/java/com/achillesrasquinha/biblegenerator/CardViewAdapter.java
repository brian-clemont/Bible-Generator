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

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.ViewHolder> {
  private static final String  TAG = "DEV_LOG";

  private Context                 mContext;
  private ArrayList<String>       mArrayList;
  private DatabaseOpenHelper      mDbOpenHelper;
  private CoordinatorLayout       mCoordinatorLayout;
  private HashMap<String, String> mHashMap;

  //use sensitively.
  private CardViewHelper          mCardViewHelper;

  private MaterialDialog.Builder  mDialogBuilder;

  public class ViewHolder extends RecyclerView.ViewHolder implements
      Toolbar.OnMenuItemClickListener {
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
        
      vh.mButton1.setText(R.string.btn_dislike);
    }

    @Override
    public void onMenuItemClick(MenuItem item) {

    }
  }

  public CardViewAdapter (Context context, ArrayList<String> list, CoordinatorLayout layout) {
    mContext           = context;
    mArrayList         = list;
    mDbOpenHelper      = new DatabaseOpenHelper(context, DatabaseContract.DATABASE_NAME,
        DatabaseContract.DATABASE_VERSION);
    mCoordinatorLayout = layout;
    mHashMap           = new HashMap<String, String>();

    mCardViewHelper    = new CardViewHelper(context, layout);

    mDialogBuilder     = new MaterialDialog.Builder((Activity) mContext)
        .title(R.string.dialog_title_remove_from_favourites)
        .positiveText(R.string.btn_dialog_remove)
        .negativeText(R.string.btn_dialog_cancel);
  }

  @Override
  public CardViewAdapter.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
    View       view = LayoutInflater.from(parent.getContext());    
    ViewHolder vh   = new ViewHolder(view);

    return vh;
  }

  @Override
  public void onBindViewHolder (ViewHolder vh, int position) {
    //From the Docs: "RecyclerView will not call this method again if the position of the item 
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
        new String[] { mArrayList.get(position) },
        null,
        null,
        null);
    if (cursor.moveToFirst()) {
      mHashMap.put(MapKeys.CHAPTER, cursor.getString(2));
      mHashMap.put(MapKeys.VERSE  , cursor.getString(3));
      mHashMap.put(MapKeys.TEXT   , cursor.getString(4));
    }

    cursor = mDbOpenHelper.db.query(
        DatabaseContract.Table2.TABLE_NAME,
        new String[] {DatabaseContract.Table2.COLUMN_NAME_1},
        DatabaseContract.Table2.COLUMN_NAME_0 + " = ?",
        new String[] { cursor.getString(0) },
        null,
        null,
        null);
    if (cursor.moveToFirst()) {
      mHashMap.put(MapKeys.TITLE, cursor.getString(0));
    }

    mCardViewHelper.setDataset(mHashMap);

    vh.mTextView1.setText(helper.title);
    vh.mTextView2.setText(helper.subtitle);
    vh.mTextView3.setText(helper.text);

    cursor.close();
    mDbOpenHelper.close();
  }

  @Override
  public int getItemCount () {
    return mArrayList.size();
  }
}