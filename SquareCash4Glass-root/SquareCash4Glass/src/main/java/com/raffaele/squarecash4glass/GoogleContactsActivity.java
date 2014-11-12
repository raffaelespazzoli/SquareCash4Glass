package com.raffaele.squarecash4glass;

import java.io.IOException;
import java.net.URL;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.gdata.client.Query;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gdata.util.ServiceException;

public class GoogleContactsActivity extends Activity {

  private static final String TAG = "GoogleContactsActivity";
  
  private Account[] accounts;
  private AccountManager mManager;
  private ContactsService mService;
  private URL feedUrl;
  private final String webapp_clientid="135749034165-lf5m5cc24pmmfbkfqg0k00e7gmcbociq.apps.googleusercontent.com";
  private final String cross_identity_prefix="oauth2:server:client_id:"+webapp_clientid;
  private final String google_account_scope = "https://www.googleapis.com/auth/contacts.readonly";
  private final String auth_scope=cross_identity_prefix+":api_scope:"+google_account_scope;
  private final String ACCOUNTS_TYPE = "com.google";
  private final String AUTH_TOKEN_TYPE = "cp";
  private String mToken;
  private TextView textNumberOfContacts;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.google_contact_list_view);
    textNumberOfContacts = (TextView) findViewById(R.id.textNumberOfContacts);

    mManager = AccountManager.get(this);
    
    Log.d(TAG, "accounts: "+ mManager.getAccounts());
    
    accounts = mManager.getAccountsByType(ACCOUNTS_TYPE);

    Log.d(TAG, "account[0]" + accounts[0]);
    
   mManager.getAuthToken(accounts[0], AUTH_TOKEN_TYPE, null, this, new AccountManagerCallback<Bundle>() {
      public void run(AccountManagerFuture<Bundle> future) {
        try {
          mToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
          GetTask task = new GetTask();
          task.doInBackground(mToken);
        }

        catch (OperationCanceledException e) {
          e.printStackTrace();
        }

        catch (AuthenticatorException e) {
          e.printStackTrace();
        }

        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }, null);
  }



public void createService(String token) throws IOException, ServiceException {
    mService = new ContactsService("contacts_list");
    mService.setUserToken(token);
    Log.d(TAG, "token set in: "+mService);

//    feedUrl = new URL(url);
//
//    ContactFeed resultFeed = (ContactFeed) mService.getFeed(feedUrl, ContactFeed.class);
//    for (ContactEntry entry : resultFeed.getEntries())
//      getContacts(entry);
  }

  private class GetTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... params) {
      try {
        String token = "";
        if (params != null) {
          for (String s : params)
            token += s;
        }
        Log.d(TAG, "token: "+token);
        createService(token);
        printDateMinQueryResults(mService,null);
      }

      catch (ServiceException e) {
        e.printStackTrace();
      }

      catch (IOException e) {
        e.printStackTrace();
      }

      return null;
    }
  }

  public void getContacts(ContactEntry entry) throws ServiceException, IOException {
    if (entry.hasName()) {
      Name name = entry.getName();

      if (name.hasFullName()) {
        String fullName = name.getFullName().getValue();
        Log.d(TAG, fullName);
      }
    }

    else
      Log.d(TAG, "no name");

    if (entry.hasPhoneNumbers()) {
      for (PhoneNumber phone : entry.getPhoneNumbers()) {
        String phoneNum = phone.getPhoneNumber();
        Log.d(TAG, phoneNum);
      }
    }

    else
      Log.d(TAG, "no phone");

  }
  
  public void printDateMinQueryResults(ContactsService myService, DateTime startTime) throws ServiceException, IOException 
  {
    // Create query and submit a request
    URL feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
    Query myQuery = new Query(feedUrl);
    //myQuery.setUpdatedMin(startTime);
    ContactFeed resultFeed = myService.query(myQuery, ContactFeed.class);
    // Print the results
    for (ContactEntry entry : resultFeed.getEntries()) {
      Log.d(TAG,entry.getName().getFullName().getValue());
      Log.d(TAG,"Updated on: " + entry.getUpdated().toStringRfc822());
    }
    
    textNumberOfContacts.setText(resultFeed.getEntries().size());
    
  }
  

}
