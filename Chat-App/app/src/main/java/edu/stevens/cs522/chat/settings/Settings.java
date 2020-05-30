package edu.stevens.cs522.chat.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.UUID;

import edu.stevens.cs522.chat.R;

/**
 * Created by dduggan.
 */

public class Settings {

    public static final String SETTINGS = "settings";

    public static final boolean SYNC = true;

    private static String APPID_KEY = "app-id";

    private static String SENDER_ID_KEY = "sender-id";

    private static final String CHAT_NAME_KEY = "user-name";

    public static UUID getAppId(Context context) {
        SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String appID = prefs.getString(APPID_KEY, null);
        if (appID == null) {
            appID = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(APPID_KEY, appID);
            String chatName = prefs.getString(CHAT_NAME_KEY, null);
            if (chatName == null) {
                editor.putString(CHAT_NAME_KEY, context.getString(R.string.user_name_default));
            }
            editor.commit();
        }
        return UUID.fromString(appID);
    }

    public static long getSenderId(Context context) {
        SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return (prefs.getLong(SENDER_ID_KEY, -1));
    }

    public static void saveSenderId(Context context, long senderId) {
        SharedPreferences.Editor editor = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putLong(SENDER_ID_KEY, senderId);
        editor.commit();
    }

    public static String getChatName(Context context) {
        SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(CHAT_NAME_KEY, null);
    }

    public static void saveChatName(Context context, String chatName) {
        SharedPreferences.Editor editor = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(CHAT_NAME_KEY, chatName);
        editor.commit();
    }

    public static boolean isRegistered(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(SENDER_ID_KEY, -1) >= 0;
    }

}