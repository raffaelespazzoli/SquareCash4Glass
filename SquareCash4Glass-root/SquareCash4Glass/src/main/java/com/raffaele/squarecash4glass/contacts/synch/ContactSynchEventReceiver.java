package com.raffaele.squarecash4glass.contacts.synch;

import java.util.Calendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ContactSynchEventReceiver extends BroadcastReceiver {

  private static boolean initialized = false;
  private static final Lock lock = new ReentrantLock();
  private static final String TAG = "ContactSynchEventReceiver";
  public static final String SCHEDULE_COMMAND = "com.raffaele.squarecash4glass.Schedule";
  public static final String START_COMMAND = "com.raffaele.squarecash4glass.Start";
  public static final long interval = 1000 * 60 * 60 * 24;

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i(TAG, "onReceive started.");
    if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) || SCHEDULE_COMMAND.equals(intent.getAction())) {
      Log.i(TAG, "boot completed or schedule command received.");
      // regardless of how we have been awaked let's check if execution is
      // already
      // scheduled
      lock.lock();
      try {
        if (!initialized) {
          Log.i(TAG, "initialized: " + initialized);
          AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

          Intent i = new Intent(context, ContactSynchEventReceiver.class);
          i.setAction(START_COMMAND);
          PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
          Calendar cal = Calendar.getInstance();
          // Start 30 seconds after boot completed
          cal.add(Calendar.SECOND, 30);
          //
          // Fetch every 30 seconds
          // InexactRepeating allows Android to optimize the energy consumption
          service.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), interval, pending);
          initialized = true;
          Log.i(TAG, "alarm scheduled");
        }
      } finally {
        lock.unlock();
      }
    }
    if (START_COMMAND.equals(intent.getAction())) {
      Log.i(TAG, "start command received.");
      Intent service = new Intent(context, ContactSynchService.class);
      context.startService(service);
      Log.i(TAG, "service started.");
    }
    Log.i(TAG, "onReceive completed.");
  }
}
