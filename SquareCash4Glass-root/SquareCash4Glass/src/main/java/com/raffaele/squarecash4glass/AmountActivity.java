package com.raffaele.squarecash4glass;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
//import com.google.android.glass.eye.EyeGesture;
//import com.google.android.glass.eye.EyeGestureManager;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.Slider;
import com.raffaele.squarecash4glass.contacts.GoogleContactsUtils;
import com.raffaele.squarecash4glass.contacts.synch.ContactSynchEventReceiver;
import com.raffaele.squarecash4glass.payment.PaymentBean;
import com.raffaele.squarecash4glass.payment.PaymentBean.PaymentProvider;
import com.raffaele.squarecash4glass.rest.PreferencesService;
import com.wolfram.alpha.WAEngine;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;
import com.wolfram.alpha.WASubpod;

public class AmountActivity extends Activity {

  private static final String TAG = "AmountActivity";

  private static final int SPEECH_REQUEST = 1;

  private static final String DollarAmountRegExprStr = "[-+]?([0-9]*\\.[0-9]+|[0-9]+)";
  private static final Pattern DollarAmountPattern = Pattern.compile(DollarAmountRegExprStr);

  public static final String WolframEngineAppId = "XUH8J4-Q557W36UQ2";
  private static final int MAX_AMOUNT = 50;
  private static final BigDecimal one = new BigDecimal(1);
  private static final BigDecimal ten = new BigDecimal(10);
  private BigDecimal mAmount = new BigDecimal(0);
  private GestureDetector mGestureDetector;
  private NumberFormat mCurrencyFormatter = NumberFormat.getCurrencyInstance();
  private NumberFormat numberFormatter = NumberFormat.getInstance();
  // private EyeGestureManager mEyeGestureManager;
  // private EyeGestureManager.Listener mEyeGestureListener;
  private SensorManager mSensorManager;
  private SensorEventListener mSensorEventListener;

  private Slider.Indeterminate mIndeterminate;
  private Slider mSlider;
  
  private PaymentBean paymentInfo=new PaymentBean();
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    InstallationListener.onStart(this);
    // start the synchronization if this is first install
    Intent i = new Intent(this, ContactSynchEventReceiver.class);
    i.setAction(ContactSynchEventReceiver.SCHEDULE_COMMAND);
    sendBroadcast(i);
    getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
    mGestureDetector = createGestureDetector(this);
    // mEyeGestureManager = EyeGestureManager.from(this);
    // mEyeGestureListener = createEyeGestureListener();
    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    mSensorEventListener = createSensorListener();
    setContentView(R.layout.amount_view);
 // Set the view for the Slider
    mSlider = Slider.from(findViewById(R.id.textAmountInstruction3));
    updateAmount();   
    getUserPreferences();
    Log.i(TAG, "onCreate completed.");
  }

  /**
   * 
   */
  private void getUserPreferences() {
    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void...voids) {
        GoogleContactsUtils googleContactsUtils=new GoogleContactsUtils(AmountActivity.this);
        String email=googleContactsUtils.getUserEmail();
        PreferencesService preferencesService;
        try {
          preferencesService = googleContactsUtils.createPreferencesService();
          Map<String,String> preferences=preferencesService.getPreferences(email);
          String provider=preferences.get("provider");
          if ("dwolla".equals(provider)){
            paymentInfo.setPaymentProvider(PaymentProvider.DWOLLA);
          }
          if ("venmo".equals(provider)){
            paymentInfo.setPaymentProvider(PaymentProvider.VENMO);
          }
          if ("square".equals(provider)){
            paymentInfo.setPaymentProvider(PaymentProvider.SQUARE);
          }
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
          //for now default provider is dwolla
          paymentInfo.setPaymentProvider(PaymentProvider.DWOLLA);
        }
        return null;
      }

    }.execute();
  }

  @Override
  protected void onStart() {
    super.onStart();
    // mEyeGestureManager.stopDetector(EyeGesture.WINK);
    // mEyeGestureManager.enableDetectorPersistently(EyeGesture.WINK, true);
    // mEyeGestureManager.register(EyeGesture.WINK, mEyeGestureListener);
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
    // mEyeGestureManager.unregister(EyeGesture.WINK, mEyeGestureListener);
    // mEyeGestureManager.stopDetector(EyeGesture.WINK);
    Log.i(TAG, "onStop completed.");
  }

  private GestureDetector createGestureDetector(Context context) {
    GestureDetector gestureDetector = new GestureDetector(context);
    gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
      @Override
      public boolean onGesture(Gesture gesture) {
        if (gesture == Gesture.TAP) {
          Log.i(TAG, "TAP is detected");
          AudioManager audio = (AudioManager) AmountActivity.this.getSystemService(Context.AUDIO_SERVICE);
          audio.playSoundEffect(Sounds.TAP);
          doLaunchGoogleContactsActivity();
          return true;
        } else if (gesture == Gesture.SWIPE_RIGHT) {
          // do something on right (forward) swipe
          if (MAX_AMOUNT > mAmount.intValue()) {
            mAmount = mAmount.add(one);
            updateAmount();
          }
          return true;
        } else if (gesture == Gesture.SWIPE_LEFT) {
          // do something on left (backwards) swipe
          if (0 < mAmount.intValue()) {
            mAmount = mAmount.subtract(one);
            updateAmount();
          }
          return true;
        } else if (gesture == Gesture.TWO_SWIPE_RIGHT) {
          // do something on right (forward) swipe
          if (MAX_AMOUNT > mAmount.intValue()) {
            mAmount = new BigDecimal(Math.min(MAX_AMOUNT, mAmount.intValue() + 10));
            updateAmount();
          }
          return true;
        } else if (gesture == Gesture.TWO_SWIPE_LEFT) {
          // do something on left (backwards) swipe
          if (0 < mAmount.intValue()) {
            mAmount = new BigDecimal(Math.max(0, mAmount.intValue() - 10));
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

  // private EyeGestureManager.Listener createEyeGestureListener() {
  // EyeGestureManager.Listener listener = new EyeGestureManager.Listener() {
  // @Override
  // public void onEnableStateChange(EyeGesture eyeGesture, boolean
  // paramBoolean) {
  // Log.i(TAG, eyeGesture + " state changed:" + paramBoolean);
  // }
  //
  // @Override
  // public void onDetected(final EyeGesture eyeGesture) {
  // Log.i(TAG, eyeGesture + " is detected");
  // }
  // };
  // return listener;
  // }

  private SensorEventListener createSensorListener() {
    SensorEventListener listener = new SensorEventListener() {
      private float[] gravity = new float[3];
      private float[] geomag = new float[3];
      private float[] rotationMatrix;
      private float[] values = new float[3];
      private int triggerRotation1 = 80;
      private int triggerRotation2 = 45;
      private long lastSensorEvent = 0;
      private long samplingInterval = 5 * 100 * 1000 * 1000;

      @Override
      public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
      }

      @Override
      public final void onSensorChanged(SensorEvent event) {
        // Log.i(TAG, event + " is detected");
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
        int pitch = -(int) Math.toDegrees(values[1]);
        ;

        // todo something with values
        // Log.i(TAG, azimuth + " is the azimut");
        // Log.i(TAG, pitch + " is the pitch");
        // Log.i(TAG, triggerRotation1 + " is the triggerRotation1");
        // Log.i(TAG, triggerRotation2 + " is the triggerRotation2");
        if (event.timestamp - lastSensorEvent < samplingInterval)
          return;
        lastSensorEvent = event.timestamp;
        if (azimuth > 0) {
          if (pitch < triggerRotation2)
            if (MAX_AMOUNT > mAmount.intValue()) {
              mAmount = new BigDecimal(Math.min(MAX_AMOUNT, mAmount.intValue() + 10));
            }
          if (pitch > triggerRotation2 && pitch < triggerRotation1)
            if (MAX_AMOUNT > mAmount.intValue()) {
              mAmount = new BigDecimal(Math.min(MAX_AMOUNT, mAmount.intValue() + 1));
            }
        }
        if (azimuth < 0) {
          if (pitch < triggerRotation2)
            if (0 < mAmount.intValue()) {
              mAmount = new BigDecimal(Math.max(0, mAmount.intValue() - 10));
            }
          if (pitch > triggerRotation2 && pitch < triggerRotation1)
            if (0 < mAmount.intValue()) {
              mAmount = new BigDecimal(Math.max(0, mAmount.intValue() - 1));
            }
        }
        updateAmount();
      }
    };
    return listener;
  }

  private void doLaunchGoogleContactsActivity() {
    paymentInfo.setAmount(mAmount);
    Intent i=new Intent(this, GoogleContactsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    i.putExtra(PaymentBean.label, paymentInfo);
    startActivity(i);
  }

  /**
   * 
   */
  private void updateAmount() {
    ((TextView) findViewById(R.id.amount)).setText(mCurrencyFormatter.format(mAmount));
  }

  @Override
  public boolean onCreatePanelMenu(int featureId, Menu menu) {
    if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
      getMenuInflater().inflate(R.menu.amount_activity_menu, menu);
      return true;
    }
    // Pass through to super to setup touch menu.
    return super.onCreatePanelMenu(featureId, menu);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.amount_activity_menu, menu);
    return true;
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
      switch (item.getItemId()) {
      case R.id.spell_amount:
        Log.i(TAG, "spell amount called");
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "say dollars and cents amout for example for $12.34 say twelve dollars and thirty four cents");
        startActivityForResult(intent, SPEECH_REQUEST);
        break;
      default:
        return true;
      }
      return true;
    }
    // Good practice to pass through to super if not handled
    return super.onMenuItemSelected(featureId, item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.i(TAG, "on activity result requestCode: " + requestCode + " resultCode: " + resultCode);
    if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) {
      List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
      String spokenText = results.get(0);
      Log.i(TAG, "spoken text: " + spokenText);
      new AsyncTask<String, Void, BigDecimal>() {

        @Override
        protected void onPreExecute() {
          mIndeterminate=mSlider.startIndeterminate();
        }
        @Override
        protected BigDecimal doInBackground(final String... args) {
          try {
            BigDecimal result = parseformat(args[0]);
            return result;
          } catch (Exception e) {
            Log.e("tag", "error", e);
            return null;
          }
        }
        @Override
        protected void onPostExecute(final BigDecimal success) {
          // Setting data to list adapter
          if (mIndeterminate!=null){
            mIndeterminate.hide();
            mIndeterminate=null;
          }
          if (success!=null){
          mAmount=success;
          }
          else mAmount=new BigDecimal(0);
          updateAmount();

        }
      }.execute(spokenText);
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  private BigDecimal parseformat(String spokenText) throws Exception {
    // The WAEngine is a factory for creating WAQuery objects,
    // and it also used to perform those queries. You can set properties of
    // the WAEngine (such as the desired API output format types) that will
    // be inherited by all WAQuery objects created from it. Most applications
    // will only need to crete one WAEngine object, which is used throughout
    // the life of the application.
    WAEngine engine = new WAEngine();

    // These properties will be set in all the WAQuery objects created from this
    // WAEngine.
    engine.setAppID(WolframEngineAppId);
    engine.addFormat("plaintext");

    // Create the query.
    WAQuery query = engine.createQuery();

    // Set properties of the query.
    query.setInput(spokenText);

    // This sends the URL to the Wolfram|Alpha server, gets the XML result
    // and parses it into an object hierarchy held by the WAQueryResult object.
    WAQueryResult queryResult = engine.performQuery(query);
    if (queryResult.isError()) {
      Log.i(TAG, "Query error");
      Log.i(TAG, "  error code: " + queryResult.getErrorCode());
      Log.i(TAG, "  error message: " + queryResult.getErrorMessage());
      throw new Exception("Query error" + " error code: " + queryResult.getErrorCode() + "  error message: " + queryResult.getErrorMessage());
    } else if (!queryResult.isSuccess()) {
      Log.i(TAG, "Query was not understood; no results available.");
      throw new Exception("Query was not understood; no results available.");
    } else {
      // Got a result.
      for (WAPod pod : queryResult.getPods()) {
        Log.i(TAG, "current pod: " + pod.toString());
        if ("Input interpretation".equals(pod.getTitle())) {
          WASubpod subPod = pod.getSubpods()[0];
          String plainText="";
          for (Object element : subPod.getContents()) {
            if (element instanceof WAPlainText) {
              plainText=((WAPlainText) element).getText();
            }
          }
          Log.i(TAG, "Result subpod plaintext: " + plainText);
          Matcher CVVMatcher = DollarAmountPattern.matcher(plainText);
          // we print the first occurrence if it exists
          String resultString = null;
          if (CVVMatcher.find()) {
            resultString = CVVMatcher.group();
            return new BigDecimal(numberFormatter.parse(resultString).doubleValue());
          }
        }
      }
    }
    throw new Exception("result not found");
  }
}
