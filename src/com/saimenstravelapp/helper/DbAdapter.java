package com.saimenstravelapp.helper;


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
    public static final String KEY_OPPONENT_ID = "opponent_id";
    public static final String KEY_TYPE = "type";
    public static final String KEY_USERNAME = "username";
    
    
    public static final String KEY_GAMEID = "gameId";
    public static final String KEY_INFO = "info";

    private static final String TAG = "DbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "gamerequests";
    private static final String DATABASE_TABLE_REQUESTS = "requests";
    private static final String DATABASE_TABLE_GAMEINFO= "gameinfo";


    private static final int DATABASE_VERSION = 1;
    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	Log.d("onCreateDbAdapter", "dropping images");
          db.execSQL("create table " + DATABASE_TABLE_REQUESTS +  "(" + KEY_OPPONENT_ID + " integer, " + KEY_TYPE +
          		" string, " + KEY_USERNAME + " string);");
          db.execSQL("create table " + DATABASE_TABLE_GAMEINFO +  "(" + KEY_GAMEID + " integer, " +
          		KEY_INFO + " string);");
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
    	mDb.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_REQUESTS);
    	mDb.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_GAMEINFO);

      mDb.execSQL("create table " + DATABASE_TABLE_REQUESTS +  "(" + KEY_OPPONENT_ID + " integer, " + KEY_TYPE +
      		" string, " + KEY_USERNAME + " string);");
      mDb.execSQL("create table " + DATABASE_TABLE_GAMEINFO +  "(" + KEY_GAMEID + " integer, " +
      		KEY_INFO + " string);");
      }

    /**
     * Inserts a new card into the cardTable using the cardsId and cardId provided. 
     * On success return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     */
    
    public long insertNewGameRequest(int opponentId, String type, String username){
    	ContentValues initialValues = new ContentValues();
      initialValues.put(KEY_OPPONENT_ID, opponentId);
      initialValues.put(KEY_TYPE, type);
      initialValues.put(KEY_USERNAME, username);
      
      try {
        long tmp = mDb.insertWithOnConflict(DATABASE_TABLE_REQUESTS, null, initialValues, SQLiteDatabase.CONFLICT_IGNORE);
        return tmp;
      }
      catch(android.database.sqlite.SQLiteConstraintException e){
      	e.printStackTrace();
      }
     	return -1;
    }
    
    public long insertGameInfo(int gameId, String info){
    	ContentValues initialValues = new ContentValues();
      initialValues.put(KEY_GAMEID, gameId);
      initialValues.put(KEY_INFO, info);
      
      try {
        long tmp = mDb.insertWithOnConflict(DATABASE_TABLE_GAMEINFO, null, initialValues, SQLiteDatabase.CONFLICT_IGNORE);
        return tmp;
      }
      catch(android.database.sqlite.SQLiteConstraintException e){
      	e.printStackTrace();
      }
//      catch(android.database.sqlite.SQLiteException e){
//        mDb.execSQL("create table " + DATABASE_TABLE_GAMEINFO +  "(" + KEY_GAMEID + " integer, "+
//        		KEY_INFO + " string);");
//      }
     	return -1;
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteGameRequest(int opponentId) {
        return mDb.delete(DATABASE_TABLE_REQUESTS, KEY_OPPONENT_ID + "=" + opponentId, null) > 0;
    }
    
    public boolean deleteGameInfo(int gameId){
      return mDb.delete(DATABASE_TABLE_GAMEINFO, KEY_GAMEID + "=" + gameId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all cards in the database
     * 
     * @return Cursor over all cards
     */
    public Cursor fetchAllGameRequests() {
        return mDb.query(DATABASE_TABLE_REQUESTS, new String[] {KEY_OPPONENT_ID, KEY_TYPE, KEY_USERNAME}, 
        		null, null, null, null, null);
    }
    
    public Cursor fetchAllGameInfo(int gameId) {
      return mDb.query(DATABASE_TABLE_GAMEINFO, new String[] {KEY_GAMEID, KEY_INFO}, KEY_GAMEID + "=" + gameId, 
      		null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
//    public Cursor fetchImage(long cardId) throws SQLException {
//
//        Cursor mCursor =
//
//            mDb.query(true, DATABASE_TABLE_REQUESTS, new String[] {KEY_LOCATION
//                    }, KEY_CARD_ID + "=" + cardId, null,
//                    null, null, null, null);
//        if (mCursor != null) {
//            mCursor.moveToFirst();
//        }
//        return mCursor;
//    }
    
    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
//    public Cursor fetchCardId(long resourceId) throws SQLException {
//        Cursor mCursor =
//
//            mDb.query(true, DATABASE_TABLE_IMAGES, new String[] {KEY_LOCATION
//                    }, KEY_LOCATION + "=" + resourceId, null,
//                    null, null, null, null);
//        if (mCursor != null) {
//            mCursor.moveToFirst();
//        }
//        return mCursor;
//    }
    
    
//    public Cursor fetchRecent(long resourceId) throws SQLException {
//      Cursor mCursor =
//          mDb.query(true, DATABASE_TABLE_RECENT, new String[] {KEY_FID, KEY_USERNAME
//                  }, null, null, null, null, null, null);
//      if (mCursor != null) {
//          mCursor.moveToFirst();
//      }
//      return mCursor;
//  }
    
//  public long insertNewPlayerImageRecent(int fid, String username){
//  	ContentValues initialValues = new ContentValues();
//    initialValues.put(KEY_LOCATION, fid);
//    initialValues.put(KEY_CARD_ID, username);
//    
//    try {
//      long tmp = mDb.insertWithOnConflict(DATABASE_TABLE_RECENT, null, initialValues, SQLiteDatabase.CONFLICT_IGNORE);
//      
//      
//      return tmp;
//    }
//    catch(android.database.sqlite.SQLiteConstraintException e){
//    	e.printStackTrace();
//    }
//   	return -1;
//  }
}
