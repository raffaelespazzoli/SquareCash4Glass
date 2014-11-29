package com.raffaele.squarecash4glass.contacts.synch;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.util.ServiceException;
import com.raffaele.squarecash4glass.contacts.GoogleContactsUtils;

public class ContactSynchService extends Service {
  private static int pageSize=25;
  private static final String TAG = "GoogleContactsUtils";
  
  
  @Override
  public void onCreate() {
    // TODO Auto-generated method stub
    super.onCreate();
  }

  @Override
  public void onDestroy() {
    // TODO Auto-generated method stub
    super.onDestroy();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    // TODO Auto-generated method stub
    new Thread(new Runnable(){

      @Override
      public void run() {
        // TODO Auto-generated method stub
        try {
          synchContacts();
        } catch (IOException | ServiceException | ParseException e) {
          // TODO Auto-generated catch block
          Log.e(TAG, "Error synchronizing contacts", e);
        }
      }
      
    }).start();
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent arg0) {
    // TODO Auto-generated method stub
    return null;
  }
  
  private void synchContacts() throws IOException, ServiceException, ParseException{
    GoogleContactsUtils googleContactsUtils=new GoogleContactsUtils(getApplication());
    ContactsService contactService=googleContactsUtils.createContactService();
    Pair<DateTime,Integer> synchStatus=googleContactsUtils.getSynchStatus();
    List<ContactEntry> contacts=Collections.emptyList();
    DateTime lastUpdate=synchStatus!=null?synchStatus.getLeft():new DateTime(0);
    int page=synchStatus!=null?synchStatus.getRight():0;
    do {
      contacts=googleContactsUtils.getContacts(contactService, null, page, pageSize);
      googleContactsUtils.saveContacts(contacts,page);
      page++;
    }
    while (contacts.size()==pageSize);
    googleContactsUtils.updateSynchStatus();
  }

}
