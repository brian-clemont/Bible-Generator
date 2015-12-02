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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cocosw.bottomsheet.BottomSheet;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
  private DatabaseOpenHelper      mDbOpenHelper;
  private HashMap<String, String> mHashMap;
  private Button                  mButton;

  private String getText() {
    return
        "\""
        + mHashMap.get(DatabaseContract.Table1.COLUMN_NAME_4)
        + "\""
        + "\n"
        + mHashMap.get(DatabaseContract.Table2.COLUMN_NAME_1)
        + " "
        + mHashMap.get(DatabaseContract.Table1.COLUMN_NAME_2)
        + ":"
        + mHashMap.get(DatabaseContract.Table1.COLUMN_NAME_3)
        + "\n"
        + "\n"
        + "- via "
        + this.getApplicationContext().getResources().getString(R.string.app_name);
  }

  private void updateButtonText() {
    SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
    Cursor cursor;

    cursor = db.query(
        DatabaseContract.Table3.TABLE_NAME,
        null,
        DatabaseContract.Table3.COLUMN_NAME_0 + " = ?",
        new String[]{mHashMap.get(DatabaseContract.Table1.COLUMN_NAME_0)},
        null,
        null,
        null);

    mButton.setText(cursor.moveToFirst() ? "DISLIKE" : "LIKE");

    cursor.close();
    mDbOpenHelper.close(db);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

    AdView lAdView = (AdView) findViewById(R.id.ad_view);
    lAdView.loadAd(new AdRequest.Builder()
        .addTestDevice("B2237171B30BD9744A213A70313165F0")
        .build());

    mDbOpenHelper = new DatabaseOpenHelper(this, DatabaseContract.DATABASE_NAME,
        DatabaseContract.DATABASE_VERSION);

    if (!mDbOpenHelper.create()) {
      //abort application, unable to create database
    }

    mHashMap = new HashMap<String, String>();

    final Resources resources = getResources();

    Toolbar toolbar = (Toolbar) findViewById(R.id.card_view_toolbar);
    if (toolbar != null) {
      toolbar.inflateMenu(R.menu.menu_card_view_toolbar);
      toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          switch (item.getItemId()) {
            case R.id.action_content_copy:
              ClipboardManager clipboardManager = (ClipboardManager)
                  getSystemService(CLIPBOARD_SERVICE);
              clipboardManager.setPrimaryClip(ClipData.newPlainText(
                  resources.getString(R.string.app_name), getText()));

              //issues with coordinator_layout(snackbar + fab), using a toast instead.
              Toast.makeText(getApplicationContext(),
                  resources.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show();
              return true;

            case R.id.action_save:
              final ImageGenerator ig = new ImageGenerator(getApplicationContext());
              new MaterialDialog.Builder(MainActivity.this)
                  .title(R.string.save_as)
                  .items(R.array.image_format)
                  .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog materialDialog, View view, int i,
                                               CharSequence charSequence) {
                      switch (i) {
                        case 0:
                          ig.setCompressFormat(Bitmap.CompressFormat.JPEG);
                          break;

                        case 1:
                          ig.setCompressFormat(Bitmap.CompressFormat.PNG);
                          break;
                      }

                      return true;
                    }
                  })
                  .positiveText(R.string.ok)
                  .negativeText(R.string.cancel)
                  .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog,
                                        DialogAction dialogAction) {
                      //TO DO: create and save bitmap
                      Toast.makeText(getApplicationContext(),
                          resources.getString(R.string.saved_to_gallery), Toast.LENGTH_SHORT)
                          .show();
                    }
                  })
                  .show();

              return true;

            default:
              return false;
          }
        }
      });
    }

    mButton = (Button) findViewById(R.id.btn_like);
    mButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        String id = mHashMap.get(DatabaseContract.Table1.COLUMN_NAME_0);

        if (mButton.getText().equals("LIKE")) {
          ContentValues cv = new ContentValues();
          cv.put(DatabaseContract.Table3.COLUMN_NAME_0,
              Integer.parseInt(id));

          db.insert(DatabaseContract.Table3.TABLE_NAME, null, cv);
        } else {
          db.delete(
              DatabaseContract.Table3.TABLE_NAME,
              DatabaseContract.Table3.COLUMN_NAME_0 + " = ?",
              new String[]{id});
        }

        mDbOpenHelper.close(db);
        updateButtonText();
      }
    });

    findViewById(R.id.btn_share).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        new BottomSheet.Builder(MainActivity.this)
            .title(resources.getString(R.string.share_via))
            .grid()
            .sheet(R.menu.menu_bottom_sheet_share)
            .listener(new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                  case R.id.share_facebook:
                    break;

                  case R.id.share_google_plus:
                    break;

                  case R.id.share_twitter:
                    break;

                  case R.id.share_instagram:
                    if (ThirdPartyApplication.isInstalled(MainActivity.this,
                        "com.instagram.android")) {
                      Intent intent = new Intent(Intent.ACTION_SEND);
                    } else {

                    }

                    break;

                  case R.id.share_whatsapp:
                    if (ThirdPartyApplication.isInstalled(MainActivity.this, "com.whatsapp")) {
                      new BottomSheet.Builder(MainActivity.this)
                          .title(resources.getString(R.string.share_as))
                          .sheet(R.menu.menu_bottom_sheet_share_as)
                          .listener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                              switch(which) {
                                case R.id.share_as_text:
                                  startActivity(new Intent() {{
                                    setAction(Intent.ACTION_SEND);
                                    setPackage("com.whatsapp");
                                    setType("text/plain");
                                    putExtra(Intent.EXTRA_TEXT, getText());
                                  }});
                                  break;

                                case R.id.share_as_image:

                                  break;
                              }
                            }
                          })
                          .show();
                    } else {

                    }

                    break;

                  case R.id.share_message:
                    startActivity(new Intent() {{
                      setAction(Intent.ACTION_VIEW);
                      setData(Uri.parse("sms:"));
                      putExtra("sms_body", getText());
                    }});
                    break;
                }
              }
            })
            .show();
      }
    });

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.btn_generate);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        Cursor cursor;

        cursor = db.rawQuery(DatabaseContract.Table1.QUERY_RANDOM_ROW, null);
        if (cursor.moveToFirst()) {
          for (int i = 0; i < DatabaseContract.Table1.COLUMN_COUNT; ++i)
            mHashMap.put(DatabaseContract.Table1.COLUMN_NAMES[i], cursor.getString(i));
        }

        cursor = db.query(
            DatabaseContract.Table2.TABLE_NAME,
            new String[]{DatabaseContract.Table2.COLUMN_NAME_1},
            DatabaseContract.Table2.COLUMN_NAME_0 + " = ?",
            new String[]{mHashMap.get(DatabaseContract.Table1.COLUMN_NAME_1)},
            null,
            null,
            null);

        if (cursor.moveToFirst()) {
          mHashMap.put(DatabaseContract.Table2.COLUMN_NAME_1, cursor.getString(0));
        }

        TextView tv1 = (TextView) findViewById(R.id.text_view_title);
        tv1.setText(mHashMap.get(DatabaseContract.Table2.COLUMN_NAME_1));
        TextView tv2 = (TextView) findViewById(R.id.text_view_subtitle);
        tv2.setText("Chapter " + mHashMap.get(DatabaseContract.Table1.COLUMN_NAME_2) + ", Verse "
            + mHashMap.get(DatabaseContract.Table1.COLUMN_NAME_3));
        TextView tv3 = (TextView) findViewById(R.id.text_view_supporting_text);
        tv3.setText(mHashMap.get(DatabaseContract.Table1.COLUMN_NAME_4));

        cursor.close();
        mDbOpenHelper.close(db);

        updateButtonText();
      }
    });
    fab.performClick();
  }
}