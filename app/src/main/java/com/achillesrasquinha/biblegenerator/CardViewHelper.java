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
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cocosw.bottomsheet.BottomSheet;

import java.io.File;
import java.util.HashMap;

public class CardViewHelper {
  public static final int ACTIVITY_MAIN       = 0;
  public static final int ACTIVITY_FAVOURITES = 1;

  public static final int TEXT_VIEW_TITLE     = 2;
  public static final int TEXT_VIEW_SUBTITLE  = 3;
  public static final int TEXT_VIEW_TEXT      = 4;

  public static final int BUTTON_SHARE        = 5;
  public static final int BUTTON_LIKE         = 6;

  private Context                 mContext;
  private DatabaseOpenHelper      mDbOpenHelper;
  private HashMap<String, String> mHashMap;

  public CardViewHelper(Context context, HashMap<String, String> map) {
    mContext      = context;
    //Database has been created while initiating the Application, no need to recreate again.
    mDbOpenHelper = new DatabaseOpenHelper(context, DatabaseContract.DATABASE_NAME,
        DatabaseContract.DATABASE_VERSION);
    mHashMap      = map;
  }

  private String getText () {
    return "\""
        + mHashMap.get(MapKeys.TEXT)
        + "\""
        + "\n"
        + mHashMap.get(MapKeys.TITLE)
        + " "
        + mHashMap.get(MapKeys.CHAPTER)
        + ":"
        + mHashMap.get(MapKeys.VERSE)
        + "\n"
        + "\n"
        + "- via "
        + mContext.getString(R.string.app_name);
  }

  public void setTextView(TextView tv, int type) {
    switch(type) {
      case TEXT_VIEW_TITLE:
        tv.setText(mHashMap.get(MapKeys.TITLE));
        return;

      case TEXT_VIEW_SUBTITLE:
        tv.setText("Chapter " + mHashMap.get(MapKeys.CHAPTER) + ", Verse " +
            mHashMap.get(MapKeys.VERSE));
        return;

      case TEXT_VIEW_TEXT:
        tv.setText(mHashMap.get(MapKeys.TEXT));
        return;

      default:
        return;
    }
  }

  public void setToolbar(Toolbar toolbar) {
    toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick (MenuItem item) {
        switch(item.getItemId()) {
          case R.id.action_content_copy:
            ClipboardManager cm = (ClipboardManager) mContext.getSystemService(
                mContext.CLIPBOARD_SERVICE);
            ClipData         cd = ClipData.newPlainText(mContext.getString(R.string.app_name),
                getText());
            cm.setPrimaryClip(cd);

            //TO-DO: Snackbar instead Toast
            Toast.makeText(mContext, mContext.getString(R.string.copied_to_clipboard),
                Toast.LENGTH_SHORT);

            return true;

          case R.id.action_save:
            final FileManager fm = new FileManager(mContext);
            new MaterialDialog.Builder(mContext)
                .title(R.string.save_as)
                .items(R.array.image_format)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                  @Override
                  public boolean onSelection (MaterialDialog materialDialog, View view, int i,
                                              CharSequence charSequence) {
                    switch (i) {
                      case 0:
                        fm.setCompressFormat(Bitmap.CompressFormat.JPEG);
                        break;

                      case 1:
                        fm.setCompressFormat(Bitmap.CompressFormat.PNG);
                        break;
                    }

                    return true;
                  }
                })
                .positiveText(R.string.btn_dialog_save)
                .negativeText(R.string.btn_dialog_cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                  @Override
                  public void onClick (MaterialDialog materialDialog, DialogAction dialogAction) {
                    ImageGenerator ig     = new ImageGenerator(mContext);
                    Bitmap         bitmap = ig.getBitmap(
                        mHashMap.get(MapKeys.TITLE),
                        mHashMap.get("Chapter " + MapKeys.CHAPTER + ", Verse " +
                            MapKeys.VERSE),
                        mHashMap.get(MapKeys.TEXT)
                    );

                    File file = fm.saveBitmap(bitmap);

                    if (file != null) {
                      Toast.makeText(mContext, R.string.saved_to_gallery,
                          Toast.LENGTH_SHORT);
                    } else {
                      //TO-DO: Snackbar instead Toast
                      //TO-DO: Find and describe reason for not being able to save file,
                      //       eg. storage full
                      Toast.makeText(mContext, R.string.unable_to_save_file,
                          Toast.LENGTH_SHORT);
                    }
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

  public void setButton(Button button, int type) {
    switch(type) {
      case ACTIVITY_FAVOURITES | BUTTON_LIKE:
        button.setText(mContext.getString(R.string.btn_dislike));

        return;

      case BUTTON_SHARE:
        button.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick (View view) {
            new BottomSheet.Builder((Activity) mContext)
                .title(R.string.bottom_sheet_share_title)
                .grid()
                .sheet(R.menu.menu_bottom_sheet_share)
                .listener(new Dialog.OnClickListener() {
                  @Override
                  public void onClick (DialogInterface dialog, int which) {
                    switch (which) {
                      case R.id.action_share_facebook:
                        //TO-DO: Intent for sharing via Facebook, may require SDK
                        break;

                      case R.id.action_share_google_plus:
                        //TO-DO: Intent for sharing via Google+, may require SDK
                        break;

                      case R.id.action_share_twitter:
                        //TO-DO: Intent for sharing via Twitter, including 3rd party apps.
                        break;

                      case R.id.action_share_instagram:
                        if (ThirdPartyApplication.isInstalled(mContext,
                            ThirdPartyApplication.PackageName.INSTAGRAM)) {
                          ImageGenerator ig = new ImageGenerator(mContext);
                          Bitmap bitmap = ig.getBitmap(
                              mHashMap.get(MapKeys.TITLE),
                              mHashMap.get("Chapter " + MapKeys.CHAPTER + ", Verse " + MapKeys.VERSE),
                              mHashMap.get(MapKeys.TEXT)
                          );

                          FileManager fm = new FileManager(mContext);
                          File file      = fm.saveBitmap(bitmap);

                          if (file != null) {
                            final Uri URI = Uri.fromFile(file);
                            mContext.startActivity(new Intent() {{
                              setAction(Intent.ACTION_SEND);
                              setPackage(ThirdPartyApplication.PackageName.INSTAGRAM);
                              setType("image/*");
                              putExtra(Intent.EXTRA_STREAM, URI);
                            }});

                            //TO-DO: Snackbar instead Toast
                            Toast.makeText(mContext, R.string.saved_to_gallery, Toast.LENGTH_SHORT);
                          } else {
                            //TO-DO: Snackbar instead Toast
                            //TO-DO: Find and describe reason for not being able to save file,
                            //       eg. storage full
                            Toast.makeText(mContext, R.string.unable_to_save_file,
                                Toast.LENGTH_SHORT);
                          }

                        } else {
                          //TO-DO: Snackbar instead Toast
                          Toast.makeText(mContext, R.string.you_dont_have_instagram_installed,
                              Toast.LENGTH_SHORT);
                        }

                        break;

                      case R.id.action_share_whatsapp:
                        if (ThirdPartyApplication.isInstalled(mContext,
                            ThirdPartyApplication.PackageName.WHATSAPP)) {
                          new BottomSheet.Builder((Activity) mContext)
                              .title(R.string.share_as)
                              .sheet(R.menu.menu_bottom_sheet_share_as)
                              .listener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick (DialogInterface dialog, int which) {
                                  switch (which) {
                                    case R.id.share_as_text:
                                      mContext.startActivity(new Intent() {{
                                        setAction(Intent.ACTION_SEND);
                                        setPackage(ThirdPartyApplication.PackageName.WHATSAPP);
                                        setType("text/plain");
                                        putExtra(Intent.EXTRA_TEXT, getText());
                                      }});

                                      break;

                                    case R.id.share_as_image:
                                      ImageGenerator ig = new ImageGenerator(mContext);
                                      Bitmap bitmap = ig.getBitmap(
                                          mHashMap.get(MapKeys.TITLE),
                                          mHashMap.get("Chapter " + MapKeys.CHAPTER + ", Verse " +
                                              MapKeys.VERSE),
                                          mHashMap.get(MapKeys.TEXT)
                                      );

                                      FileManager fm = new FileManager(mContext);
                                      File file = fm.saveBitmap(bitmap);

                                      if (file != null) {
                                        final Uri URI = Uri.fromFile(file);
                                        mContext.startActivity(new Intent() {{
                                          setAction(Intent.ACTION_SEND);
                                          setPackage(ThirdPartyApplication.PackageName.WHATSAPP);
                                          setType("image/*");
                                          putExtra(Intent.EXTRA_STREAM, URI);

                                          Toast.makeText(mContext, R.string.saved_to_gallery,
                                              Toast.LENGTH_SHORT);
                                        }});
                                      } else {
                                        //TO-DO: Snackbar instead Toast
                                        //TO-DO: Find and describe reason for not being able to save file,
                                        //       eg. storage full
                                        Toast.makeText(mContext, R.string.unable_to_save_file,
                                            Toast.LENGTH_SHORT);
                                      }
                                  }
                                }
                              })
                              .show();
                        } else {
                          //TO-DO: Snackbar instead Toast
                          Toast.makeText(mContext, R.string.you_dont_have_whatsapp_installed,
                              Toast.LENGTH_SHORT);
                        }

                        break;

                      case R.id.action_share_message:
                        mContext.startActivity(new Intent() {{
                          setAction(Intent.ACTION_VIEW);
                          setData(Uri.parse("sms:"));
                          putExtra("sms_body", getText());
                        }});
                    }
                  }
                })
                .show();

          }
        });

        break;
    }
  }
}