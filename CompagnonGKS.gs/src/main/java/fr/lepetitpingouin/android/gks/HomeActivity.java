package fr.lepetitpingouin.android.gks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends ActionBarActivity {

    TextView tx_ratio, tx_upload, tx_download, tx_username, tx_karma, tx_classe, tx_twit, tx_hitrun, tx_mails, tx_aura;
    Animation animation;
    ImageView refreshBtn, iv_Avatar;

    String code;

    SharedPreferences prefs;

    BroadcastReceiver mReceiver;

    ProgressBar updateBar;

    WebView www;

    private DrawerLayout menuDrawer;
    ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onResume() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getString("account_username", "").equals("")) {
            finish();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(this.mReceiver);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        getSupportActionBar().setTitle("Compagnon GKS");
        getSupportActionBar().setSubtitle("...");

        menuDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, menuDrawer, R.drawable.ic_menubg, 0, 0) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                ActivityCompat.invalidateOptionsMenu(HomeActivity.this); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                ActivityCompat.invalidateOptionsMenu(HomeActivity.this); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        menuDrawer.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        updateBar = (ProgressBar) findViewById(R.id.updateProgressBar);
        updateBar.setVisibility(View.INVISIBLE);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getString("account_username", "").equals("")) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Default.Intent_endUpdate);
        intentFilter.addAction(Default.Intent_startUpdate);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Default.Intent_startUpdate)) {
                    //refreshBtn.startAnimation(animation);
                    setSupportProgressBarIndeterminateVisibility(true);
                    getSupportActionBar().setSubtitle("Mise \u00e0 jour...");
                    updateBar.setVisibility(View.VISIBLE);
                } else if (intent.getAction().equals(Default.Intent_endUpdate)) {
                    //refreshBtn.clearAnimation();
                    setSupportProgressBarIndeterminateVisibility(false);
                    updateBar.setVisibility(View.INVISIBLE);
                    updateValues();
                }
            }
        };
        //registering our receiver
        this.registerReceiver(mReceiver, intentFilter);

        //refreshBtn = (ImageView)findViewById(R.id.ivRefresh);

        //animation = AnimationUtils.loadAnimation(this, R.anim.rotate);

        tx_ratio = (TextView) findViewById(R.id.tx_ratio);
        tx_download = (TextView) findViewById(R.id.tx_download);
        tx_upload = (TextView) findViewById(R.id.tx_upload);
        tx_karma = (TextView) findViewById(R.id.tx_karma);
        tx_username = (TextView) findViewById(R.id.tx_username);
        tx_classe = (TextView) findViewById(R.id.tx_classe);
        iv_Avatar = (ImageView) findViewById(R.id.ivAvatar);
        tx_mails = (TextView) findViewById(R.id.tx_mails);
        tx_aura = (TextView) findViewById(R.id.tx_aura);
        tx_twit = (TextView) findViewById(R.id.tx_twit);
        tx_hitrun = (TextView) findViewById(R.id.tx_hitrun);
        www = (WebView) findViewById(R.id.www);
        www.getSettings().setJavaScriptEnabled(true);
        www.setWebViewClient(new WebViewClient());
        www.setWebChromeClient(new WebChromeClient());
        www.setBackgroundColor(0x00000000);

        updateValues();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void onRefresh(View v) {
        startService(new Intent(getApplicationContext(), gksUpdater.class));
    }

    public void onSearch(View v) {
        startActivity(new Intent(getApplicationContext(), SearchActivity.class));
    }

    public void onAboutClick(View v) {
        startActivity(new Intent(getApplicationContext(), AboutActivity.class));
    }

    public void updateValues() {
        tx_ratio.setText(prefs.getString("ratio", "??"));
        tx_karma.setText(prefs.getString("karma", "0"));
        tx_upload.setText(prefs.getString("upload", ""));
        tx_download.setText(prefs.getString("download", ""));
        tx_classe.setText(prefs.getString("classe", ""));
        tx_username.setText(prefs.getString("username", ""));
        getSupportActionBar().setSubtitle(prefs.getString("lastUpdate", ""));

        //mails, aura, txit, hirtun
        tx_mails.setText(prefs.getString("mails", ""));
        tx_aura.setText(prefs.getString("aura", ""));
        tx_twit.setText(prefs.getString("twit", ""));
        tx_hitrun.setText(prefs.getString("hitrun", ""));

        www.getSettings().setJavaScriptEnabled(true);
        www.setWebViewClient(new WebViewClient());
        www.setWebChromeClient(new WebChromeClient());

        //www.loadUrl("about:blank");
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        code = "<body><script src=\"file:///android_asset/amstock.js\" type=\"text/javascript\"></script><script>" + prefs.getString("chart", "") + "</script><div id=\"chartratio\" style=\"width: 100%; height: 100%;\"></div></body>";
        code = code.replace("Upload (Go)", "Upload").replace("Download (Go)", "Download").replace("Aura par jour", "Aura/jour").replace("chart.startDuration = 1;", "chart.startDuration = 0;");
        www.loadDataWithBaseURL(null, code, "text/html", "utf-8", null);
        www.setBackgroundColor(0x00000000);


        String encodedImage = prefs.getString("avatar", "");
        if (!encodedImage.equalsIgnoreCase("")) {
            try {
                byte[] b = Base64.decode(encodedImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
                iv_Avatar.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void dummyFunction(View v) {
        Toast.makeText(getApplicationContext(), "Ce bouton ne sert à rien.", Toast.LENGTH_SHORT).show();
    }

    public void onMessages(View v) {
        Toast.makeText(getApplicationContext(), "Ce bouton ne sert à rien.", Toast.LENGTH_SHORT).show();
    }

    public void onBookmarks(View v) {
        //Toast.makeText(getApplicationContext(), "Ce bouton ne sert à rien.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(), BookmarksActivity.class).putExtra("display", "bookmarks"));
    }

    public void onAutoGet(View v) {
        //Toast.makeText(getApplicationContext(), "Ce bouton ne sert à rien.", Toast.LENGTH_SHORT).show();
    }

    public void onPreferencesClick(View v) {
        startActivity(new Intent(getApplicationContext(), UserSettingsActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_update:
                onRefresh(getCurrentFocus());
                return true;
            case R.id.action_search:
                onSearch(getCurrentFocus());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
