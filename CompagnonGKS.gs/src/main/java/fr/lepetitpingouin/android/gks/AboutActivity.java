package fr.lepetitpingouin.android.gks;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        String version = "????";
        PackageInfo pInfo;

        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView version_text = (TextView)findViewById(R.id.about_version);
        version_text.setText(version);

    }

    public void onOtherAppsPress(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(Default.URL_MYAPPS));
        startActivity(i);
    }

    public void onChangeLogPress(View v) {
        String chlog = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("Changelog.txt"), "UTF-8"));
            String mLine = reader.readLine();
            while (mLine != null) {
                chlog += mLine + "\n";
                mLine = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
        }
        AlertDialog alertDialog = alertDialog = new AlertDialog.Builder(this).setTitle("Changelog").setMessage(chlog).setIcon(R.drawable.ic_titlebar).show();
        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
        textView.setTextSize(10);
        textView.setTypeface(Typeface.MONOSPACE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_donate:
                startActivity(new Intent(getApplicationContext(), DonateActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
