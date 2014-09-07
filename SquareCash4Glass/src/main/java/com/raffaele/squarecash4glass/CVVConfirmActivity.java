/*
 * Copyright (c) 2014 Rex St. John on behalf of AirPair.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raffaele.squarecash4glass;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Take a picture directly from inside the app using this fragment.
 * 
 * Reference: http://developer.android.com/training/camera/cameradirect.html
 * Reference:
 * http://stackoverflow.com/questions/7942378/android-camera-will-not-
 * work-startpreview-fails Reference:
 * http://stackoverflow.com/questions/10913181/camera-preview-is-not-restarting
 * 
 * Created by Rex St. John (on behalf of AirPair.com) on 3/4/14.
 */
public class CVVConfirmActivity extends Activity {

  private static final String CVVRegExprStr = "^[0-9]{3,4}$";
  private static final Pattern CVVPattern = Pattern.compile(CVVRegExprStr);

  private static final String TAG = "CVVConfirmActivity";

  private String CVVCode = null;

  private GestureDetector mGestureDetector;

  // Native camera.
  private Camera mCamera;

  // View to display the camera output.
  private CameraPreview mPreview;

  protected int maxZoomLevel;

  protected int currentZoomLevel;


  /**
   * Default empty constructor.
   */
  public CVVConfirmActivity() {
    super();
  }

  private GestureDetector createGestureDetector(Context context) {
    GestureDetector gestureDetector = new GestureDetector(context);
    gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
      @Override
      public boolean onGesture(Gesture gesture) {
        if (gesture == Gesture.TAP) {
          Log.i(TAG, "TAP is detected");
          if (mCamera != null) {
            mCamera.takePicture(mShutterCallback, null, mPictureCallback);
          }
          return true;
        } else if (gesture == Gesture.SWIPE_RIGHT) {
          currentZoomLevel = Math.max(currentZoomLevel + 1, 0);
          updateCameraZoom();
          return true;
        } else if (gesture == Gesture.SWIPE_LEFT) {
          currentZoomLevel = Math.max(currentZoomLevel - 1, 0);
          updateCameraZoom();
          return true;
        }
        if (gesture == Gesture.TWO_SWIPE_RIGHT) {
          currentZoomLevel = Math.min(currentZoomLevel + 10, maxZoomLevel);
          updateCameraZoom();
          return true;
        }
        if (gesture == Gesture.TWO_SWIPE_LEFT) {
          currentZoomLevel = Math.max(currentZoomLevel - 10, 0);
          updateCameraZoom();
          return true;
        }
        return false;
      }
    });
    return gestureDetector;
  }
  
  class ProcessPictureTask extends AsyncTask<byte[],Void,String> {
    @Override
    protected String doInBackground(byte[]... bytes) {
      if (bytes.length!=1) return null;
      byte[] data=bytes[0];
      Bitmap bitmap=BitmapFactory.decodeByteArray(data, 0, data.length);   
      String lang = "eng";
      // Make sure this path exist
      File dataPathDir = new File(getFilesDir(), InstallationListener.tesseactDirName);
      // String DATA_PATH = this.getDir(dataPathDir.getAbsolutePath(),
      // Context.MODE_PRIVATE).getAbsolutePath()+"/";
      TessBaseAPI baseApi = new TessBaseAPI();
      baseApi.setDebug(true);
      baseApi.init(dataPathDir.getAbsolutePath(), lang);
      baseApi.setImage(bitmap);
      String recognizedText = baseApi.getUTF8Text();
      Log.i(TAG, "recognizedText: " + recognizedText);
      Matcher CVVMatcher = CVVPattern.matcher(recognizedText);
      // we print the first occurrence if it exists
      String result=null;
      if (CVVMatcher.find()) {
        result=CVVMatcher.group();
      }
      Log.i(TAG, "ProcessPictureTask.doInBackground() completed, result: "+result);
      return result;
    }
    protected void onPostExecute(String result){
      if (result!=null) {
        CVVCode=result;
        ((TextView)findViewById(R.id.TextView1)).setText("Scanned Code: "+CVVCode);
      }
    }

  }

//  private String processPicture(Bitmap bitmap) {
//    String lang = "eng";
//    // Make sure this path exist
//    File dataPathDir = new File(getFilesDir(), InstallationListener.tesseactDirName);
//    // String DATA_PATH = this.getDir(dataPathDir.getAbsolutePath(),
//    // Context.MODE_PRIVATE).getAbsolutePath()+"/";
//    TessBaseAPI baseApi = new TessBaseAPI();
//    baseApi.setDebug(true);
//    baseApi.init(dataPathDir.getAbsolutePath(), lang);
//    baseApi.setImage(bitmap);
//    String recognizedText = baseApi.getUTF8Text();
//    Log.i(TAG, "recognizedText: " + recognizedText);
//    Matcher CVVMatcher = CVVPattern.matcher(recognizedText);
//    // we print the first occurrence if it exists
//    String result=null;
//    if (CVVMatcher.find()) {
//      result=CVVMatcher.group();
//    }
//    Log.i(TAG, "processPicture() completed, result: "+result);
//    return result;
//  }

  /*
   * Send generic motion events to the gesture detector
   */
  @Override
  public boolean onGenericMotionEvent(MotionEvent event) {
    if (mGestureDetector != null) {
      return mGestureDetector.onMotionEvent(event);
    }
    return false;
  }

  public void onResume() {
    super.onResume();
    View view = findViewById(R.id.container);
    // Create our Preview view and set it as the content of our activity.
    boolean opened = safeCameraOpenInView(view);

    if (opened == false) {
      Log.d("CameraGuide", "Error, Camera failed to open");
      return;
    }
    maxZoomLevel = mCamera.getParameters().getMaxZoom();
    currentZoomLevel = mCamera.getParameters().getZoom();

  }

  /**
   * OnCreateView fragment override
   * 
   * @param inflater
   * @param container
   * @param savedInstanceState
   * @return
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.cvv_confirm_view);
    mGestureDetector = createGestureDetector(this);
    Log.i(TAG, "onCreate completed.");

  }

  /**
   * Recommended "safe" way to open the camera.
   * 
   * @param view
   * @return
   */
  private boolean safeCameraOpenInView(View view) {
    boolean qOpened = false;
    releaseCameraAndPreview();
    mCamera = getCameraInstance();
    qOpened = (mCamera != null);

    if (qOpened == true) {
      mPreview = new CameraPreview(getBaseContext(), mCamera, view);
      FrameLayout preview = (FrameLayout) view.findViewById(R.id.camera_preview);
      preview.addView(mPreview);
      mPreview.startCameraPreview();
      Log.i(TAG, "camera parameters:" + mCamera.getParameters());
      Log.i(TAG, "camera current size:" + ToStringBuilder.reflectionToString(mCamera.getParameters().getPictureSize()));
      Log.i(TAG, "camera supported sizes:" + ToStringBuilder.reflectionToString(mCamera.getParameters().getSupportedPictureSizes().toArray(), new RecursiveToStringStyle()));

    }
    Log.i(TAG, "safeCameraOpenInView completed.");
    return qOpened;
  }

  /**
   * Safe method for getting a camera instance.
   * 
   * @return
   */
  public static Camera getCameraInstance() {
    Camera c = null;
    try {
      c = Camera.open(); // attempt to get a Camera instance
    } catch (Exception e) {
      e.printStackTrace();
    }
    return c; // returns null if camera is unavailable
  }

  @Override
  public void onPause() {
    super.onPause();
    releaseCameraAndPreview();
    Log.i(TAG, "onPause completed.");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Log.i(TAG, "onDestroy completed.");
  }

  /**
   * Clear any existing preview / camera.
   */
  private void releaseCameraAndPreview() {

    if (mCamera != null) {
      mCamera.stopPreview();
      mCamera.release();
      mCamera = null;
    }
    if (mPreview != null) {
      mPreview.destroyDrawingCache();
      mPreview.mCamera = null;
    }
  }

  /**
   * @param parameters
   */
  private void updateCameraZoom() {
    Log.i(TAG, "currentZoomLevel" + currentZoomLevel);
    Log.i(TAG, "maxZoomLevel" + maxZoomLevel);
    Camera.Parameters parameters = mCamera.getParameters();
    Log.i(TAG, "zoomSupported" + parameters.isZoomSupported());
    Log.i(TAG, "smoothZoomSupported" + parameters.isSmoothZoomSupported());
    if (parameters.isZoomSupported()) {
      if (parameters.isSmoothZoomSupported()) {
        mCamera.startSmoothZoom(currentZoomLevel);
      } else {
        parameters.setZoom(currentZoomLevel);
        mCamera.setParameters(parameters);
      }
    }
    Log.i(TAG, "updateCameraZoom completed.");
  }

  /**
   * Surface on which the camera projects it's capture results. This is derived
   * both from Google's docs and the excellent StackOverflow answer provided
   * below.
   * 
   * Reference / Credit:
   * http://stackoverflow.com/questions/7942378/android-camera
   * -will-not-work-startpreview-fails
   */
  class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    // SurfaceHolder
    private SurfaceHolder mHolder;

    // Our Camera.
    private Camera mCamera;

    // Parent Context.
    private Context mContext;

    // Camera Sizing (For rotation, orientation changes)
    private Camera.Size mPreviewSize;

    // List of supported preview sizes
    private List<Camera.Size> mSupportedPreviewSizes;

    // Flash modes supported by this camera
    private List<String> mSupportedFlashModes;

    // View holding this camera.
    private View mCameraView;

    public CameraPreview(Context context, Camera camera, View cameraView) {
      super(context);

      // Capture the context
      mCameraView = cameraView;
      mContext = context;
      setCamera(camera);

      // Install a SurfaceHolder.Callback so we get notified when the
      // underlying surface is created and destroyed.
      mHolder = getHolder();
      mHolder.addCallback(this);
      mHolder.setKeepScreenOn(true);
      // deprecated setting, but required on Android versions prior to 3.0
      mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
      Log.i(TAG, "CameraPreview() completed.");
    }

    /**
     * Begin the preview of the camera input.
     */
    public void startCameraPreview() {
      try {
        mCamera.setPreviewDisplay(mHolder);
        mCamera.startPreview();
      } catch (Exception e) {
        e.printStackTrace();
      }
      Log.i(TAG, "startCameraPreview() completed.");
    }

    /**
     * Extract supported preview and flash modes from the camera.
     * 
     * @param camera
     */
    private void setCamera(Camera camera) {
      // Source:
      // http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
      mCamera = camera;
      mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
      mSupportedFlashModes = mCamera.getParameters().getSupportedFlashModes();

      // Set the camera to Auto Flash mode.
      if (mSupportedFlashModes != null && mSupportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        mCamera.setParameters(parameters);
      }

      requestLayout();
      Log.i(TAG, "setCamera() completed.");
    }

    /**
     * The Surface has been created, now tell the camera where to draw the
     * preview.
     * 
     * @param holder
     */
    public void surfaceCreated(SurfaceHolder holder) {
      try {
        mCamera.setPreviewDisplay(holder);
      } catch (IOException e) {
        e.printStackTrace();
      }
      Log.i(TAG, "surfaceCreated() completed.");
    }

    /**
     * Dispose of the camera preview.
     * 
     * @param holder
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
      if (mCamera != null) {
        mCamera.stopPreview();
      }
      Log.i(TAG, "surfaceDestroyed() completed.");
    }

    /**
     * React to surface changed events
     * 
     * @param holder
     * @param format
     * @param w
     * @param h
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
      // If your preview can change or rotate, take care of those events here.
      // Make sure to stop the preview before resizing or reformatting it.

      if (mHolder.getSurface() == null) {
        // preview surface does not exist
        return;
      }

      // stop preview before making changes
      try {
        Camera.Parameters parameters = mCamera.getParameters();

        // Set the auto-focus mode to "continuous"
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);

        // Preview size must exist.
        if (mPreviewSize != null) {
          Camera.Size previewSize = mPreviewSize;
          parameters.setPreviewSize(previewSize.width, previewSize.height);
        }

        mCamera.setParameters(parameters);
        mCamera.startPreview();
      } catch (Exception e) {
        e.printStackTrace();
      }
      Log.i(TAG, "surfaceChanged() camera focus more: "+mCamera.getParameters().getFocusMode());
      Log.i(TAG, "surfaceChanged() completed.");
    }

    /**
     * Calculate the measurements of the layout
     * 
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      // Source:
      // http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
      final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
      final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
      setMeasuredDimension(width, height);

      if (mSupportedPreviewSizes != null) {
        mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
      }
      Log.i(TAG, "onMeasure() completed. witdh: " + width + " height: " + height);
    }

    /**
     * Update the layout based on rotation and orientation changes.
     * 
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
      // Source:
      // http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
      if (changed) {
        final int width = right - left;
        final int height = bottom - top;

        int previewWidth = width;
        int previewHeight = height;

        // if (mPreviewSize != null) {
        // Display display = ((WindowManager)
        // mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        // Log.i(TAG, "display:" + display);
        // switch (display.getRotation()) {
        // case Surface.ROTATION_0:
        // previewWidth = mPreviewSize.width;
        // previewHeight = mPreviewSize.height;
        // // mCamera.setDisplayOrientation(90);
        // break;
        // case Surface.ROTATION_90:
        // previewWidth = mPreviewSize.width;
        // previewHeight = mPreviewSize.height;
        // break;
        // case Surface.ROTATION_180:
        // previewWidth = mPreviewSize.height;
        // previewHeight = mPreviewSize.width;
        // break;
        // case Surface.ROTATION_270:
        // previewWidth = mPreviewSize.width;
        // previewHeight = mPreviewSize.height;
        // mCamera.setDisplayOrientation(180);
        // break;
        // }
        // }

        final int scaledChildHeight = previewHeight * width / previewWidth;
        mCameraView.layout(0, height - scaledChildHeight, width, height);
        Log.i(TAG, "onLayout() completed." + 0 + " " + (height - scaledChildHeight) + " " + width + " " + height);
      }
    }

    /**
     * 
     * @param sizes
     * @param width
     * @param height
     * @return
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {
      // Source:
      // http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
      Camera.Size optimalSize = null;

      final double ASPECT_TOLERANCE = 0.1;
      double targetRatio = (double) height / width;

      // Try to find a size match which suits the whole screen minus the menu on
      // the left.
      for (Camera.Size size : sizes) {

        if (size.height != width)
          continue;
        double ratio = (double) size.width / size.height;
        if (ratio <= targetRatio + ASPECT_TOLERANCE && ratio >= targetRatio - ASPECT_TOLERANCE) {
          optimalSize = size;
        }
      }

      // If we cannot find the one that matches the aspect ratio, ignore the
      // requirement.
      if (optimalSize == null) {
        // TODO : Backup in case we don't get a size.
      }
      Log.i(TAG, "getOptimalPreviewSize() completed: " + ToStringBuilder.reflectionToString(optimalSize));
      return optimalSize;
    }
  }
  
  private final ShutterCallback mShutterCallback=new ShutterCallback(){

    @Override
    public void onShutter() {
      // TODO Auto-generated method stub
      AudioManager audio = (AudioManager) CVVConfirmActivity.this.getSystemService(Context.AUDIO_SERVICE);
      audio.playSoundEffect(Sounds.TAP);
      Log.i(TAG, "onShutter() completed.");
    }
    
  };

  /**
   * Picture Callback for handling a picture capture and saving it out to a
   * file.
   */
  private final PictureCallback mPictureCallback = new PictureCallback() {

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {    
      new ProcessPictureTask().execute(data);
      mCamera.startPreview();
      Log.i(TAG, "onPictureTaken() completed.");
    }  
  };

}
