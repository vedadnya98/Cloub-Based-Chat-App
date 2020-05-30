package edu.stevens.cs522.chat.rest;

import android.os.Parcel;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.UUID;

import edu.stevens.cs522.chat.entities.ChatMessage;

/**
 * Created by dduggan.
 */

public class SynchronizeRequest extends Request {

    // Added by request processor
    public long lastSequenceNumber;

    @Override
    public String getRequestEntity() throws IOException {
        // We stream output for SYNC, so this always returns null
        return null;
    }

    @Override
    public Response getResponse(HttpURLConnection connection, JsonReader rd) throws IOException{
        assert rd == null;
        return new SynchronizeResponse(connection);
    }

    @Override
    public Response process(RequestProcessor processor) {
        return processor.perform(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.senderId);
        dest.writeSerializable(this.timestamp);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeString(String.valueOf(appID));
        dest.writeLong(this.lastSequenceNumber);

    }

    public SynchronizeRequest(long senderId, UUID clientID) {
        super(senderId, clientID);
        this.senderId = senderId;
        this.appID = clientID;
    }

    public SynchronizeRequest(long senderId, UUID clientID, ChatMessage message) {
        super(senderId, clientID);
//        this.message = message;
    }

    public SynchronizeRequest(Parcel in) {
        super(in);
        // TODO
        this.senderId = in.readLong();
        this.timestamp = (Date) in.readSerializable();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.appID = UUID.fromString(in.readString());
        this.lastSequenceNumber = in.readLong();
    }

    public static Creator<SynchronizeRequest> CREATOR = new Creator<SynchronizeRequest>() {
        @Override
        public SynchronizeRequest createFromParcel(Parcel source) {
            return new SynchronizeRequest(source);
        }

        @Override
        public SynchronizeRequest[] newArray(int size) {
            return new SynchronizeRequest[size];
        }
    };

}
