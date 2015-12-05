package com.achillesrasquinha.biblegenerator;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileManager {
  private Context               mContext;
  private File                  mStorageDirectory;

  public FileManager(Context context) {
    mContext          = context;
    mStorageDirectory = new File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name));
  }

  public File saveBitmap(Bitmap bitmap) {
    if(!mStorageDirectory.exists()) {
      if(!mStorageDirectory.mkdirs()) {
        return null;
      }
    }

    String filename = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
    File   file     = new File(mStorageDirectory + File.separator + filename);

    FileOutputStream oStream = null;
    try {
      oStream = new FileOutputStream(file);
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, oStream);
    } catch(FileNotFoundException e) {
      return null;
    } finally {
      if (oStream != null)
        try {
          oStream.close();
        } catch(IOException e) {
          //TO-DO: Handle, maybe.
        }
    }

    return file;
  }
}
