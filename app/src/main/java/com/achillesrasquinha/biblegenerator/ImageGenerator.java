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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class ImageGenerator {
  private Context        mContext;
  
  private Point          mImageSize;
  private int            mX;
  private int            mYTitle;
  private int            mYSubtitle;
  private float          mSizeTitle;
  private float          mSizeSubtitle;
  private float          mLineSpacing;

  private TextPaint      mTextPaint;

  private int            mColor1;
  private int            mColor2;
  private int            mColor3;

  public ImageGenerator(Context context) {
    mContext         = context;

    DisplayMetrics m = mContext.getResources().getDisplayMetrics();

    mImageSize       = new Point();
    mImageSize.x     = m.widthPixels;
    mImageSize.y     = mImageSize.x;

    mX               = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, m));
    mSizeTitle       = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,  24, m);
    mYTitle          = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, m)
        + mSizeTitle);
    mSizeSubtitle    = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,  14, m);
    mYSubtitle       = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, m)
        + mYTitle + mSizeSubtitle);
    mLineSpacing     = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,  8, m);

    mTextPaint       = new TextPaint();
    mTextPaint.setAntiAlias(true);
    mTextPaint.setTextAlign(TextPaint.Align.LEFT);
    mTextPaint.setTypeface(Typeface.createFromAsset(mContext.getAssets(),
        "fonts/Roboto/Roboto-Medium.ttf"));

    mColor1 = ContextCompat.getColor(mContext, R.color.primary_text_default_material_light);
    mColor2 = ContextCompat.getColor(mContext, R.color.primary_text_disabled_material_light);
    mColor3 = ContextCompat.getColor(mContext, R.color.secondary_text_default_material_light);
  }

  public Bitmap getBitmap(String title, String subtitle, String text) {
    Bitmap bitmap = Bitmap.createBitmap(mImageSize.x, mImageSize.y, Bitmap.Config.ARGB_8888);
    //convert bitmap to card view background color
    bitmap.eraseColor(ContextCompat.getColor(mContext, R.color.cardview_light_background));

    Canvas canvas = new Canvas(bitmap);   

    //title
    mTextPaint.setTextSize(mSizeTitle);
    mTextPaint.setColor(mColor1);
    canvas.drawText(title, mX, mYTitle, mTextPaint);

    //subtitle
    mTextPaint.setTextSize(mSizeSubtitle);
    mTextPaint.setColor(mColor2);
    canvas.drawText(subtitle, mX, mYSubtitle, mTextPaint);

    //since text needs to be wrapped, using a StaticLayout
    mTextPaint.setColor(mColor3);
    //text size same as subtitle size
    StaticLayout layout = new StaticLayout(
        text,
        mTextPaint,
        mImageSize.x - 2 * mX,
        Layout.Alignment.ALIGN_NORMAL,
        1.0f,
        mLineSpacing,
        false);
    //16dp padding as per material design guidelines
    canvas.translate(mX, mYSubtitle + mX);
    layout.draw(canvas);

    return bitmap;
  }
}
