package com.achillesrasquinha.biblegenerator;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileManager {
  private Context               mContext;
  private File                  mStorageDirectory;
  private Bitmap.CompressFormat mFormat;

  public FileManager(Context context) {
    mContext          = context;
    mStorageDirectory = new File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name));
    mFormat           = mFormat;
  }

  public File saveBitmap(Bitmap bitmap) {
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
