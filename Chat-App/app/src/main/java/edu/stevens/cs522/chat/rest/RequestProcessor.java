package edu.stevens.cs522.chat.rest;

import android.content.ContentValues;
import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

import edu.stevens.cs522.base.StringUtils;
import edu.stevens.cs522.chat.entities.ChatMessage;
import edu.stevens.cs522.chat.entities.Peer;
import edu.stevens.cs522.chat.managers.MessageManager;
import edu.stevens.cs522.chat.managers.PeerManager;
import edu.stevens.cs522.chat.managers.RequestManager;
import edu.stevens.cs522.chat.managers.TypedCursor;
import edu.stevens.cs522.chat.settings.Settings;

/**
 * Created by dduggan.
 */

public class RequestProcessor {

    private Context context;

    private RestMethod restMethod;

    private RequestManager requestManager;

    static  int  flag =0;
    static int flag1=0;

    public RequestProcessor(Context context) {
        this.context = context;
        this.restMethod = new RestMethod(context);
        // Used for managing messages in the database
        this.requestManager = new RequestManager(context);
    }

    public Response process(Request request) {

            return request.process(this);


    }

    public Response perform(RegisterRequest request) {
        Response response = restMethod.perform(request);
        PeerManager peerManager = new PeerManager(context);
        if (response instanceof RegisterResponse) {
            // TODO update the user name and sender id in settings, updated peer record PK
            Settings.saveSenderId(context, (((RegisterResponse) response).getSenderId()));
            Settings.saveChatName(context, request.chatname);

            Peer peer = new Peer();
            peer.name = Settings.getChatName(context);
            peer.latitude = request.latitude;
            peer.longitude = request.longitude;
            peer.timestamp = request.timestamp;
            peer.id = Settings.getSenderId(context);

            peerManager.persist(peer);
        }
        return response;
    }

    public Response perform(PostMessageRequest request) {
        if (!Settings.SYNC) {
            // TODO insert the message into the local database
            MessageManager messageManager = new MessageManager(context);
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.chatRoom = request.chatRoom;
            chatMessage.latitude = request.latitude;
            chatMessage.longitude = request.longitude;
            chatMessage.messageText = request.message;
            chatMessage.timestamp = request.timestamp;
            chatMessage.senderId = Settings.getSenderId(context);
            chatMessage.sender = Settings.getChatName(context);

            long messageId = messageManager.persist(chatMessage);

            Response response = restMethod.perform(request);
            if (response instanceof PostMessageResponse) {
                // TODO update the message in the database with the sequence number
                long seqNum =  ((PostMessageResponse) response).getMessageId();
                chatMessage.chatRoom = request.chatRoom;
                chatMessage.latitude = request.latitude;
                chatMessage.longitude = request.longitude;
                chatMessage.messageText = request.message;
                chatMessage.timestamp = request.timestamp;
                chatMessage.senderId = request.senderId;
                chatMessage.sender = Settings.getChatName(context);
                chatMessage.seqNum = seqNum;

                messageManager.update(chatMessage, messageId);
            }
            return response;
        } else {
            /*
             * We will just insert the message into the database, and rely on background
             * sync to upload.
             */
            ChatMessage chatMessage = new ChatMessage();
            // TODO fill the fields with values from the request message
            chatMessage.chatRoom = request.chatRoom;
            chatMessage.latitude = request.latitude;
            chatMessage.longitude = request.longitude;
            chatMessage.messageText = request.message;
            chatMessage.timestamp = request.timestamp;
            chatMessage.senderId = request.senderId;
            chatMessage.sender = Settings.getChatName(context);
            requestManager.persist(chatMessage);
            return request.getDummyResponse();
        }
    }

    /**
     * For SYNC: perform a sync using a request manager
     * @param request
     * @return
     */
    public Response perform(SynchronizeRequest request) {
        RestMethod.StreamingResponse response = null;
        final TypedCursor<ChatMessage> messages = requestManager.getUnsentMessages();
        final int numMessagesReplaced = messages.getCount();
        Log.i("RequestProcessor","numMessagesReplaced="+numMessagesReplaced);
        try {
            /*
             * This is the callback from streaming new local messages to the server.
             */
            RestMethod.StreamingOutput out = new RestMethod.StreamingOutput() {
                @Override
                public void write(final OutputStream os) throws IOException {
                    try {

                            JsonWriter wr = new JsonWriter(new OutputStreamWriter(new BufferedOutputStream(os)));
                            wr.beginArray();
                            /*
                             * TODO stream unread messages to the server:
                             * {
                             *   chatroom : ...,
                             *   timestamp : ...,
                             *   latitude : ...,
                             *   longitude : ....,
                             *   text : ...
                             * }
                             */
                                if (messages.moveToFirst()) {
                                    do {
                                        ChatMessage message = new ChatMessage(messages.getCursor());
                                        wr.beginObject();
                                        wr.name("chatroom").value(message.chatRoom);
                                        wr.name("timestamp").value(message.timestamp.getTime());
                                        wr.name("latitude").value(message.latitude);
                                        wr.name("longitude").value(message.longitude);
                                        wr.name("text").value(message.messageText);
                                        wr.endObject();
                                    } while (messages.moveToNext());
                                }
                                wr.endArray();
                                wr.flush();

                    } finally {
                        messages.close();
                    }
                }
            };
            /*r
             * Connect to the server and upload messages not yet shared.
             */
                request.lastSequenceNumber = requestManager.getLastSequenceNumber();
                if(numMessagesReplaced > 0) {
                    response = restMethod.perform(request, out);
                }
                else
                {
                    response = restMethod.perform(request,null);
                }
              /*
                 * Stream downloaded peer and message information, and update the database.
                 * The connection is closed in the finally block below.
                 */

                    JsonReader rd = new JsonReader(new InputStreamReader(new BufferedInputStream(response.getInputStream()), StringUtils.CHARSET));
                    // TODO parse data from server (messages and peers) and update database
                    // See RequestManager for operations to help with this.

                    String sectionName;
                    String valueName;
                    long seqNum = 0;


                    rd.beginObject();
                    while (rd.hasNext()) {
                        sectionName = rd.nextName();
                        if (sectionName.equals("clients")) {
                            rd.beginArray();


                            while (rd.hasNext()) {
                                rd.beginObject();
                                Peer receivePeer = new Peer();
                                while (rd.hasNext()) {
                                    valueName = rd.nextName();
                                    switch (valueName) {
                                        case "id":
                                            receivePeer.id = rd.nextInt();
                                            break;
                                        case "username":
                                            receivePeer.name = rd.nextString();
                                            break;
                                        case "timestamp":
                                            long t = rd.nextLong();
                                            receivePeer.timestamp = new Date(t);
                                            break;
                                        case "latitude":
                                            receivePeer.latitude = rd.nextDouble();
                                            break;
                                        case "longitude":
                                            receivePeer.longitude = rd.nextDouble();
                                            break;
                                    }
                                }
                                rd.endObject();

                                ContentValues values = new ContentValues();
                                receivePeer.writeToProvider(values);
                                PeerManager peerManager = new PeerManager(context);
                                peerManager.persist(receivePeer);
                            }

                            rd.endArray();
                        } else if (sectionName.equals("messages")) {
                            rd.beginArray();

                            while (rd.hasNext()) {
                                rd.beginObject();
                                ChatMessage receiveMessage = new ChatMessage();
                                while (rd.hasNext()) {
                                    valueName = rd.nextName();
                                    switch (valueName) {
                                        case "chatroom":
                                            receiveMessage.chatRoom = rd.nextString();
                                            break;
                                        case "timestamp":
                                            receiveMessage.timestamp = new Date(rd.nextLong());
                                            break;
                                        case "latitude":
                                            receiveMessage.latitude = rd.nextDouble();
                                            break;
                                        case "longitude":
                                            receiveMessage.longitude = rd.nextDouble();
                                            break;
                                        case "seqnum":
                                            receiveMessage.seqNum = rd.nextInt();
                                            seqNum = receiveMessage.seqNum;
                                            Log.i("Seq", String.valueOf(seqNum));
                                            break;
                                        case "sender":
                                            receiveMessage.sender = rd.nextString();
                                            break;
                                        case "text":
                                            receiveMessage.messageText = rd.nextString();
                                            break;
                                    }

                                }
                                rd.endObject();

                                ContentValues values = new ContentValues();
                                receiveMessage.writeToProvider(values);
                                RequestManager messageManager = new RequestManager(context);

                                messageManager.persist(receiveMessage);
                                //context.getContentResolver().insert(MessageContract.CONTENT_URI, values);


                            }

                            rd.endArray();
                        }
                    }
                    rd.endObject();
            requestManager.updateSeqNum(request.senderId, seqNum);
            return response.getResponse();
        } catch (IOException e) {
            return new ErrorResponse(0, ErrorResponse.Status.SERVER_ERROR, e.getMessage());

        } finally {
            if (response != null) {
                response.disconnect();
            }
        }
    }


}
