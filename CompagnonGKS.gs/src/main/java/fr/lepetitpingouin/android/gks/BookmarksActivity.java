package fr.lepetitpingouin.android.gks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gregory on 20/01/2014.
 */
public class BookmarksActivity extends ActionBarActivity {

    SharedPreferences prefs;

    GridView listView;

    ArrayList<HashMap<String, String>> listItems;
    HashMap<String, String> listItem;

    SimpleAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_bookmark);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        listView = (GridView) findViewById(R.id.bmGridView);

        listItems = new ArrayList<HashMap<String, String>>();

        String display = getIntent().getStringExtra("display");

        if (display.equals("bookmarks")) {
            loadBookmarks();
        } else if (display.equals("autoget")) {
            loadAutoget();
        } else finish();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) listView.getItemAtPosition(position);
                Intent i;
                i = new Intent();
                i.setClass(getApplicationContext(), torrentDetailsActivity.class);
                i.putExtra("url", Default.URL_TORRENT_SHOW + map.get("id") + "/");
                i.putExtra("nom", map.get("title"));
                i.putExtra("ID", map.get("id"));
                i.putExtra("icon", R.drawable.ic_launcher);
                startActivity(i);
            }
        });
    }

    public void loadBookmarks() {
        getSupportActionBar().setTitle("Mes Bookmarks");
        new asyncGetBookmarks().execute();
    }

    public void loadAutoget() {
        getSupportActionBar().setTitle("Mon AutoGet");
    }


    private class asyncGetBookmarks extends AsyncTask<String, String, String> {

        Document doc;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                try {
                    doc = Jsoup.parse(new SuperGKSHttpBrowser(getApplicationContext())
                            .login(prefs.getString("account_username", ""), prefs.getString("account_password", ""))
                            .connect(Default.URL_BOOKMARKS)
                            .executeInAsyncTask());

                    Log.e("html", doc.html());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Elements bookmarks = doc.select("#bookmark_list tr:has(td.name)");

                for (Element bookmark : bookmarks) {
                    Log.e("torrent", "found");
                    Log.e("title", bookmark.select(".name a").last().text());
                    Log.e("id", bookmark.select(".name a").last().attr("href").replaceAll("^/.*/(.*)/.*", "$1"));
                    Log.e("id_del", bookmark.attr("id").replaceAll("\\D(\\d*)", "$1"));

                    listItem = new HashMap<String, String>();
                    listItem.put("title", bookmark.select(".name a").last().text());
                    listItem.put("id", bookmark.select(".name a").last().attr("href").replaceAll("^/.*/(.*)/.*", "$1"));
                    listItem.put("id_del", bookmark.attr("id").replaceAll("\\D(\\d*)", "$1"));
                    listItems.add(listItem);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String value) {
            try {
                adapter = new SimpleAdapter(
                        getBaseContext(), listItems,
                        R.layout.item_bookmark,
                        new String[]{"title"},
                        new int[]{R.id.bmNom}
                );

                listView.setAdapter(adapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
            setProgressBarIndeterminateVisibility(false);
        }
    }

    private class asyncGetAutoget extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            return null;
        }
    }
}
