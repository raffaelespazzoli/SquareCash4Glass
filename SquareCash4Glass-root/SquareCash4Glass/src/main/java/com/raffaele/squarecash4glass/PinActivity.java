package com.raffaele.squarecash4glass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.touchpad.GestureDetector.BaseListener;
import com.google.android.glass.view.WindowUtils;
import com.raffaele.squarecash4glass.payment.PaymentBean;

public class PinActivity extends Activity implements BaseListener, View.OnFocusChangeListener {

  private static final String TAG = "GoogleContactsActivity";

  private List<TextView> digitViewList = new ArrayList<TextView>();
  private List<Integer> listOfDigit = new ArrayList<Integer>();
  private int focused = 0;

  private GestureDetector mDetector;

  private static final int SPEECH_REQUEST = 1;
  
  private PaymentBean paymentInfo; 

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    
    Bundle extras = getIntent().getExtras();
    if (extras != null) {
        paymentInfo = (PaymentBean)extras.get(PaymentBean.label);
        Log.i(TAG, "deserialized paymentInfo: "+paymentInfo);
    }
    
    getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);

    setContentView(R.layout.pin_view);
    digitViewList.add((TextView) findViewById(R.id.digit1));
    digitViewList.add((TextView) findViewById(R.id.digit2));
    digitViewList.add((TextView) findViewById(R.id.digit3));
    digitViewList.add((TextView) findViewById(R.id.digit4));

    for (View v : digitViewList) {
      v.setOnFocusChangeListener(this);
    }

    listOfDigit = Arrays.asList(0, 0, 0, 0);
    focused = 0;

    mDetector = new GestureDetector(this).setBaseListener(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onResume()
   */
  @Override
  protected void onResume() {
    // TODO Auto-generated method stub
    super.onResume();
    updatePin();
  }

  @Override
  public boolean onGenericMotionEvent(MotionEvent event) {
    return mDetector.onMotionEvent(event);
  }

  @Override
  public boolean onGesture(Gesture gesture) {
    switch (gesture) {
    case TAP: {
      AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
      audio.playSoundEffect(Sounds.TAP);
      doLaunchTransactionCompleted();
      return true;
    }
    case SWIPE_LEFT: {
      Log.i(TAG, "focused view before event #: " + focused);
      View nextFocusView = getCurrentFocus().focusSearch(View.FOCUS_LEFT);
      nextFocusView.requestFocus();
      focused = focused+1 % 4;
      Log.i(TAG, "focused view after event #: " + focused);
      return true;
    }
    case SWIPE_RIGHT: {
      Log.i(TAG, "focused view before event #: " + focused);
      View nextFocusView = getCurrentFocus().focusSearch(View.FOCUS_RIGHT);
      nextFocusView.requestFocus();
      //deal with the fact that in java module of a negative number is a negative number.
      focused = ((focused-1 % 4) + 4) % 4;
      Log.i(TAG, "focused view after event #: " + focused);
      return true;
    }
    case SWIPE_UP: {
      listOfDigit.set(focused, Math.min(9, listOfDigit.get(focused) + 1));
      updatePin();
      return true;
    }
    case SWIPE_DOWN: {
      listOfDigit.set(focused, Math.max(0, listOfDigit.get(focused) - 1));
      updatePin();
      return true;
    }
    default:
      return false;
    }
  }

  private void doLaunchTransactionCompleted() {
    Intent i=new Intent(this, TransactionCompleted.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    paymentInfo.setAuthCode(StringUtils.join(listOfDigit.toArray(),null));
    i.putExtra(PaymentBean.label, paymentInfo);
    startActivity(i);
  }

  @Override
  public void onFocusChange(View v, boolean hasFocus) {
    if (hasFocus) {
      Log.i(TAG, "focus gained by view: " + v.getId());
      v.setBackgroundColor(Color.DKGRAY);
    } else {
      Log.i(TAG, "focus lost by view: " + v.getId());
      v.setBackgroundColor(Color.BLACK);
    }
  }

  @Override
  public boolean onCreatePanelMenu(int featureId, Menu menu) {
    if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
      getMenuInflater().inflate(R.menu.pin_activity_menu, menu);
      return true;
    }
    // Pass through to super to setup touch menu.
    return super.onCreatePanelMenu(featureId, menu);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.pin_activity_menu, menu);
    return true;
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
      switch (item.getItemId()) {
      case R.id.spell_pin:
        Log.i(TAG, "spell pin called");
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "say each digit of your pin as a separate word");
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
      listOfDigit = parseText(spokenText);
      updatePin();
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  /**
   * @param listOfDigits
   */
  private void updatePin() {
    for (int i = 0; i < listOfDigit.size(); i++) {
      digitViewList.get(i).setText(listOfDigit.get(i).toString());
    }
  }

  private List<Integer> parseText(String spokenText) {
    List<Integer> listOfDigits = new ArrayList<Integer>();
    // first attempt we assume a number
    try {
      int intResult=Integer.valueOf(spokenText);
      Log.i(TAG, "integer conversion result: " + intResult);
      char[] arrayOfDigit = spokenText.toCharArray();
      for (char c : arrayOfDigit) {
        Log.i(TAG, "adding: " + c);
        listOfDigits.add(Integer.valueOf(new String(new char[]{c})));
      }
      return listOfDigits;
    } catch (NumberFormatException e) {
      // suppress we continue with the second attempt
    }

    // second attempt we assume a sequence of digits

    String[] arrayOfStringDigit = spokenText.split("([^\\s]+)");
    for (String digit : arrayOfStringDigit) {
      if ("0".equals(digit) || ("zero".equals(digit))) {
        listOfDigits.add(0);
      }
      if ("1".equals(digit) || ("one".equals(digit))) {
        listOfDigits.add(1);
        continue;
      }
      if ("2".equals(digit) || ("two".equals(digit))) {
        listOfDigits.add(2);
        continue;
      }
      if ("3".equals(digit) || ("three".equals(digit))) {
        listOfDigits.add(3);
        continue;
      }
      if ("4".equals(digit) || ("four".equals(digit))) {
        listOfDigits.add(4);
        continue;
      }
      if ("5".equals(digit) || ("five".equals(digit))) {
        listOfDigits.add(5);
        continue;
      }
      if ("6".equals(digit) || ("six".equals(digit))) {
        listOfDigits.add(6);
        continue;
      }
      if ("7".equals(digit) || ("seven".equals(digit))) {
        listOfDigits.add(7);
        continue;
      }
      if ("8".equals(digit) || ("eight".equals(digit))) {
        listOfDigits.add(8);
        continue;
      }
      if ("9".equals(digit) || ("nine".equals(digit))) {
        listOfDigits.add(9);
      }
    }
    return listOfDigits;
  }

}
