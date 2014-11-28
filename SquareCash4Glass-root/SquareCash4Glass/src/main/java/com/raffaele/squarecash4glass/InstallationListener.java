package com.raffaele.squarecash4glass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.compress.archivers.ArchiveException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.raffaele.squarecash4glass.utils.CompressedFilesUtils;

public class InstallationListener {

  public static final String tesseactDirName = "tesseract-ocr";
  public static final String tessdataDirName = "tessdata";
  public static final String tesseractAssetFileName = "tesseract-ocr-3.02.eng.tar";

  private static final String TAG = "InstallationListener";

  public static void onStart(final Context context) {
    Log.i(TAG, "first launch event received");
    // check if the data is already there
    File privateFilesDir = context.getFilesDir();
    File tessDir = new File(privateFilesDir, tesseactDirName);
    if (tessDir.exists() && Arrays.asList(tessDir.list()).contains(tessdataDirName)) {
      Log.i(TAG, "tesseact data already exists");
      return;
    }
    // decompress data if not there
    new Thread(new Runnable() {
      public void run() {
        try {
          Log.i(TAG, "tesseact data does not exist");
          // Log.i(TAG,
          // "list of assets in /: "+Arrays.toString(context.getResources().getAssets().list("/")));
          // Log.i(TAG,
          // "list of assets in /assets: "+Arrays.toString(context.getResources().getAssets().list("/assets")));
          InputStream is = context.getResources().getAssets().open(tesseractAssetFileName);
          CompressedFilesUtils.unTar(is, context.getFilesDir());
          Log.i(TAG, "tesseact data successfully uncompressed");
        } catch (FileNotFoundException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (ArchiveException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }).start();

  }
}
