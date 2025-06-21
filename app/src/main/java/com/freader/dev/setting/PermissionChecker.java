package com.freader.dev.setting;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionChecker {

  public static boolean checkStoragePermissionGranted(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      return Environment.isExternalStorageManager();
    } else {
      int readExternalStorage =
          ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
      int writeExternalStorage =
          ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
      return readExternalStorage == PackageManager.PERMISSION_GRANTED
          && writeExternalStorage == PackageManager.PERMISSION_GRANTED;
    }
  }
}
