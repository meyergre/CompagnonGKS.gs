package fr.lepetitpingouin.android.gks;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;

public class torrentDetailsActivity extends ActionBarActivity {

    SharedPreferences prefs;

    torrentDetailsGetter tG;

    Torrent torrent;


    ProgressDialog dialog;

    WebView details_www;

    String torrent_URL, torrent_ID, torrent_Name;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getActiveNetworkInfo();
        if (!(netInfo != null && netInfo.isConnectedOrConnecting())) {
            Intent i = new Intent(Default.Intent_endUpdate);
            sendBroadcast(i);

            Toast.makeText(getApplicationContext(), "Pas de connexion", Toast.LENGTH_SHORT).show();
            finish();
        }
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.prez, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.prez_context_menu_download:
                torrent.download();
                return false;
            case R.id.prez_context_menu_share:
                torrent.share();
                return false;
            case R.id.prez_context_menu_autoget:
                torrent.autoget();
                return false;
            case R.id.prez_context_menu_bookmark:
                torrent.bookmark();
                return false;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torrentdetails);

        prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        details_www = (WebView) findViewById(R.id.prez);
        details_www.getSettings().setUseWideViewPort(true);
        details_www.getSettings().setLoadWithOverviewMode(true);

        details_www.getSettings().setJavaScriptEnabled(false);

        dialog = ProgressDialog.show(this, "GKS.gs", "Patientez", true, true);
        dialog.setOnCancelListener(new ProgressDialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        });

        getSupportActionBar().setIcon(getIntent().getIntExtra("icon", R.drawable.ic_launcher));

        dialog.show();

        torrent_URL = getIntent().getStringExtra("url");
        torrent_Name = getIntent().getStringExtra("nom");
        torrent_ID = getIntent().getStringExtra("ID");

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            torrent_URL = getIntent().getData().toString();


            String htmlpage = null;
            try {
                htmlpage = new SuperGKSHttpBrowserNoApache(getApplicationContext()).connect(torrent_URL).execute();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            String href = Jsoup.parse(htmlpage).select(".shortlink").attr("href").toString();
            String _id = href.substring(href.lastIndexOf("/") + 1);
            String _title = Jsoup.parse(htmlpage).select("span:has(a.shortlink)").html().replaceAll("<a\\b[^>]+>([^<]*(?:(?!</a)<[^<]*)*)</a>", "").toString();

            torrent_ID = _id;
            torrent_Name = _title;
        }

        getSupportActionBar().setTitle("Détails du torrent");
        getSupportActionBar().setSubtitle(torrent_Name);

        torrent = new Torrent(getApplicationContext(), torrent_Name, torrent_ID);

        tG = new torrentDetailsGetter();
        try {
            tG.execute();
        } catch (Exception e) {
        }
    }

    public void onDownloadClick(View v) {
        torrent.download();
    }

    private class AsyncThx extends AsyncTask<Void, String[], Void> {

        String msg;

        @Override
        protected Void doInBackground(Void... arg0) {
            String username = prefs.getString("login", ""), password = prefs.getString("password", "");

            Document doc;

            try {
                doc = Jsoup.parse(new SuperGKSHttpBrowserNoApache(getApplicationContext())
                        .login(username, password)
                        .connect(torrent_ID)
                        .executeInAsyncTask());

                msg = doc.select(".content ").first().text();

            } catch (Exception e) {
                Log.e("Erreur connect :", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    private class torrentDetailsGetter extends AsyncTask<Void, String[], Void> {

        String prez = "<meta name=\"viewport\" content=\"width=400; user-scalable=no\" />";
        String css = "<style>" +
                "body {width: 100%; overflow: none; margin: 0px; padding: 0px;}" +
                "img {max-width: 400px; max-width: 100%; margin: auto;}" +
                "</style>";

        Document doc = null;

        final String mimeType = "text/html";
        final String encoding = "utf-8";

        @Override
        protected Void doInBackground(Void... arg0) {

            try {

                doc = Jsoup.parse(new SuperGKSHttpBrowser(getApplicationContext())
                        .login(prefs.getString("account_username", ""), prefs.getString("account_password", ""))
                        .connect(torrent_URL)
                        .executeInAsyncTask());

                Log.e("url torrent", torrent_URL);
                prez += css;
                prez += doc.select("#prez").first().html().replaceAll("(on[click|load])", "-$1");
                Log.e("prez torrent", prez);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.e("testCode", doc.outerHtml());

            details_www.loadDataWithBaseURL(null, prez, mimeType, encoding, null);

            TextView tdtTaille = (TextView) findViewById(R.id.tdt_taille);
            tdtTaille.setText(new BSize(doc.select("p:has(.torr-taille)").first().text().replaceAll(".*:\\s(.*)\\s*", "$1")).convert());


            String tdt_seeders = doc.select(".upload").first().text();
            String tdt_leechers = doc.select(".download").first().text();
            String tdt_complets = doc.select(".completed").first().text();

            TextView tdtSeeders, tdtLeechers, tdtComplets;

            tdtSeeders = (TextView) findViewById(R.id.tdt_seeders);
            tdtSeeders.setText(tdt_seeders + " Seeders");

            tdtLeechers = (TextView) findViewById(R.id.tdt_leechers);
            tdtLeechers.setText(tdt_leechers + " Leechers");

            tdtComplets = (TextView) findViewById(R.id.tdt_complets);
            tdtComplets.setText(tdt_complets + " Complets");

            dialog.dismiss();
        }
    }

}
