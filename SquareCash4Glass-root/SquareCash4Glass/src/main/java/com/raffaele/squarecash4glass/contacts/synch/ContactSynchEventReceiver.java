package com.raffaele.squarecash4glass.contacts.synch;

import java.util.Calendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ContactSynchEventReceiver extends BroadcastReceiver {

  private static boolean initialized = false;
  private static Lock lock=new ReentrantLock();

  @Override
  public void onReceive(Context context, Intent intent) {
    // TODO Auto-generated method stub
    // regardless of how we have been awaked let's check if execution is already
    // scheduled
    lock.lock();
      if (!initialized) {
        AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(context, ContactSynchService.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar cal = Calendar.getInstance();
        // Start 30 seconds after boot completed
        cal.add(Calendar.SECOND, 30);
        //
        // Fetch every 30 seconds
        // InexactRepeating allows Android to optimize the energy consumption
        service.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pending);
        initialized=true;
      }
      lock.unlock();
    }
}
