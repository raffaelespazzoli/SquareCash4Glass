package com.raffaele.squarecash4glass;

import java.text.NumberFormat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

//import com.google.android.glass.eye.EyeGesture;
//import com.google.android.glass.eye.EyeGestureManager;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class AmountActivity extends Activity {

  private static final String TAG = "AmountActivity";

  private static final int MAX_AMOUNT = 50;
  private int mAmount = 0;
  private GestureDetector mGestureDetector;
  private NumberFormat mCurrencyFormatter = NumberFormat.getCurrencyInstance();
//  private EyeGestureManager mEyeGestureManager;
//  private EyeGestureManager.Listener mEyeGestureListener;
  private SensorManager mSensorManager;
  private SensorEventListener mSensorEventListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    InstallationListener.onStart(this);
    setContentView(R.layout.amount_view);
    updateAmount();
    mGestureDetector = createGestureDetector(this);
//    mEyeGestureManager = EyeGestureManager.from(this);
//    mEyeGestureListener = createEyeGestureListener();
    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    mSensorEventListener = createSensorListener();
    Log.i(TAG, "onCreate completed.");
  }

  @Override
  protected void onStart() {
    super.onStart();
//    mEyeGestureManager.stopDetector(EyeGesture.WINK);
//    mEyeGestureManager.enableDetectorPersistently(EyeGesture.WINK, true);
//    mEyeGestureManager.register(EyeGesture.WINK, mEyeGestureListener);
    Log.i(TAG, "onStart completed.");
  }

  @Override
  protected void onResume() {
    super.onResume();
    mSensorManager.registerListener(mSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
    mSensorManager.registerListener(mSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    Log.i(TAG, "onResume completed.");
  }

  @Override
  protected void onPause() {
    super.onPause();
    mSensorManager.unregisterListener(mSensorEventListener);
    Log.i(TAG, "onPause completed.");
  }

  @Override
  protected void onStop() {
    super.onStop();
//    mEyeGestureManager.unregister(EyeGesture.WINK, mEyeGestureListener);
//    mEyeGestureManager.stopDetector(EyeGesture.WINK);
    Log.i(TAG, "onStop completed.");
  }

  private GestureDetector createGestureDetector(Context context) {
    GestureDetector gestureDetector = new GestureDetector(context);
    gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
      @Override
      public boolean onGesture(Gesture gesture) {
        if (gesture == Gesture.TAP) {
          Log.i(TAG, "TAP is detected");
          doLaunchContactPicker();
          return true;
        } else if (gesture == Gesture.SWIPE_RIGHT) {
          // do something on right (forward) swipe
          if (MAX_AMOUNT > mAmount) {
            mAmount++;
            updateAmount();
          }
          return true;
        } else if (gesture == Gesture.SWIPE_LEFT) {
          // do something on left (backwards) swipe
          if (0 < mAmount) {
            mAmount--;
            updateAmount();
          }
          return true;
        } else if (gesture == Gesture.TWO_SWIPE_RIGHT) {
          // do something on right (forward) swipe
          if (MAX_AMOUNT > mAmount) {
            mAmount = Math.min(MAX_AMOUNT, mAmount + 10);
            updateAmount();
          }
          return true;
        } else if (gesture == Gesture.TWO_SWIPE_LEFT) {
          // do something on left (backwards) swipe
          if (0 < mAmount) {
            mAmount = Math.max(0, mAmount - 10);
            updateAmount();
          }
          return true;
        }
        return false;
      }
    });
    return gestureDetector;
  }

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

//  private EyeGestureManager.Listener createEyeGestureListener() {
//    EyeGestureManager.Listener listener = new EyeGestureManager.Listener() {
//      @Override
//      public void onEnableStateChange(EyeGesture eyeGesture, boolean paramBoolean) {
//        Log.i(TAG, eyeGesture + " state changed:" + paramBoolean);
//      }
//
//      @Override
//      public void onDetected(final EyeGesture eyeGesture) {
//        Log.i(TAG, eyeGesture + " is detected");
//      }
//    };
//    return listener;
//  }

  private SensorEventListener createSensorListener() {
    SensorEventListener listener = new SensorEventListener() {
      private float[] gravity = new float[3];
      private float[] geomag = new float[3];
      private float[] rotationMatrix;
      private float[] values = new float[3];
      private int triggerRotation1 = 80;
      private int triggerRotation2 = 45;
      private long lastSensorEvent=0;
      private long samplingInterval=5*100*1000*1000;

      @Override
      public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
      }

      @Override
      public final void onSensorChanged(SensorEvent event) {
        //Log.i(TAG, event + " is detected");
        int type = event.sensor.getType();
        // Smoothing the sensor data a bit
        if (type == Sensor.TYPE_MAGNETIC_FIELD) {
          geomag[0] = (geomag[0] * 1 + event.values[0]) * 0.5f;
          geomag[1] = (geomag[1] * 1 + event.values[1]) * 0.5f;
          geomag[2] = (geomag[2] * 1 + event.values[2]) * 0.5f;
        } else if (type == Sensor.TYPE_ACCELEROMETER) {
          gravity[0] = (gravity[0] * 2 + event.values[0]) * 0.33334f;
          gravity[1] = (gravity[1] * 2 + event.values[1]) * 0.33334f;
          gravity[2] = (gravity[2] * 2 + event.values[2]) * 0.33334f;
        }
        rotationMatrix = new float[16];
        SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomag);
        SensorManager.getOrientation(rotationMatrix, values);
        int azimuth = (int) Math.toDegrees(values[0]);
        int pitch = -(int) Math.toDegrees(values[1]);;

        // todo something with values
//        Log.i(TAG, azimuth + " is the azimut");
//        Log.i(TAG, pitch + " is the pitch");
//        Log.i(TAG, triggerRotation1 + " is the triggerRotation1");
//        Log.i(TAG, triggerRotation2 + " is the triggerRotation2");
        if (event.timestamp-lastSensorEvent<samplingInterval) return;
        lastSensorEvent=event.timestamp;
        if (azimuth > 0) {
          if (pitch < triggerRotation2)
            if (MAX_AMOUNT > mAmount) {
              mAmount = Math.min(MAX_AMOUNT, mAmount + 10);
            }
          if (pitch > triggerRotation2 && pitch < triggerRotation1)
            if (MAX_AMOUNT > mAmount) {
              mAmount++;
            }
        }
        if (azimuth < 0) {
          if (pitch < triggerRotation2)
            if (0 < mAmount) {
              mAmount = Math.max(0, mAmount - 10);
            }
          if (pitch > triggerRotation2 && pitch < triggerRotation1)
            if (0 < mAmount) {
              mAmount--;
            }
        }
        updateAmount();
      }
    };
    return listener;
  }

  private void doLaunchContactPicker() {
    startActivity(new Intent(this, ContactActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
    //startActivity(new Intent(this, ContactsListActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));

  }

  /**
   * 
   */
  private void updateAmount() {
    ((TextView) findViewById(R.id.amount)).setText(mCurrencyFormatter.format(mAmount));
  }

}
