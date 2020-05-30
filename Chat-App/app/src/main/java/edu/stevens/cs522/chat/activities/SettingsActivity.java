package edu.stevens.cs522.chat.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import edu.stevens.cs522.chat.R;

/**
 * Created by dduggan.
 */

public class SettingsActivity extends AppCompatActivity {

    static Context context;
    public static class SettingsFragment extends PreferenceFragmentCompat {



        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String key) {
            setPreferencesFromResource(R.xml.settings, key);
        }

        @Override
        public void onResume() {
            super.onResume();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            EditTextPreference AppId =  (EditTextPreference) findPreference("app-id");
            AppId.setSummary(prefs.getString("app-id","000"));
            EditTextPreference Username =  (EditTextPreference) findPreference("user-name");
            Username.setSummary(prefs.getString("user-name","Tester"));
            EditTextPreference SenderId =  (EditTextPreference) findPreference("sender-id");
            Long sendId = prefs.getLong("sender-id", -1);
            SenderId.setSummary(sendId.toString());

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the messages content.
        context = getApplicationContext();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }


}