package com.achillesrasquinha.biblegenerator;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageGenerator {

    private static final int PIXEL_IMAGE_WIDTH  = 512;
    private static final int PIXEL_IMAGE_HEIGHT = PIXEL_IMAGE_WIDTH;

    private Context mContext;
    private File    mStorageDirectory;

    public ImageGenerator(Context context) {
        this.mContext          = context;
        this.mStorageDirectory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES),
                mContext.getResources().getString(R.string.app_name));
    }

    protected Bitmap getBitmap(String title, String subtitle, String text) {
        Resources resources            = mContext.getResources();
        DisplayMetrics lDisplayMetrics = resources.getDisplayMetrics();

        final float PIXEL_SIZE_TITLE         = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, lDisplayMetrics);
        final float PIXEL_SIZE_SUBTITLE      = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, lDisplayMetrics);
        final float PIXEL_SIZE_TEXT          = PIXEL_SIZE_SUBTITLE;

        final int PIXEL_POSITION_X_LEFT      = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, lDisplayMetrics));
        final int PIXEL_POSITION_X_RIGHT     = PIXEL_IMAGE_WIDTH - PIXEL_POSITION_X_LEFT;
        final int PIXEL_POSITION_Y_TITLE     = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, lDisplayMetrics));
        final int PIXEL_POSITION_Y_SUBTITLE  = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, lDisplayMetrics) + PIXEL_POSITION_Y_TITLE + PIXEL_SIZE_SUBTITLE);
        final int PIXEL_POSITION_Y_TEXT      = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, lDisplayMetrics) + PIXEL_POSITION_Y_SUBTITLE + PIXEL_SIZE_TEXT);

        final int COLOR_BACKGROUND           = resources.getColor(R.color.cardview_light_background);
        final int COLOR_TITLE                = resources.getColor(R.color.primary_text_default_material_light);
        final int COLOR_SUBTITLE             = resources.getColor(R.color.secondary_text_default_material_light);
        final int COLOR_TEXT                 = COLOR_SUBTITLE;

        final float PIXEL_LINE_SPACING_EXTRA = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, lDisplayMetrics);

        final String TITLE                   = title;
        final String SUBTITLE                = subtitle;
        final String TEXT                    = text;

        Bitmap bitmap = Bitmap.createBitmap(PIXEL_IMAGE_WIDTH, PIXEL_IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(COLOR_BACKGROUND);

        Canvas canvas     = new Canvas(bitmap);
        TextPaint paint   = new TextPaint();
        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto/Roboto-Medium.ttf");

        paint.setAntiAlias(true);
        paint.setTextSize(PIXEL_SIZE_TITLE);
        paint.setColor(COLOR_TITLE);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(typeface);

        canvas.drawText(TITLE, PIXEL_POSITION_X_LEFT, PIXEL_POSITION_Y_TITLE, paint);

        paint.setColor(COLOR_SUBTITLE);
        paint.setTextSize(PIXEL_SIZE_SUBTITLE);
        canvas.drawText(SUBTITLE, PIXEL_POSITION_X_LEFT, PIXEL_POSITION_Y_SUBTITLE, paint);

        paint.setColor(COLOR_TEXT);
        paint.setTextSize(PIXEL_SIZE_TEXT);

        StaticLayout layout = new StaticLayout(TEXT, paint, PIXEL_POSITION_X_RIGHT - PIXEL_POSITION_X_LEFT, Layout.Alignment.ALIGN_NORMAL, 1.0f, PIXEL_LINE_SPACING_EXTRA, false);
        canvas.translate(PIXEL_POSITION_X_LEFT, PIXEL_POSITION_Y_TEXT - PIXEL_SIZE_TEXT);
        layout.draw(canvas);

        return bitmap;
    }

    protected Uri save(Bitmap bitmap) {
        if(!mStorageDirectory.exists()) {
            if(!mStorageDirectory.mkdirs()) {
                return null;
            }
        }

        String filename = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
        File   file     = new File(mStorageDirectory.getPath() + File.separator + filename);


        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        }
        catch(Exception e) {
            return null;
        }

        return Uri.fromFile(file);
    }
}