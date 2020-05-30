package edu.stevens.cs522.chat.providers;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import edu.stevens.cs522.chat.contracts.BaseContract;
import edu.stevens.cs522.chat.contracts.MessageContract;
import edu.stevens.cs522.chat.contracts.PeerContract;

public class ChatProvider extends ContentProvider {

    public ChatProvider() {
    }

    private static final String AUTHORITY = BaseContract.AUTHORITY;

    private static final String MESSAGE_CONTENT_PATH = MessageContract.CONTENT_PATH;

    private static final String MESSAGE_CONTENT_PATH_ITEM = MessageContract.CONTENT_PATH_ITEM;

    private static final String PEER_CONTENT_PATH = PeerContract.CONTENT_PATH;

    private static final String PEER_CONTENT_PATH_ITEM = PeerContract.CONTENT_PATH_ITEM;


    private static final String DATABASE_NAME = "chat.db";

    private static final int DATABASE_VERSION = 1;

    private static final String MESSAGES_TABLE = "messages";

    private static final String PEERS_TABLE = "peers";

    // Create the constants used to differentiate between the different URI  requests.
    private static final int MESSAGES_ALL_ROWS = 1;
    private static final int MESSAGES_SINGLE_ROW = 2;
    private static final int PEERS_ALL_ROWS = 3;
    private static final int PEERS_SINGLE_ROW = 4;

    public static class DbHelper extends SQLiteOpenHelper {


        private static final String DATABASE_CREATE_Message = // TODO
                " CREATE TABLE "+ MESSAGES_TABLE + "("+ MessageContract._ID+" INTEGER PRIMARY KEY, "+
                        MessageContract.SEQUENCE_NUMBER+" INTEGER, "+
                        MessageContract.MESSAGE_TEXT+" TEXT, "+
                        MessageContract.CHAT_ROOM+" TEXT, "+
                        MessageContract.TIMESTAMP+" INTEGER, "+
                        MessageContract.LATITUDE+" TEXT, "+
                        MessageContract.LONGITUDE+" TEXT, "+
                        MessageContract.SENDER+" TEXT , "+
                        MessageContract.SENDER_ID+" INTEGER, " +
                        "FOREIGN KEY ("+ MessageContract.SENDER+") REFERENCES "+PEERS_TABLE+"("+ PeerContract.NAME+") ON DELETE CASCADE )";

        private static final String DATABASE_CREATE_Peer = // TODO
                "CREATE TABLE " + PEERS_TABLE +"("+ PeerContract._ID + " INTEGER PRIMARY KEY,"+
                        PeerContract.NAME+" TEXT ,"+
                        PeerContract.TIMESTAMP+" TEXT ,"+
                        PeerContract.LATITUDE+ " TEXT ,"+
                        PeerContract.LONGITUDE+ " TEXT " +" )";

        public DbHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO initialize database tables
            db.execSQL(DATABASE_CREATE_Peer);
            db.execSQL(DATABASE_CREATE_Message);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO upgrade database if necessary
            db.execSQL("DROP TABLE IF EXISTS " + MESSAGES_TABLE);
            db.execSQL(("DROP TABLE IF EXISTS " + PEERS_TABLE));
            onCreate(db);
        }
    }

    private DbHelper dbHelper;

    @Override
    public boolean onCreate() {
        // Initialize your content provider on startup.
        dbHelper = new DbHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        return true;
    }

    // Used to dispatch operation based on URI
    private static final UriMatcher uriMatcher;

    // uriMatcher.addURI(AUTHORITY, CONTENT_PATH, OPCODE)
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, MESSAGE_CONTENT_PATH, MESSAGES_ALL_ROWS);
        uriMatcher.addURI(AUTHORITY, MESSAGE_CONTENT_PATH_ITEM, MESSAGES_SINGLE_ROW);
        uriMatcher.addURI(AUTHORITY, PEER_CONTENT_PATH, PEERS_ALL_ROWS);
        uriMatcher.addURI(AUTHORITY, PEER_CONTENT_PATH_ITEM, PEERS_SINGLE_ROW);
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        switch (uriMatcher.match(uri)) {
            case MESSAGES_ALL_ROWS:

            case PEERS_ALL_ROWS:

            case MESSAGES_SINGLE_ROW:

            case PEERS_SINGLE_ROW:

            default:
                throw new IllegalStateException("insert: bad case");
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case MESSAGES_ALL_ROWS:
                // TODO: Implement this to handle requests to insert a new message.
                long messageRow = db.insert(MESSAGES_TABLE,null,values);
                if(messageRow>0)
                {
                    Uri instanceURI = MessageContract.CONTENT_URI(messageRow);
                    ContentResolver cr = getContext().getContentResolver();
                    cr.notifyChange(instanceURI,null);
                    return instanceURI;
                }
                // Make sure to notify any observers
                throw new UnsupportedOperationException("Not yet implemented");
            case PEERS_ALL_ROWS:
                // TODO: Implement this to handle requests to insert a new peer.
                long peerRow = db.insert(PEERS_TABLE,null,values);
                if(peerRow>0)
                {
                    Uri instanceURI = PeerContract.CONTENT_URI(peerRow);
                    ContentResolver cr = getContext().getContentResolver();
                    cr.notifyChange(instanceURI,null);
                    return instanceURI;
                }
                // Make sure to notify any observers
                throw new UnsupportedOperationException("Not yet implemented");
            default:
                throw new IllegalStateException("insert: bad case");
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        ContentResolver cr;
        switch (uriMatcher.match(uri)) {

            case MESSAGES_ALL_ROWS:
                // TODO: Implement this to handle query of all messages.
                cursor = db.query(MESSAGES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                cr = getContext().getContentResolver();
                cursor.setNotificationUri(cr,uri);

                cr.notifyChange(uri,null);
                return cursor;
            case PEERS_ALL_ROWS:
                // TODO: Implement this to handle query of all peers.
                cursor= db.query(PEERS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                cr = getContext().getContentResolver();
                cursor.setNotificationUri(cr,uri);

                cr.notifyChange(uri,null);
                return cursor;
            case MESSAGES_SINGLE_ROW:
                // TODO: Implement this to handle query of a specific message.
                if(uri!=null) {
                    selection = MessageContract._ID + "=?";
                    selectionArgs = new String[]{String.valueOf(BaseContract.getId(uri))};
                    cursor = db.query(MESSAGES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                    cr = getContext().getContentResolver();
                    cursor.setNotificationUri(cr,uri);

                    cr.notifyChange(uri,null);
                    return cursor;
                }
                throw new UnsupportedOperationException("Not yet implemented");
            case PEERS_SINGLE_ROW:
                // TODO: Implement this to handle query of a specific peer.
                if(uri !=null)
                {
                    selection = PeerContract._ID + "=?";
                    selectionArgs = new String[]{String.valueOf(BaseContract.getId(uri))};
                    return db.query(PEERS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                }
                throw new UnsupportedOperationException("Not yet implemented");
            default:
                throw new IllegalStateException("insert: bad case");
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO Implement this to handle requests to update one or more rows.
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentResolver cr;
        switch (uriMatcher.match(uri)) {

            case MESSAGES_ALL_ROWS:
                // TODO: Implement this to handle query of all messages.
                cr = getContext().getContentResolver();
                return db.update(MESSAGES_TABLE, values, selection, selectionArgs);
            case PEERS_ALL_ROWS:
                // TODO: Implement this to handle query of all peers.
                return db.update(PEERS_TABLE, values, selection, selectionArgs);
            case MESSAGES_SINGLE_ROW:
                // TODO: Implement this to handle query of a specific message.
                if (uri != null) {
                    selection = MessageContract._ID + "=?";
                    selectionArgs = new String[]{String.valueOf(BaseContract.getId(uri))};
                    return db.update(MESSAGES_TABLE, values, selection, selectionArgs);
                }
                throw new UnsupportedOperationException("Not yet implemented");
            case PEERS_SINGLE_ROW:
                // TODO: Implement this to handle query of a specific peer.
                if (uri != null) {
                    selection = PeerContract._ID + "=?";
                    selectionArgs = new String[]{String.valueOf(BaseContract.getId(uri))};
                    return db.update(PEERS_TABLE, values, selection, selectionArgs);
                }
                throw new UnsupportedOperationException("Not yet implemented");
            default:
                throw new IllegalStateException("insert: bad case");

        }
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Implement this to handle requests to delete one or more rows.
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        switch (uriMatcher.match(uri)) {

            case MESSAGES_ALL_ROWS:
                // TODO: Implement this to handle query of all messages.
                return db.delete(MESSAGES_TABLE, selection, selectionArgs);
            case PEERS_ALL_ROWS:
                // TODO: Implement this to handle query of all peers.
                return db.delete(PEERS_TABLE, selection, selectionArgs);
            case MESSAGES_SINGLE_ROW:
                // TODO: Implement this to handle query of a specific message.
                if (uri != null) {
                    selection = MessageContract._ID + "=?";
                    selectionArgs = new String[]{String.valueOf(BaseContract.getId(uri))};
                    return db.delete(MESSAGES_TABLE, selection, selectionArgs);
                }
                throw new UnsupportedOperationException("Not yet implemented");
            case PEERS_SINGLE_ROW:
                // TODO: Implement this to handle query of a specific peer.
                if (uri != null) {
                    selection = PeerContract._ID + "=?";
                    selectionArgs = new String[]{String.valueOf(BaseContract.getId(uri))};
                    return db.delete(PEERS_TABLE, selection, selectionArgs);
                }
                throw new UnsupportedOperationException("Not yet implemented");
            default:
                throw new IllegalStateException("insert: bad case");

        }
    }

}
