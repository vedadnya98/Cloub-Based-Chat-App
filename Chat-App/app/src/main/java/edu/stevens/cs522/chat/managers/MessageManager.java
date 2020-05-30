package edu.stevens.cs522.chat.managers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import edu.stevens.cs522.chat.async.AsyncContentResolver;
import edu.stevens.cs522.chat.async.IContinue;
import edu.stevens.cs522.chat.async.IEntityCreator;
import edu.stevens.cs522.chat.async.IQueryListener;
import edu.stevens.cs522.chat.contracts.MessageContract;
import edu.stevens.cs522.chat.entities.ChatMessage;
import edu.stevens.cs522.chat.entities.Peer;


/**
 * Created by dduggan.
 */

public class MessageManager extends Manager<ChatMessage> {

    private static int LOADER_ID = 1;

    private static final IEntityCreator<ChatMessage> creator = new IEntityCreator<ChatMessage>() {
        @Override
        public ChatMessage create(Cursor cursor) {
            return new ChatMessage(cursor);
        }
    };

    public MessageManager(Context context) {
        super(context, creator, LOADER_ID);
    }

    public void getAllMessagesAsync(IQueryListener<ChatMessage> listener) {
        // TODO use QueryBuilder to complete this
        executeQuery(MessageContract.CONTENT_URI,null,null,null,null,listener);
    }

    public void getAllMessagesAsyncReload(IQueryListener<ChatMessage> listener) {
        // TODO use QueryBuilder to complete this
        reexecuteQuery(MessageContract.CONTENT_URI,null,null,null,null,listener);
    }

    public void getMessagesByPeerAsync(Peer peer, IQueryListener<ChatMessage> listener) {
        // TODO use QueryBuilder to complete this
        // Remember to reset the loader!

        reexecuteQuery(MessageContract.CONTENT_URI,null,MessageContract.SENDER+"="+peer.id,null,null,listener);
    }

    public void persistAsync(ChatMessage ChatMessage) {
        // TODO use AsyncContentResolver to complete this
        ContentValues contentValues = new ContentValues();
        ChatMessage.writeToProvider(contentValues);
        AsyncContentResolver asyncContentResolver = getAsyncResolver();
        //final IContinue<Long> callback = null;
        asyncContentResolver.insertAsync(MessageContract.CONTENT_URI,contentValues,new IContinue<Uri>() {
            public void kontinue(Uri uri) {
                getSyncResolver().notifyChange(uri,null);
            }
        });
    }


    public long persist(ChatMessage message)
    {
        ContentValues contentValues = new ContentValues();
        message.writeToProvider(contentValues);
        ContentResolver contentResolver = this.getSyncResolver();
        contentResolver.insert(MessageContract.CONTENT_URI,contentValues);
        return message.id;

    }

    public void update(ChatMessage chatMessage, long messageId)
    {

        ContentValues contentValues = new ContentValues();
        chatMessage.writeToProvider(contentValues);
        ContentResolver contentResolver = this.getSyncResolver();
        String where = MessageContract.ID + "=?";
        String[] condition = {((Long) messageId).toString()};
        contentResolver.update(MessageContract.CONTENT_URI, contentValues, where, condition);
    }

}
