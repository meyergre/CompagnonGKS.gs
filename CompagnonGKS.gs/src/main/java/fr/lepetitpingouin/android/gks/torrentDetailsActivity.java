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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class torrentDetailsActivity extends ActionBarActivity {

    SharedPreferences prefs;

    torrentDetailsGetter tG;
    AsyncThx thx;

    public String html_filelist = "";


    ProgressDialog dialog;

    WebView details_www;

    ImageView btnShare, btnDownload, btnThx, btnDlLater, rmDlLater, btnList;

    String torrent_URL, torrent_NFO, torrent_ID, torrent_Name;

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

        dialog = ProgressDialog.show(this,
                "GKS.gs",
                "Patientez", true, true);
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

            //test

            String htmlpage = new SuperGKSHttpBrowser(getApplicationContext()).connect(torrent_URL).execute();
            String href = Jsoup.parse(htmlpage).select(".shortlink").attr("href").toString();
            String _id = href.substring(href.lastIndexOf("/")+1);
            String _title = Jsoup.parse(htmlpage).select("span:has(a.shortlink)").html().replaceAll("<a\\b[^>]+>([^<]*(?:(?!</a)<[^<]*)*)</a>", "").toString();

            //test

            //torrent_ID = torrent_URL.split("=")[1];
            torrent_ID = _id;
                    //torrent_Name = torrent_ID;
            torrent_Name = _title;
            //Toast.makeText(getApplicationContext(),torrent_URL, Toast.LENGTH_SHORT).show();
        }

        getSupportActionBar().setTitle("Détails du torrent");
        getSupportActionBar().setSubtitle(torrent_Name);

        tG = new torrentDetailsGetter();
        try {
            tG.execute();
        } catch (Exception e) {
        }
    }

    public void onDownloadClick(View v) {
        Torrent torrent = new Torrent(getApplicationContext(), torrent_Name, torrent_ID);
        torrent.download();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.prez, menu);
        return true;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Torrent torrent = new Torrent(getApplicationContext(), torrent_Name, torrent_ID);

        switch(item.getItemId()) {
            case R.id.prez_context_menu_download:
                torrent.download();
                return true;
            case R.id.prez_context_menu_share:
                torrent.share();
                return true;
            case R.id.prez_context_menu_autoget:
                torrent.autoget();
                return true;
            case R.id.prez_context_menu_bookmark:
                torrent.bookmark();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private class AsyncThx extends AsyncTask<Void, String[], Void> {

        String msg;

        @Override
        protected Void doInBackground(Void... arg0) {
            String username = prefs.getString("login", ""), password = prefs
                    .getString("password", "");

            Connection.Response res = null;
            Document doc = null;

            try {
                /*res = Jsoup
                        .connect(Default.URL_SAY_THANKS + torrent_ID)
                        .data("login", username, "password", password)
                        .method(Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true)
                        .ignoreContentType(true).execute();

                doc = res.parse();*/
                doc = Jsoup.parse(new SuperGKSHttpBrowser(getApplicationContext())
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

                Log.e("url torrent",torrent_URL);
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

            details_www.loadDataWithBaseURL(null, prez, mimeType, encoding, null);

            TextView tdtTaille = (TextView) findViewById(R.id.tdt_taille);
            tdtTaille.setText(new BSize(doc.select("p:has(.torr-taille)").first().text().replaceAll(".*:\\s(.*)\\s*","$1")).convert());

            /*

            try {

                getSupportActionBar().setSubtitle(tduploader.toString());

                tdt_seeders = doc.select(".details table tr td.up").first().text();
                tdt_leechers = doc.select(".details table tr td.down").first().text();
                tdt_note = doc.select("div.accordion div table tr").get(8).select("td").first().text().split(" ", 2)[0];
                note = Double.valueOf(tdt_note.split("/")[0].replace(",", "."));
                tdt_votes = doc.select("div.accordion div table tr").get(8).select("td").first().text().split(" ", 2)[1];
                tdt_complets = doc.select(".details table tr td.down").first().parent().select("td").last().text();
                tdt_taille = doc.select("div.accordion table tr").get(3).select("td").first().text();

                TextView tdtSeeders, tdtLeechers, tdtNote, tdtVotes, tdtComplets, tdtTaille;

                tdtSeeders = (TextView) findViewById(R.id.tdt_seeders);
                tdtSeeders.setText(tdt_seeders + " Seeders");

                tdtLeechers = (TextView) findViewById(R.id.tdt_leechers);
                tdtLeechers.setText(tdt_leechers + " Leechers");

                //tdtNote = (TextView) findViewById(R.id.tdt_note);
                //tdtNote.setText(tdt_note);

                //tdtVotes = (TextView) findViewById(R.id.tdt_votes);
                //tdtVotes.setText(tdt_votes);

                tdtComplets = (TextView) findViewById(R.id.tdt_complets);
                tdtComplets.setText(tdt_complets + " Complets");

                tdtTaille = (TextView) findViewById(R.id.tdt_taille);
                tdtTaille.setText(tdt_taille);

            } catch (Exception e) {
                //details_www.loadDataWithBaseURL("fake://seeJavaDocForExplanation/", "<meta name=\"viewport\" content=\"width=320; user-scalable=no\" />" + doc.select(".block").first().text(), mimeType, encoding, "");
                details_www.loadDataWithBaseURL("fake://seeJavaDocForExplanation/", "<meta name=\"viewport\" content=\"width=320; user-scalable=no\" />" + e.getMessage(), mimeType, encoding, "");
            }
            */

            dialog.dismiss();
        }
    }

}
