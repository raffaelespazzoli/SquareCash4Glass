package com.raffaele.squarecash4glass.contacts.synch;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.tuple.Pair;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.raffaele.squarecash4glass.contacts.GoogleContactsUtils;
import com.raffaele.squarecash4glass.rest.GoogleContactsService;
import com.squarecash4glass.rest.data.GoogleContactResult;

public class ContactSynchService extends Service {
  private static int pageSize = 25;
  private static final String TAG = "ContactSynchService";
  private static final Lock lock = new ReentrantLock();

  @Override
  public void onCreate() {
    // TODO Auto-generated method stub
    super.onCreate();
    Log.i(TAG, "onCreate started.");
    Log.i(TAG, "onCreate completed.");
  }

  @Override
  public void onDestroy() {
    // TODO Auto-generated method stub
    super.onDestroy();
    Log.i(TAG, "onDestroy started.");
    Log.i(TAG, "onDestroy completed.");
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    // TODO Auto-generated method stub
    Log.i(TAG, "onStartCommand started.");

    new Thread(new Runnable() {

      @Override
      public void run() {
        // TODO Auto-generated method stub
        Log.i(TAG, "synch thread started.");
        // we need to prevent multiple execution
        if (!lock.tryLock()) {
          return;
        }
        try {
          synchContacts();
        } catch (IOException | ParseException e) {
          // TODO Auto-generated catch block
          Log.e(TAG, "Error synchronizing contacts", e);
        } finally {
          lock.unlock();
        }
      }
    }).start();
    Log.i(TAG, "onStartCommand completed.");
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent arg0) {
    // TODO Auto-generated method stub
    Log.i(TAG, "onBind started.");
    Log.i(TAG, "onBind completed.");
    return null;
  }

  private void synchContacts() throws IOException, ParseException {
    Log.i(TAG, "synchContacts started.");
    GoogleContactsUtils googleContactsUtils = new GoogleContactsUtils(getApplication());
    GoogleContactsService contactService = googleContactsUtils.createContactService();
    Log.i(TAG, "created contact service: " + contactService);
    Pair<Long, Integer> synchStatus = googleContactsUtils.getSynchStatus();
    Log.i(TAG, "retrieved synch status: " + synchStatus);
    GoogleContactResult contacts;
    Long lastUpdate = synchStatus != null ? synchStatus.getLeft() : 0;
    int page = synchStatus != null ? synchStatus.getRight() : 0;
    do {
      Log.i(TAG, "getting contacts for page: " + page +" and time: "+SimpleDateFormat.getInstance().format(new Date(lastUpdate)));
      contacts = googleContactsUtils.getContacts(contactService, lastUpdate, page, googleContactsUtils.getUserEmail());
      Log.i(TAG, "got contacts for page: " + page);
      Log.i(TAG, "retrieved contacts: " + contacts);
      googleContactsUtils.saveContacts(contacts.getGoogleContactList(), page);
      Log.i(TAG, "saved contacts for page: " + page);
      page++;
    } while (page*pageSize < contacts.getTotalResults());
    googleContactsUtils.updateSynchStatus();
    Log.i(TAG, "synchContacts completed.");
  }

}
