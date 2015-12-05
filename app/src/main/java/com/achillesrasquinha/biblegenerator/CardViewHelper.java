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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
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

  private Context                 mContext1;
  private Context                 mContext2;
  private HashMap<String, String> mHashMap;
  private CoordinatorLayout       mCoordinatorLayout;

  private ClipboardManager        mClipboardManager;
  private FileManager             mFileManager;

  private MaterialDialog.Builder  mDialogBuilder1;

  public CardViewHelper(Context context, CoordinatorLayout layout) {
    mContext1          = context;
    mContext2          = context.getApplicationContext();
    mCoordinatorLayout = layout;

    mClipboardManager  = (ClipboardManager) mContext2.getSystemService(
        mContext2.CLIPBOARD_SERVICE);
    mFileManager       = new FileManager(mContext2);

    mDialogBuilder1    = new MaterialDialog.Builder(mContext1);
    mDialogBuilder1.title(R.string.save_as);
    mDialogBuilder1.items(R.array.image_format);
    mDialogBuilder1.positiveText(R.string.btn_dialog_save);
    mDialogBuilder1.negativeText(R.string.btn_dialog_cancel);

  }

  public void setDataset(HashMap<String, String> map) {
    mHashMap = map;
  }

  private String getPlainText() {
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
        + mContext2.getString(R.string.app_name);
  }

  public String getText(int type) {
    switch(type) {
      case TEXT_VIEW_TITLE:
        return mHashMap.get(MapKeys.TITLE);

      case TEXT_VIEW_SUBTITLE:
        return "Chapter " + mHashMap.get(MapKeys.CHAPTER) + ", Verse " +
            mHashMap.get(MapKeys.VERSE);

      case TEXT_VIEW_TEXT:
        return mHashMap.get(MapKeys.TEXT);

      default:
        return null;
    }
  }

  public Toolbar.OnMenuItemClickListener getToolbarOnMenuItemClickListener() {
    final String  TITLE     = mHashMap.get(MapKeys.TITLE);
    final String  CHAPTER   = mHashMap.get(MapKeys.CHAPTER);
    final String  VERSE     = mHashMap.get(MapKeys.VERSE);
    final String  TEXT      = mHashMap.get(MapKeys.TEXT);
    return new Toolbar.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick (MenuItem item) {
        switch(item.getItemId()) {
          case R.id.action_content_copy:
            ClipData cd = ClipData.newPlainText(mContext2.getString(
              R.string.message_copied_to_clipboard), getPlainText());
              mClipboardManager.setPrimaryClip(cd);

            Snackbar.make(mCoordinatorLayout, R.string.message_copied_to_clipboard,
                Snackbar.LENGTH_SHORT).show();

            return true;

          case R.id.action_save:
            final FileManager       FM = mFileManager;
            final CoordinatorLayout CL = mCoordinatorLayout;
            mDialogBuilder1
              .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                  @Override
                  public boolean onSelection (MaterialDialog materialDialog, View view, int i,
                                              CharSequence charSequence) {
                    switch (i) {
                      case 0:
                        FM.setCompressFormat(Bitmap.CompressFormat.JPEG);
                        break;

                      case 1:
                        FM.setCompressFormat(Bitmap.CompressFormat.PNG);
                        break;
                    }

                    return true;
                  }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                  @Override
                  public void onClick (MaterialDialog materialDialog, DialogAction dialogAction) {
                    ImageGenerator ig = new ImageGenerator(mContext2);
                    Bitmap bitmap     = ig.getBitmap(TITLE, "Chapter " + CHAPTER + ", Verse " + 
                      VERSE, TEXT);

                    File file = FM.saveBitmap(bitmap);

                    if (file != null) {
                      Snackbar.make(mCoordinatorLayout, R.string.saved_to_gallery,
                          Snackbar.LENGTH_SHORT).show();
                    } else {
                      //TO-DO: Find and describe reason for not being able to save file,
                      //       eg. storage full
                      Snackbar.make(CL, R.string.unable_to_save_file,
                          Snackbar.LENGTH_SHORT).show();
                    }
                  }
                })
                .show();

            return true;

          default:
            return false;
        }
      }
    };
  }

  public View.OnClickListener getOnClickListener(int type) {
    final String TITLE   = this.mHashMap.get(MapKeys.TITLE);
    final String CHAPTER = this.mHashMap.get(MapKeys.CHAPTER);
    final String VERSE   = this.mHashMap.get(MapKeys.VERSE);
    final String TEXT    = this.mHashMap.get(MapKeys.TEXT);
    switch(type) {
      case BUTTON_SHARE:
        return new View.OnClickListener() {
          @Override
          public void onClick (View view) {
            new BottomSheet.Builder((Activity) mContext1)
                .title(R.string.bottom_sheet_share_title)
                .grid()
                .sheet(R.menu.menu_bottom_sheet_share)
                .listener(new Dialog.OnClickListener() {
                  @Override
                  public void onClick (DialogInterface dialog, int which) {
                    switch (which) {
                      case R.id.action_share_facebook:
                        //TO-DO: Intent for sharing via Facebook, may require SDK
                        return;

                      case R.id.action_share_google_plus:
                        //TO-DO: Intent for sharing via Google+, may require SDK
                        return;

                      case R.id.action_share_twitter:
                        //TO-DO: Intent for sharing via Twitter, including 3rd party apps.
                        return;

                      case R.id.action_share_instagram:
                        if (ThirdPartyApplication.isInstalled(mContext2,
                            ThirdPartyApplication.PackageName.INSTAGRAM)) {
                          ImageGenerator ig = new ImageGenerator(mContext1);
                          Bitmap bitmap     = ig.getBitmap(TITLE, "Chapter " + CHAPTER + ", Verse " 
                              + VERSE, TEXT);

                          FileManager fm = new FileManager(mContext2);
                          File file      = fm.saveBitmap(bitmap);

                          if (file != null) {
                            final Uri URI = Uri.fromFile(file);
                            mContext1.startActivity(new Intent() {{
                              setAction(Intent.ACTION_SEND);
                              setPackage(ThirdPartyApplication.PackageName.INSTAGRAM);
                              setType("image/*");
                              putExtra(Intent.EXTRA_STREAM, URI);
                            }});

                            //TO-DO: Snackbar instead Toast
                            Toast.makeText(mContext2, 
                                mContext2.getString(R.string.saved_to_gallery), 
                                Toast.LENGTH_SHORT);
                          } else {
                            //TO-DO: Snackbar instead Toast
                            //TO-DO: Find and describe reason for not being able to save file,
                            //       eg. storage full
                            Toast.makeText(mContext2, 
                                mContext2.getString(R.string.unable_to_save_file),
                                Toast.LENGTH_SHORT);
                          }

                        } else {
                          //TO-DO: Snackbar instead Toast
                          Toast.makeText(mContext2, 
                              mContext2.getString(R.string.you_dont_have_instagram_installed),
                              Toast.LENGTH_SHORT);
                        }

                        return;

                      case R.id.action_share_whatsapp:
                        if (ThirdPartyApplication.isInstalled(mContext2,
                            ThirdPartyApplication.PackageName.WHATSAPP)) {
                          new BottomSheet.Builder((Activity) mContext1)
                              .title(R.string.share_as)
                              .sheet(R.menu.menu_bottom_sheet_share_as)
                              .listener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick (DialogInterface dialog, int which) {
                                  switch (which) {
                                    case R.id.share_as_text:
                                      mContext1.startActivity(new Intent() {{
                                        setAction(Intent.ACTION_SEND);
                                        setPackage(ThirdPartyApplication.PackageName.WHATSAPP);
                                        setType("text/plain");
                                        putExtra(Intent.EXTRA_TEXT, getPlainText());
                                      }});

                                      break;

                                    case R.id.share_as_image:
                                      ImageGenerator ig = new ImageGenerator(mContext1);
                                      Bitmap bitmap     = ig.getBitmap(TITLE, "Chapter " + CHAPTER +
                                          ", Verse " + VERSE, TEXT);

                                      FileManager fm = new FileManager(mContext2);
                                      File file      = fm.saveBitmap(bitmap);

                                      if (file != null) {
                                        final Uri URI = Uri.fromFile(file);
                                        mContext1.startActivity(new Intent() {{
                                          setAction(Intent.ACTION_SEND);
                                          setPackage(ThirdPartyApplication.PackageName.WHATSAPP);
                                          setType("image/*");
                                          putExtra(Intent.EXTRA_STREAM, URI);

                                          Toast.makeText(mContext2, 
                                              mContext2.getString(R.string.saved_to_gallery),
                                              Toast.LENGTH_SHORT);
                                        }});
                                      } else {
                                        //TO-DO: Snackbar instead Toast
                                        //TO-DO: Find and describe reason for not being able to save file,
                                        //       eg. storage full
                                        Toast.makeText(mContext2, 
                                            mContext2.getString(R.string.unable_to_save_file),
                                            Toast.LENGTH_SHORT);
                                      }
                                  }
                                }
                              })
                              .show();
                        } else {
                          //TO-DO: Snackbar instead Toast
                          Toast.makeText(mContext2, 
                              mContext2.getString(R.string.you_dont_have_whatsapp_installed),
                              Toast.LENGTH_SHORT);
                        }

                        return;

                      case R.id.action_share_message:
                        mContext1.startActivity(new Intent() {{
                          setAction(Intent.ACTION_VIEW);
                          setData(Uri.parse("sms:"));
                          putExtra("sms_body", getPlainText());
                        }});
                    }
                  }
                })
                .show();

          }
        };

      default:
        return null;
    }
  }
}