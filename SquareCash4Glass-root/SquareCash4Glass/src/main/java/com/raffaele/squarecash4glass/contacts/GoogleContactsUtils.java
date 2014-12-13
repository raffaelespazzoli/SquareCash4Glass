package com.raffaele.squarecash4glass.contacts;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import retrofit.RestAdapter;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.raffaele.squarecash4glass.rest.GoogleContactsService;
import com.raffaele.squarecash4glass.rest.Oauth2CredentialService;
import com.squarecash4glass.rest.data.GoogleContact;
import com.squarecash4glass.rest.data.GoogleContactResult;
import com.squarecash4glass.rest.data.Oauth2Credential;

public class GoogleContactsUtils {
  Logger logger = LoggerFactory.getLogger(GoogleContactsUtils.class);
  private Context context;
  private static final String AUTH_TOKEN_TYPE = "cp";
  private static final String ACCOUNTS_TYPE = "com.google";
  private static final DateFormat dateFormat=new SimpleDateFormat("yyMMddHHmmssZ", Locale.US);

  public GoogleContactsUtils(Context context) {
    this.context = context;
  }
  
  public String getUserEmail(){
    AccountManager mManager = AccountManager.get(context);

    logger.debug("accounts: " + mManager.getAccounts());

    Account[] accounts = mManager.getAccountsByType(ACCOUNTS_TYPE);

    logger.debug("account[0]" + accounts[0]);

    // TODO at this point the real authentication doesn't work
    // we are going to use the unsecure approach but is is going to be enabled
    // on for myself

    if (!"raffaele.spazzoli@gmail.com".equals(accounts[0].name)) {
      return null;
    }
    return accounts[0].name;
  }

//  private String getToken() {
//
//    AccountManager mManager = AccountManager.get(context);
//
//    logger.debug("accounts: " + mManager.getAccounts());
//
//    Account[] accounts = mManager.getAccountsByType(ACCOUNTS_TYPE);
//
//    logger.debug("account[0]" + accounts[0]);
//
//    // TODO at this point the real authentication doesn't work
//    // we are going to use the unsecure approach but is is going to be enabled
//    // on for myself
//
//    if (!"raffaele.spazzoli@gmail.com".equals(accounts[0].name)) {
//      return null;
//    }
//
//    RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("https://squarecash4glass.appspot.com").build();
//    Oauth2CredentialService credentialService = restAdapter.create(Oauth2CredentialService.class);
//    List<Oauth2Credential> credentials = credentialService.getCredentials(accounts[0].name);
//
//    String token = credentials.get(0).getToken();
//    return token;
//
//  }

  public GoogleContactsService createContactService() throws IOException {
    RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("https://squarecash4glass.appspot.com").build();
    GoogleContactsService contactService = restAdapter.create(GoogleContactsService.class);
    return contactService;
  }

//  public void getContacts(ContactEntry entry) throws ServiceException, IOException {
//    if (entry.hasName()) {
//      Name name = entry.getName();
//
//      if (name.hasFullName()) {
//        String fullName = name.getFullName().getValue();
//        logger.debug(fullName);
//      }
//    }
//
//    else
//      logger.debug("no name");
//
//    if (entry.hasPhoneNumbers()) {
//      for (PhoneNumber phone : entry.getPhoneNumbers()) {
//        String phoneNum = phone.getPhoneNumber();
//        logger.debug( phoneNum);
//      }
//    }
//
//    else
//      logger.debug("no phone");
//
//  }
  
  public Pair<Long,Integer> getSynchStatus() throws ParseException{
    SynchStatusDTO synchStatusDTO=new Select().from(SynchStatusDTO.class).executeSingle();
    if (synchStatusDTO!=null){
      Long dateMillis=dateFormat.parse(synchStatusDTO.getLastUpdate()).getTime();
      return new ImmutablePair<Long,Integer>(dateMillis,synchStatusDTO.getPageCompleted());
    }
    return null;    
  }

  public GoogleContactResult getContacts(GoogleContactsService myService, Long startTimeMillis, int page, String email) throws IOException {    
    return myService.getContacts(email, page, startTimeMillis);
  }

  public void saveContacts(List<GoogleContact> contacts, int page) {
    ActiveAndroid.beginTransaction();

    for (GoogleContact entry : contacts) {
      ContactDTO contact = new ContactDTO();
          contact.setLastName(entry.getLastName());
          contact.setFirstName(entry.getFirstName());

     contact.save();

      List<String> emails = entry.getEmails();

      for (String email : emails) {
        EmailDTO emailDTO = new EmailDTO(contact, email);
        emailDTO.save();
      }

      List<String> phoneNumbers = entry.getPhoneNumbers();

      for (String phoneNumber : phoneNumbers) {
        PhoneNumberDTO phoneNumberDTO = new PhoneNumberDTO(contact, phoneNumber);
        phoneNumberDTO.save();
      }
    }
    
    SynchStatusDTO synchStatusDTO=new Select().from(SynchStatusDTO.class).executeSingle();
    synchStatusDTO.setPageCompleted(page);
    synchStatusDTO.save();
    ActiveAndroid.endTransaction();

  }

//  public void printDateMinQueryResults(ContactsService myService, DateTime startTime) throws ServiceException, IOException {
//    // Create query and submit a request
//    URL feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
//    Query myQuery = new Query(feedUrl);
//    // myQuery.setUpdatedMin(startTime);
//    ContactFeed resultFeed = myService.query(myQuery, ContactFeed.class);
//    // Print the results
//    logger.debug( "number of contacts: " + resultFeed.getEntries().size());
//    for (ContactEntry entry : resultFeed.getEntries()) {
//      logger.debug(entry.getName().getFullName().getValue());
//      logger.debug("Updated on: " + entry.getUpdated().toStringRfc822());
//    }
//
//  }

  public void updateSynchStatus() {
    // TODO Auto-generated method stub
    SynchStatusDTO synchStatusDTO=new Select().from(SynchStatusDTO.class).executeSingle();
    synchStatusDTO.setPageCompleted(0);
    synchStatusDTO.setLastUpdate(dateFormat.format(new Date()));
    synchStatusDTO.save();
  }

//  public ContactsService createContactService(String token) {
//    ContactsService mService = new ContactsService("SquareCash4Glass-1.0");
//    mService.setProtocolVersion(ContactsService.Versions.V3);
//    OAuthParameters oauthParameters = new OAuthParameters();
//    oauthParameters.setOAuthToken(token);
//    oauthParameters.setOAuthConsumerKey("Bearer");
//    GoogleAuthTokenFactory.OAuthToken oauthToken = new GoogleAuthTokenFactory.OAuthToken(oauthParameters, new OAuthRsaSha1Signer());
//    HttpGDataRequest.Factory requestFactory = new HttpGDataRequest.Factory();
//    requestFactory.setAuthToken(oauthToken);
//    mService.setRequestFactory(requestFactory);
//    logger.debug("token set in: " + mService);
//    return mService;
//  }

}
