package com.main.helper;

import com.main.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class DbAdapter {

   // public static final String KEY_CARDS_ID = "cardsId";
    public static final String KEY_CARD_ID = "cardId";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_FID = "FID";
    public static final String KEY_USERNAME = "username";
    
    private static final String TAG = "DbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE_IMAGES = "images";
    private static final String DATABASE_TABLE_RECENT = "recent";

    private static final int DATABASE_VERSION = 2;
    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	Log.d("onCreateDbAdapter", "dropping images");
          db.execSQL("DROP TABLE IF EXISTS images");
          db.execSQL("DROP TABLE IF EXISTS recent");
          db.execSQL("create table images (cardId integer, location integer);");
          db.execSQL("create table recent (fid integer, username text);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }
    
    public void wipeAndCreateDatabase() {
    	mDb.execSQL("DROP TABLE IF EXISTS images");
      mDb.execSQL("create table images (cardId integer, location integer);");
    }

    /**
     * Inserts a new card into the cardTable using the cardsId and cardId provided. 
     * On success return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     */
    
    public long insertCardIdToImageTable(int location, int cardId){
    	ContentValues initialValues = new ContentValues();
      initialValues.put(KEY_LOCATION, location);
      initialValues.put(KEY_CARD_ID, cardId);
      try {
        long tmp = mDb.insertWithOnConflict(DATABASE_TABLE_IMAGES, null, initialValues, SQLiteDatabase.CONFLICT_IGNORE);
        return tmp;
      }
      catch(android.database.sqlite.SQLiteConstraintException e){
      	e.printStackTrace();
      }
     	return -1;
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteCardsForCardsId(long cardId) {

        return mDb.delete(DATABASE_TABLE_IMAGES, KEY_CARD_ID + "=" + cardId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all cards in the database
     * 
     * @return Cursor over all cards
     */
//    public Cursor fetchAllCards() {
//
//        return mDb.query(DATABASE_TABLE_IMAGES, new String[] {KEY_CARDS_ID, KEY_CARD_ID}, null, null, null, null, null);
//    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchImage(long cardId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE_IMAGES, new String[] {KEY_LOCATION
                    }, KEY_CARD_ID + "=" + cardId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchCardId(long resourceId) throws SQLException {
        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE_IMAGES, new String[] {KEY_LOCATION
                    }, KEY_LOCATION + "=" + resourceId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    
    public Cursor fetchRecent(long resourceId) throws SQLException {
      Cursor mCursor =
          mDb.query(true, DATABASE_TABLE_RECENT, new String[] {KEY_FID, KEY_USERNAME
                  }, null, null, null, null, null, null);
      if (mCursor != null) {
          mCursor.moveToFirst();
      }
      return mCursor;
  }
    
  public long insertNewPlayerImageRecent(int fid, String username){
  	ContentValues initialValues = new ContentValues();
    initialValues.put(KEY_LOCATION, fid);
    initialValues.put(KEY_CARD_ID, username);
    
    try {
      long tmp = mDb.insertWithOnConflict(DATABASE_TABLE_RECENT, null, initialValues, SQLiteDatabase.CONFLICT_IGNORE);
      
      
      return tmp;
    }
    catch(android.database.sqlite.SQLiteConstraintException e){
    	e.printStackTrace();
    }
   	return -1;
  }
}
