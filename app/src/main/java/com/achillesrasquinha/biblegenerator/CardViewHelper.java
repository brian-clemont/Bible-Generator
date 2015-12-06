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
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.cocosw.bottomsheet.BottomSheet;

import java.io.File;
import java.util.HashMap;

public class CardViewHelper implements Toolbar.OnMenuItemClickListener, View.OnClickListener, 
    Dialog.OnClickListener {
  private Context                 mContext1;
  private Context                 mContext2;

  private String                  mAppName;

  public  String                  title;
  public  String                  mChapter;
  public  String                  mVerse;
  public  String                  subtitle;
  public  String                  text;

  private CoordinatorLayout       mCoordinatorLayout;

  private ClipboardManager        mClipboardManager;
  private FileManager             mFileManager;
  private ImageGenerator          mImageGenerator;

  private BottomSheet.Builder     mBottomSheetBuilder1;
  private BottomSheet.Builder     mBottomSheetBuilder2;

  public CardViewHelper(Context context, CoordinatorLayout layout) {
    mContext1            = context;
    mContext2            = context.getApplicationContext();
    mCoordinatorLayout   = layout;

    //accessing static method via instance reference
    mClipboardManager    = (ClipboardManager) mContext2.getSystemService(
        mContext2.CLIPBOARD_SERVICE);
    mFileManager         = new FileManager(mContext2);
    mImageGenerator      = new ImageGenerator(mContext2);

    mAppName             = mContext2.getString(R.string.app_name);

    mBottomSheetBuilder1 = new BottomSheet.Builder((Activity) mContext1);
    mBottomSheetBuilder1.title(R.string.bottom_sheet_share_title);
    mBottomSheetBuilder1.grid();
    mBottomSheetBuilder1.sheet(R.menu.menu_bottom_sheet_share);

    mBottomSheetBuilder2 = new BottomSheet.Builder((Activity) mContext1);
    mBottomSheetBuilder2.title(R.string.bottom_sheet_share_as_title);
    mBottomSheetBuilder2.sheet(R.menu.menu_bottom_sheet_share_as);
  }

  public void setDataset(HashMap<String, String> map) {
    //providing global access since retriving from the HashMap is slow.
    title    = map.get(MapKeys.TITLE);
    mChapter = map.get(MapKeys.CHAPTER);
    mVerse   = map.get(MapKeys.VERSE);
    subtitle = "Chapter " + mChapter + ", Verse " + mVerse;
    text     = map.get(MapKeys.TEXT);
  }

  private String getPlainText() {
    return "\""
        + text
        + "\""
        + "\n"
        + title
        + " "
        + mChapter
        + ":"
        + mVerse
        + "\n"
        + "\n"
        + "- via "
        + mAppName;
  }

  @Override
  public boolean onMenuItemClick (MenuItem item) {
    switch(item.getItemId()) {
      case R.id.action_content_copy:
        ClipData cd = ClipData.newPlainText(mAppName, getPlainText());
          mClipboardManager.setPrimaryClip(cd);

        Snackbar.make(mCoordinatorLayout, R.string.message_copied_to_clipboard,
            Snackbar.LENGTH_SHORT).show();

        return true;

      case R.id.action_save:
        File file = mFileManager.saveBitmap(mImageGenerator.getBitmap(title, subtitle,
            text));

        if (file != null) {
          Snackbar.make(mCoordinatorLayout, R.string.message_saved_to_gallery,
              Snackbar.LENGTH_SHORT).show();
        } else {
          //TO-DO: Find and describe reason for not being able to save file,
          //       eg. storage full (Check documentation for possibilities)
          Snackbar.make(mCoordinatorLayout, R.string.message_unable_to_save_file,
              Snackbar.LENGTH_SHORT).show();
        }

        return true;

      default:
        return false;
    }
  }

  @Override
  public void onClick(View view) {
    switch(view.getId()) {
      case R.id.btn_share:
        mBottomSheetBuilder1.listener(this).show();
    }
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    switch(which) {
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
        if (ThirdPartyApplication.isInstalled(mContext2,
            ThirdPartyApplication.PackageName.INSTAGRAM)) {
          final File FILE = mFileManager.saveBitmap(mImageGenerator.getBitmap(title, subtitle, 
              text));

          if (FILE != null) {
            Snackbar.make(mCoordinatorLayout, R.string.message_saved_to_gallery,
                Snackbar.LENGTH_SHORT).show();

            mContext1.startActivity(new Intent() {{
              setAction(Intent.ACTION_SEND);
              setPackage(ThirdPartyApplication.PackageName.INSTAGRAM);
              setType("image/*");
              putExtra(Intent.EXTRA_STREAM, Uri.fromFile(FILE));
            }});
          } else {
            //TO-DO: Find and describe reason for not being able to save file,
            //       eg. storage full
            Snackbar.make(mCoordinatorLayout, R.string.message_unable_to_save_file,
                Snackbar.LENGTH_SHORT).show();
          }

        } else {
          Snackbar.make(mCoordinatorLayout, R.string.message_you_dont_have_instagram_installed,
              Snackbar.LENGTH_SHORT).show();
        }

        break;

      case R.id.action_share_whatsapp:
        if (ThirdPartyApplication.isInstalled(mContext2,
            ThirdPartyApplication.PackageName.WHATSAPP)) {
          mBottomSheetBuilder2.listener(this).show();
        } else {
          Snackbar.make(mCoordinatorLayout, R.string.message_you_dont_have_whatsapp_installed,
              Snackbar.LENGTH_SHORT).show();
        }

        break;

      case R.id.action_share_message:
        mContext1.startActivity(new Intent() {{
          setAction(Intent.ACTION_VIEW);
          setData(Uri.parse("sms:"));
          putExtra("sms_body", getPlainText());
        }});

        break;

      case R.id.share_as_text:
        mContext1.startActivity(new Intent() {{
          setAction(Intent.ACTION_SEND);
          setPackage(ThirdPartyApplication.PackageName.WHATSAPP);
          setType("text/plain");
          putExtra(Intent.EXTRA_TEXT, getPlainText());
        }});

        break;

      case R.id.share_as_image:
        final File FILE = mFileManager.saveBitmap(mImageGenerator.getBitmap(title, subtitle, text));

        if (FILE != null) {
          Snackbar.make(mCoordinatorLayout, R.string.message_saved_to_gallery,
              Snackbar.LENGTH_SHORT).show();

          mContext1.startActivity(new Intent() {{
            setAction(Intent.ACTION_SEND);
            setPackage(ThirdPartyApplication.PackageName.WHATSAPP);
            setType("image/*");
            putExtra(Intent.EXTRA_STREAM, Uri.fromFile(FILE));
          }});
        } else {
          //TO-DO: Find and describe reason for not being able to save file,
          //       eg. storage full
          Snackbar.make(mCoordinatorLayout, R.string.message_unable_to_save_file,
              Snackbar.LENGTH_SHORT).show();
        }

        break;
    }
  }
}