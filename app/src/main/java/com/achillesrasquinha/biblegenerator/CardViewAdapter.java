package com.achillesrasquinha.biblegenerator;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.ViewHolder> {
  public ArrayList<String>  list;
  public DatabaseOpenHelper mDbOpenHelper;
  public Context            mContext;

  public class ViewHolder extends RecyclerView.ViewHolder {
    public Toolbar toolbar;
    public TextView tv1;
    public TextView tv2;
    public TextView tv3;
    public Button   button1;
    public Button   button2;

    public ViewHolder (View view) {
      super(view);
      toolbar = (Toolbar) view.findViewById(R.id.card_view_toolbar);
      toolbar.inflateMenu(R.menu.menu_card_view_toolbar);
      tv1     = (TextView) view.findViewById(R.id.text_view_title);
      tv2     = (TextView) view.findViewById(R.id.text_view_subtitle);
      tv3     = (TextView) view.findViewById(R.id.text_view_supporting_text);
      button1 = (Button) view.findViewById(R.id.btn_like);
      button2 = (Button) view.findViewById(R.id.btn_share);
    }
  }

  public CardViewAdapter (Context context, ArrayList<String> list) {
    this.list = list;
    mDbOpenHelper = new DatabaseOpenHelper(context, DatabaseContract.DATABASE_NAME,
        DatabaseContract.DATABASE_VERSION);
  }

  @Override
  public CardViewAdapter.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
    return new ViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.card_view, parent, false));
  }

  @Override
  public void onBindViewHolder (ViewHolder vh, int position) {
    SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
    Cursor cursor;

    cursor = db.query(
        DatabaseContract.Table1.TABLE_NAME,
        null,
        DatabaseContract.Table1.COLUMN_NAME_0 + " = ?",
        new String[] { list.get(position) },
        null,
        null,
        null);


    HashMap<String, String> map = new HashMap<String, String>();

    if (cursor.moveToFirst()) {
      for (int i = 0 ; i < DatabaseContract.Table1.COLUMN_COUNT ; ++i)
        map.put(DatabaseContract.Table1.COLUMN_NAME[i], cursor.getString(i));
    }

    cursor = db.query(
        DatabaseContract.Table2.TABLE_NAME,
        new String[] {DatabaseContract.Table2.COLUMN_NAME_1},
        DatabaseContract.Table2.COLUMN_NAME_0 + " = ?",
        new String[] {map.get(DatabaseContract.Table1.COLUMN_NAME_1)},
        null,
        null,
        null);

    if (cursor.moveToFirst()) {
      map.put(DatabaseContract.Table2.COLUMN_NAME_1, cursor.getString(0));
    }

    //CardViewHelper helper = new CardViewHelper(mContext, map);

    //helper.setToolbar(vh.toolbar);

    //helper.setTextView(vh.tv1, CardViewHelper.TEXT_VIEW_TITLE);
    //helper.setTextView(vh.tv1, CardViewHelper.TEXT_VIEW_SUBTITLE);
    //helper.setTextView(vh.tv1, CardViewHelper.TEXT_VIEW_TEXT);

    //helper.setButton(vh.button2, CardViewHelper.BUTTON_SHARE);

    cursor.close();
    db.close();
  }

  @Override
  public int getItemCount () {
    return list.size();
  }
}
