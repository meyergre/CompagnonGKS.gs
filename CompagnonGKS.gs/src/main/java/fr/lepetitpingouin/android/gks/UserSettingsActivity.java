package fr.lepetitpingouin.android.gks;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by gregory on 15/10/13.
 */
public class UserSettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}