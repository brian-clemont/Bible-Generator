package com.achillesrasquinha.biblegenerator;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cocosw.bottomsheet.BottomSheet;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.melnykov.fab.FloatingActionButton;

import java.io.IOException;
import java.sql.SQLException;

public class MainActivity extends AppCompatActivity {

    private Toolbar              mToolbar;
    private TextView             mTextViewTitle;
    private TextView             mTextViewSubtitle;
    private TextView             mTextViewText;
    private FloatingActionButton mFAB;
    private Button               mButton1;
    private Button               mButton2;
    private BottomSheet.Builder  mBottomSheet;

    private DatabaseOpenHelper   mDatabaseOpenHelper;
    private String               mID;
    private String               mBookID;
    private String               mBookName;
    private String               mChapter;
    private String               mVerse;
    private String               mText;

    private String               mTitle;
    private String               mSubtitle;

    private String               mIntentString;

    private AdView               mAdView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String APP_NAME = getApplicationContext().getString(R.string.app_name);

        mDatabaseOpenHelper = new DatabaseOpenHelper(this, "bible");

        mToolbar            = (Toolbar)              findViewById(R.id.app_bar);
        mTextViewTitle      = (TextView)             findViewById(R.id.text_view_title);
        mTextViewSubtitle   = (TextView)             findViewById(R.id.text_view_subtitle);
        mTextViewText       = (TextView)             findViewById(R.id.text_view_supporting_text);
        mFAB                = (FloatingActionButton) findViewById(R.id.fab_random_generator);
        mButton1            = (Button)               findViewById(R.id.btn_like);
        mButton2            = (Button)               findViewById(R.id.btn_share);
        mAdView             = (AdView)               findViewById(R.id.ad_view);

        setSupportActionBar(mToolbar);
        try {
            mDatabaseOpenHelper.create();
        }
        catch (IOException e) {

        }

        try {
            mDatabaseOpenHelper.open();
        }
        catch (SQLException e) {

        }

        updateCard();

        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCard();
            }
        });

        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mButton1.getText().equals("LIKE")) {
                    ContentValues cv = new ContentValues();
                    cv.put("bible_kjv_id", Integer.parseInt(mID));
                    mDatabaseOpenHelper.db.insert("like", null, cv);
                } else {
                    mDatabaseOpenHelper.db.execSQL("DELETE FROM like WHERE bible_kjv_id = " + mID);
                }

                updateLikeButton();
            }
        });

        mBottomSheet = new BottomSheet.Builder(this)
                .title("Share via")
                .grid()
                .sheet(R.menu.menu_bottom_sheet_share)
                .listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int resource) {
                        mIntentString =
                                "\"" + mText + "\"" + "\n" +
                                "- " + mBookName + " " + mChapter + ":" + mVerse + "\n" +
                                "\n" +
                                "via " + APP_NAME;
                        switch(resource) {
                            case R.id.share_whatsapp:
                                if(ThirdPartyApplication.isInstalled(getApplicationContext(), "com.whatsapp")) {
                                    Intent intent = new Intent();

                                    intent.setAction(Intent.ACTION_SEND);
                                    intent.putExtra(Intent.EXTRA_TEXT, mIntentString);
                                    intent.setType("text/plain");
                                    intent.setPackage("com.whatsapp");

                                    startActivity(intent);
                                }
                                else {

                                }

                                break;

                            case R.id.share_instagram:
                                if(ThirdPartyApplication.isInstalled(getApplication(), "com.instagram.android")) {
                                    ImageGenerator ig = new ImageGenerator(getApplicationContext());
                                    Uri uri = ig.save(ig.getBitmap(mTitle, mSubtitle, mText));

                                    if (uri != null) {

                                        Intent intent = new Intent();

                                        intent.setAction(Intent.ACTION_SEND);
                                        intent.setType("image/*");
                                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                                        intent.setPackage("com.instagram.android");

                                        startActivity(intent);
                                    }
                                }

                            case R.id.share_twitter:
                                if(ThirdPartyApplication.isInstalled(getApplicationContext(), "com.twitter.android")) {

                                }

                            case R.id.share_message:
                                Intent intent = new Intent();

                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("sms:"));
                                intent.putExtra("sms_body", mIntentString);

                                startActivity(intent);
                        }
                    }
                });

        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheet.show();
            }
        });

        mAdView.loadAd(new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("B2237171B30BD9744A213A70313165F0")
                .build());
    }

    private void updateCard() {
        Cursor cursor;

        cursor = mDatabaseOpenHelper.db.rawQuery("SELECT * FROM bible_kjv ORDER BY RANDOM() LIMIT 1", null);
        cursor.moveToFirst();

        mID       = cursor.getString(cursor.getColumnIndex("_id"));
        mBookID   = cursor.getString(cursor.getColumnIndex("book_id"));
        mChapter  = cursor.getString(cursor.getColumnIndex("chapter"));
        mVerse    = cursor.getString(cursor.getColumnIndex("verse"));
        mText     = cursor.getString(cursor.getColumnIndex("text"));

        cursor = mDatabaseOpenHelper.db.rawQuery("SELECT book_name FROM bible_books WHERE _id = " + mBookID, null);
        cursor.moveToFirst();

        mBookName = cursor.getString(cursor.getColumnIndex("book_name"));

        mTitle    = mBookName;
        mSubtitle = "Chapter " + mChapter + ", Verse " + mVerse;

        mTextViewTitle   .setText(mTitle);
        mTextViewSubtitle.setText(mSubtitle);
        mTextViewText    .setText(mText);

        updateLikeButton();
    }

    private void updateLikeButton() {
        Cursor cursor    = mDatabaseOpenHelper.db.rawQuery("SELECT bible_kjv_id FROM like WHERE bible_kjv_id = " + mID, null);
        boolean hasLiked = cursor.moveToFirst();

        if(hasLiked) {
            mButton1.setText("DISLIKE");
        }
        else {
            mButton1.setText("LIKE");
        }
    }
}
