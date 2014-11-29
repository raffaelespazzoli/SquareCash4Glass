package com.raffaele.squarecash4glass.contacts;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import retrofit.RestAdapter;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.google.gdata.client.Query;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gdata.util.ServiceException;
import com.raffaele.squarecash4glass.rest.Oauth2CredentialService;
import com.squarecash4glass.rest.data.Oauth2Credential;

public class GoogleContactsUtils {
  private static final String TAG = "GoogleContactsUtils";
  private Context context;
  private static final String AUTH_TOKEN_TYPE = "cp";
  private static final String ACCOUNTS_TYPE = "com.google";
  private static final DateFormat dateFormat=new SimpleDateFormat("yyMMddHHmmssZ", Locale.US);

  public GoogleContactsUtils(Context context) {
    this.context = context;
  }

  private String getToken() {

    AccountManager mManager = AccountManager.get(context);

    Log.d(TAG, "accounts: " + mManager.getAccounts());

    Account[] accounts = mManager.getAccountsByType(ACCOUNTS_TYPE);

    Log.d(TAG, "account[0]" + accounts[0]);

    // TODO at this point the real authentication doesn't work
    // we are going to use the unsecure approach but is is going to be enabled
    // on for myself

    if (!"raffaele.spazzoli@gmail.com".equals(accounts[0])) {
      return null;
    }

    RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("https://squarecash4glass.appspot.com").build();
    Oauth2CredentialService credentialService = restAdapter.create(Oauth2CredentialService.class);
    List<Oauth2Credential> credentials = credentialService.getCredentials(accounts[0].name);

    String token = credentials.get(0).getToken();
    return token;

  }

  public ContactsService createContactService() throws IOException, ServiceException {
    String token = getToken();
    ContactsService mService = new ContactsService("SquareCash4Glass-1.0");
    mService.setProtocolVersion(ContactsService.Versions.V3);
    mService.setUserToken(token);
    Log.d(TAG, "token set in: " + mService);
    return mService;
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
  
  public Pair<DateTime,Integer> getSynchStatus() throws ParseException{
    SynchStatusDTO synchStatusDTO=new Select().from(SynchStatusDTO.class).executeSingle();
    if (synchStatusDTO!=null){
      DateTime dateTime=new DateTime(dateFormat.parse(synchStatusDTO.getLastUpdate()).getTime());
      return new ImmutablePair<DateTime,Integer>(dateTime,synchStatusDTO.getPageCompleted());
    }
    return null;    
  }

  public List<ContactEntry> getContacts(ContactsService myService, DateTime startTime, int page, int pagesize) throws IOException, ServiceException {
    URL feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
    Query myQuery = new Query(feedUrl);
    myQuery.setUpdatedMin(startTime);
    myQuery.setMaxResults(pagesize);
    myQuery.setStartIndex(pagesize * page + 1);
    ContactFeed resultFeed = myService.query(myQuery, ContactFeed.class);
    return resultFeed.getEntries();
  }

  public void saveContacts(List<ContactEntry> contacts, int page) {
    ActiveAndroid.beginTransaction();

    for (ContactEntry entry : contacts) {
      if (!entry.hasName())
        continue;
      ContactDTO contact = new ContactDTO();
      Name name = entry.getName();

      if (name.hasGivenName()) {
        contact.setFirstName(name.getGivenName().getValue());
        if (name.hasFamilyName()) {
          contact.setLastName(name.getFamilyName().getValue());
        }
      } else {
        if (name.hasFullName()) {
          contact.setFirstName(name.getFullName().getValue());
        }

      }
      contact.save();

      List<Email> emails = entry.getEmailAddresses();

      for (Email email : emails) {
        EmailDTO emailDTO = new EmailDTO(contact, email.getAddress());
        emailDTO.save();
      }

      List<PhoneNumber> phoneNumbers = entry.getPhoneNumbers();

      for (PhoneNumber phoneNumber : phoneNumbers) {
        PhoneNumberDTO phoneNumberDTO = new PhoneNumberDTO(contact, phoneNumber.getPhoneNumber());
        phoneNumberDTO.save();
      }
    }
    
    SynchStatusDTO synchStatusDTO=new Select().from(SynchStatusDTO.class).executeSingle();
    synchStatusDTO.setPageCompleted(page);
    synchStatusDTO.save();
    ActiveAndroid.endTransaction();

  }

  public void printDateMinQueryResults(ContactsService myService, DateTime startTime) throws ServiceException, IOException {
    // Create query and submit a request
    URL feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
    Query myQuery = new Query(feedUrl);
    // myQuery.setUpdatedMin(startTime);
    ContactFeed resultFeed = myService.query(myQuery, ContactFeed.class);
    // Print the results
    Log.d(TAG, "number of contacts: " + resultFeed.getEntries().size());
    for (ContactEntry entry : resultFeed.getEntries()) {
      Log.d(TAG, entry.getName().getFullName().getValue());
      Log.d(TAG, "Updated on: " + entry.getUpdated().toStringRfc822());
    }

  }

  public void updateSynchStatus() {
    // TODO Auto-generated method stub
    SynchStatusDTO synchStatusDTO=new Select().from(SynchStatusDTO.class).executeSingle();
    synchStatusDTO.setPageCompleted(0);
    synchStatusDTO.setLastUpdate(dateFormat.format(new Date()));
    synchStatusDTO.save();
  }

}
