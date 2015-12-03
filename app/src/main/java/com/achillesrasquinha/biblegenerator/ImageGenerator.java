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
import android.graphics.Typeface;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageGenerator {
  private static final int IMAGE_WIDTH  = 512;
  private static final int IMAGE_HEIGHT = IMAGE_WIDTH;

  private Context               mContext;
  private File                  mStorageDirectory;
  private Bitmap.CompressFormat mFormat;

  public ImageGenerator(Context context) {
    mContext          = context;
    mStorageDirectory = new File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name));
    mFormat           = Bitmap.CompressFormat.JPEG;
  }

  public Bitmap getBitmap(String title, String subtitle, String text) {
    final DisplayMetrics DISPLAY_METRICS = mContext.getResources().getDisplayMetrics();
    final int            X               = Math.round(TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 16, DISPLAY_METRICS));
    final float          SIZE_TITLE      = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,  24, DISPLAY_METRICS);
    final int            Y_TITLE         = Math.round(TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 24, DISPLAY_METRICS) + SIZE_TITLE);
    final float          SIZE_SUBTITLE   = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,  14, DISPLAY_METRICS);
    final int            Y_SUBTITLE      = Math.round(TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 12, DISPLAY_METRICS) + Y_TITLE + SIZE_SUBTITLE);
    final float          LINE_SPACING    = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,  8, DISPLAY_METRICS);

    Bitmap bitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
    //convert bitmap to card view background color
    bitmap.eraseColor(ContextCompat.getColor(mContext, R.color.cardview_light_background));

    Canvas    canvas = new Canvas(bitmap);
    TextPaint paint  = new TextPaint() {{
      setAntiAlias(true);
      setTextAlign(TextPaint.Align.LEFT);
      setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto/Roboto-Medium.ttf"));
    }};

    //title
    paint.setTextSize(SIZE_TITLE);
    paint.setColor(ContextCompat.getColor(mContext, R.color.primary_text_default_material_light));
    canvas.drawText(title, X, Y_TITLE, paint);

    //subtitle
    paint.setTextSize(SIZE_SUBTITLE);
    paint.setColor(ContextCompat.getColor(mContext, R.color.primary_text_disabled_material_light));
    canvas.drawText(subtitle, X, Y_SUBTITLE, paint);

    //since text needs to be wrapped, using a StaticLayout
    paint.setColor(ContextCompat.getColor(mContext, R.color.secondary_text_default_material_light));
    //text size same as subtitle size
    StaticLayout layout = new StaticLayout(
        text,
        paint,
        IMAGE_WIDTH - 2 * X,
        Layout.Alignment.ALIGN_NORMAL,
        1.0f,
        LINE_SPACING,
        false);
    //16dp padding as per material design guidelines
    canvas.translate(X, Y_SUBTITLE + X);
    layout.draw(canvas);

    return bitmap;
  }

  public File save(Bitmap bitmap) {
    if(!mStorageDirectory.exists()) {
      if(!mStorageDirectory.mkdirs()) {
        return null;
      }
    }

    String filename = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())
        + (mFormat == Bitmap.CompressFormat.JPEG ? ".jpg" : ".png");
    File   file     = new File(mStorageDirectory + File.separator + filename);

    try {
      FileOutputStream oStream = new FileOutputStream(file);
      bitmap.compress(mFormat, 100, oStream);
    } catch(FileNotFoundException e) {
      return null;
    }

    return file;
  }

  public void setCompressFormat(Bitmap.CompressFormat format) { mFormat = format; }
}
