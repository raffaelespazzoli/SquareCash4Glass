/**
 * 
 */
package com.raffaele.squarecash4glass;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * @author SPAZZRA
 * 
 */
public class ContactActivity extends Activity {

  private static final String TAG = "ContactActivity";
  /*
   * Defines an array that contains column names to move from the Cursor to the
   * ListView.
   */

  private final static String[] FROM_COLUMNS = { Contacts.DISPLAY_NAME_PRIMARY };

  /*
   * Defines an array that contains resource ids for the layout views that get
   * the Cursor column contents. The id is pre-defined in the Android framework,
   * so it is prefaced with "android.R.id"
   */
  private final static int[] TO_IDS = { android.R.id.text1 };

  private static final String[] PROJECTION = { Contacts._ID, Contacts.LOOKUP_KEY, Contacts.DISPLAY_NAME_PRIMARY };

  // The column index for the _ID column
  private static final int CONTACT_ID_INDEX = 0;
  // The column index for the LOOKUP_KEY column
  private static final int LOOKUP_KEY_INDEX = 1;

  // Defines the text expression
  private static final String SELECTION =Contacts.DISPLAY_NAME_PRIMARY +" LIKE ?";
  // Defines a variable for the search string
  private String mSearchString="%" + "raffaele" + "%";
  // Defines the array to hold values that replace the ?
  private String[] mSelectionArgs = { mSearchString };

  private ListView mContactListView;
  private SimpleCursorAdapter mCursorAdapter;

  private long mContactId;
  // The contact's LOOKUP_KEY
  private String mContactKey;
  // A content URI for the selected contact
  private Uri mContactUri;

  LoaderManager.LoaderCallbacks<Cursor> mLoaderListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.i(TAG, "onCreate started.");
    setContentView(R.layout.contact_view_card);
    mContactListView = (ListView) findViewById(R.id.contactlistview);
    mCursorAdapter = new SimpleCursorAdapter(this, R.id.textView1, null, FROM_COLUMNS, TO_IDS, 0);
    // Sets the adapter for the ListView
    mContactListView.setAdapter(mCursorAdapter);
    mContactListView.setOnItemClickListener(createOnItemClickListener());
    mLoaderListener = createLoaderListener();
    getLoaderManager().initLoader(0, null, mLoaderListener);
    Log.i(TAG, "onCreate completed.");
  }

  private LoaderCallbacks<Cursor> createLoaderListener() {
    LoaderManager.LoaderCallbacks<Cursor> mLoaderListener = new LoaderManager.LoaderCallbacks<Cursor>() {

      @Override
      public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        /*
         * Makes search string into pattern and stores it in the selection array
         */
        
        // Starts the query
        CursorLoader cl=new CursorLoader(ContactActivity.this, Contacts.CONTENT_URI, PROJECTION, SELECTION, mSelectionArgs, null);
        Log.i(TAG, "onCreateLoader completed.");
        return cl;
      }

      @Override
      public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Put the result Cursor in the adapter for the ListView
        mCursorAdapter.swapCursor(data);
        Log.i(TAG, "onLoadFinished completed.");
      }

      @Override
      public void onLoaderReset(Loader<Cursor> loader) {
        // Delete the reference to the existing Cursor
        mCursorAdapter.swapCursor(null);
        Log.i(TAG, "onLoaderReset completed.");
      }

    };
    return mLoaderListener;
  }

  private AdapterView.OnItemClickListener createOnItemClickListener() {
    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Get the Cursor
        Cursor cursor = ((CursorAdapter) parent.getAdapter()).getCursor();
        // Move to the selected contact
        cursor.moveToPosition(position);
        // Get the _ID value
        mContactId = cursor.getLong(CONTACT_ID_INDEX);
        // Get the selected LOOKUP KEY
        mContactKey = cursor.getString(LOOKUP_KEY_INDEX);
        // Create the contact's content Uri
        mContactUri = Contacts.getLookupUri(mContactId, mContactKey);
        Log.i(TAG, "onItemClick completed.");
      }

    };
    return onItemClickListener;
  }

}
