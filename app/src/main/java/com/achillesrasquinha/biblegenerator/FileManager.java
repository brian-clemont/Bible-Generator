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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileManager {
  private Context mContext;
  private File    mStorageDirectory;

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
          oStream.flush();
          oStream.close();
        } catch(IOException e) {
          //TO-DO: Handle, maybe.
        }
    }

    ContentValues cv = new ContentValues();
    cv.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
    cv.put(MediaStore.Images.ImageColumns.BUCKET_ID, file.toString().toLowerCase(Locale.US)
        .hashCode());
    cv.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getName()
        .toLowerCase(Locale.US));
    cv.put("_data", file.getAbsolutePath());

    ContentResolver cr = mContext.getContentResolver();
    cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

    return file;
  }
}
