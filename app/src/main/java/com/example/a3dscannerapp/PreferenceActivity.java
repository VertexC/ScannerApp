package com.example.a3dscannerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

public class PreferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new MySettingsFragment())
                .commit();

    }

    public static class MySettingsFragment extends PreferenceFragmentCompat {

        SharedPreferences.OnSharedPreferenceChangeListener mPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener(){
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if(key.equals("uploadUrl")) {
                    String url = prefs.getString("uploadUrl", "");
                    mUploadUrlPreference.setSummary(url);
                }
            }
        };

        EditTextPreference mUploadUrlPreference;
        Preference mFeedbackPreference;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            Context context = getPreferenceManager().getContext();
            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

            SharedPreferences preferences = getPreferenceManager().getSharedPreferences();

            mUploadUrlPreference = new EditTextPreference(context);
            String url = preferences.getString("uploadUrl", "");
            mUploadUrlPreference.setKey("uploadUrl");
            mUploadUrlPreference.setTitle("Full Url for file upload");
            mUploadUrlPreference.setSummary(url);


            mFeedbackPreference = new Preference(context);
            mFeedbackPreference.setKey("feedback");
            mFeedbackPreference.setTitle("Send feedback");
            mFeedbackPreference.setSummary("Report technical issues or suggest new features");

            screen.addPreference(mUploadUrlPreference);
            screen.addPreference(mFeedbackPreference);

            setPreferenceScreen(screen);
        }

        @Override
        public void onResume(){
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(mPrefListener);
        }

        @Override
        public void onPause(){
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(mPrefListener);
        }

    }

}



