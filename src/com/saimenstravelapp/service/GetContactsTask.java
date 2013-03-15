/**
 * 
 */
package com.saimenstravelapp.service;

import com.saimenstravelapp.activitys.domain.Contact;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;

/**
 * @author Simen
 *
 */
public class GetContactsTask extends AsyncTask<Void, Contact, Void> {

	private AsyncTaskDelegate<Contact> delegate;
	private Context context;

	public GetContactsTask(AsyncTaskDelegate<Contact> delegate, Context context) {
		this.delegate = delegate;
		this.context = context;
	}

	@Override
	protected Void doInBackground(Void... params) {
		getContactsOnPhone();
		return null;
	}

	/**
	 * Gets the contacts on phone. Contact ID and contact email 
	 * are located in two different tables.
	 * @return the contacts
	 */
	public void getContactsOnPhone(){

		ContentResolver cr = this.context.getContentResolver();    
		String[] selectionArgs = new String[] { "1" };
	  String where = ContactsContract.Contacts.IN_VISIBLE_GROUP + "= ? ";

		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
				null, where, selectionArgs, ContactsContract.Contacts.DISPLAY_NAME);
    
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String id = cur.getString(
						cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(
						cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

				getEmailForTheContact(cr, id, name);
			}
		}
	}

	/**
	 * Gets the email for the contact which is stored
	 * in a different table
	 * @param cr the content resolver
	 * @param id the conctact id
	 * @param name the contact name
	 * @return the email match found
	 */
	public void getEmailForTheContact(ContentResolver cr, String id, String name){
		Cursor emailCur = cr.query( 
				ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
				null,
				ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", 
				new String[]{id}, null);


		while (emailCur.moveToNext()) {
			String email = emailCur.getString(
					emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
			
			if(!email.equals(name)){
  			Contact contact = new Contact();
  			contact.setEmail(email);
  			contact.setName(name);
  			publishProgress(contact);				
			}
		}
		emailCur.close();
	}



	@Override
	protected void onProgressUpdate(Contact... values) {
		delegate.publishItem(values[0]);
	}
}
