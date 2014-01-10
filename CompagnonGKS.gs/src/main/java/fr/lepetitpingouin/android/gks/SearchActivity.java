package fr.lepetitpingouin.android.gks;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends ActionBarActivity {

    SharedPreferences prefs;
    String catCode="0", sort="", order="";
    EditText input_search;

    HashMap<String, String> mapCat, mapSort, mapOrder;
    SimpleAdapter adapterCat, adapterSort, adapterOrder;

    ListView listViewCat, listViewSort, listViewOrder;
    ArrayList<HashMap<String, String>> arrayListCat, arrayListSort, arrayListOrder;
    Dialog dialogCat, dialogSort, dialogOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(R.layout.action_search);

        input_search = (EditText)actionBar.getCustomView().findViewById(R.id.editText_search);

        dialogCat = new Dialog(this);
        dialogCat.setContentView(R.layout.listview);
        dialogCat.setTitle("Rechercher dans...");

        listViewCat = (ListView)dialogCat.findViewById(R.id.listview);
        arrayListCat = new ArrayList<HashMap<String, String>>();

        listViewCat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) listViewCat.getItemAtPosition(position);

                catCode = map.get("code");
                TextView tv = (TextView) findViewById(R.id.dropdown_category);
                tv.setText(map.get("nom"));

                dialogCat.dismiss();
            }
        });


        dialogSort = new Dialog(this);
        dialogSort.setContentView(R.layout.listview);
        dialogSort.setTitle("Classer par...");

        listViewSort = (ListView)dialogSort.findViewById(R.id.listview);
        arrayListSort = new ArrayList<HashMap<String, String>>();

        listViewSort.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) listViewSort.getItemAtPosition(position);

                sort = map.get("code");
                TextView tv = (TextView) findViewById(R.id.dropdown_sort);
                tv.setText(map.get("nom"));

                dialogSort.dismiss();
            }
        });

        dialogOrder = new Dialog(this);
        dialogOrder.setContentView(R.layout.listview);
        dialogOrder.setTitle("Classer par...");

        listViewOrder = (ListView)dialogOrder.findViewById(R.id.listview);
        arrayListOrder = new ArrayList<HashMap<String, String>>();

        listViewOrder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) listViewOrder.getItemAtPosition(position);

                order = map.get("code");
                TextView tv = (TextView) findViewById(R.id.dropdown_order);
                tv.setText(map.get("nom"));

                dialogOrder.dismiss();
            }
        });

        mapOrder = new HashMap<String, String>();
        mapOrder.put("nom", "Croissant");
        mapOrder.put("code", "ASC");
        mapOrder.put("icon", String.valueOf(R.drawable.ic_filter_asc));
        arrayListOrder.add(mapOrder);

        mapOrder = new HashMap<String, String>();
        mapOrder.put("nom", "Décroissant");
        mapOrder.put("code", "DESC");
        mapOrder.put("icon", String.valueOf(R.drawable.ic_filter_desc));
        arrayListOrder.add(mapOrder);

        adapterOrder = new SimpleAdapter(
                getBaseContext(),
                arrayListOrder,
                R.layout.item_list,
                new String[]{"nom", "icon"},
                new int[]{R.id.listItem, R.id.listItemIcon}
        );

        listViewOrder.setAdapter(adapterOrder);

        listViewSort.setAdapter(adapterSort);

        new asyncSearchOptionsFetcher().execute();

    }

    public void dummyFunction(View v) {
        Toast.makeText(getApplicationContext(), "Ce bouton ne sert à rien.", Toast.LENGTH_SHORT).show();
    }

    public void onCategory(View v) {
        dialogCat.show();
    }

    public void onSort(View v) {
        dialogSort.show();
    }

    public void onOrder(View v) {
        dialogOrder.show();
    }

    public void onSearch(View v) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), torrentsActivity.class);
        intent.putExtra("url", Default.URL_SEARCH);
        intent.putExtra("keywords", input_search.getText().toString());
        intent.putExtra("order", order);
        intent.putExtra("catCode", catCode);
        intent.putExtra("sort", sort);
        intent.putExtra("subtitle", ((TextView)findViewById(R.id.dropdown_sort)).getText().toString().toLowerCase() + ", " + ((TextView)findViewById(R.id.dropdown_order)).getText().toString().toLowerCase());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_search:
                onSearch(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private class asyncSearchOptionsFetcher extends AsyncTask<String, String, String> {

        Document doc;
        Connection.Response res;
        Elements categories, sorts;
        ProgressBar progress;

        @Override
        protected void onPreExecute() {
            //animer les categories
            progress = (ProgressBar)findViewById(R.id.progress_category);
            progress.setVisibility(View.VISIBLE);
            progress.setIndeterminate(true);
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                Map<String, String> cookies = Jsoup.connect(Default.URL_LOGIN)
                        .data("username", prefs.getString("account_username", ""), "password", prefs.getString("account_password", ""))
                        .header("Content-Type","application/x-www-form-urlencoded;charset=UTF-8")
                        .followRedirects(true)
                        .referrer(Default.URL_LOGIN)
                        .userAgent("Mozilla")
                        .method(Connection.Method.POST)
                        .execute().cookies();

                res = Jsoup.connect(Default.URL_SEARCH)
                        .followRedirects(true)
                        .cookies(cookies)
                        .execute();

                doc = res.parse();

                categories = doc.select("select.catfine").first().select("option");
                sorts = doc.select("select[name=sort]").first().select("option");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String value) {

            try {
                for(Element category : categories) {
                    mapCat = new HashMap<String, String>();
                    mapCat.put("nom", category.text());
                    mapCat.put("code", category.attr("value"));
                    mapCat.put("icon", String.valueOf(new TorrentCategorie().getIcon(category.attr("value"))));
                    arrayListCat.add(mapCat);
                }

                adapterCat = new SimpleAdapter(
                        getBaseContext(),
                        arrayListCat,
                        R.layout.item_list,
                        new String[]{"nom", "icon", "code"},
                        new int[]{R.id.listItem, R.id.listItemIcon, R.id.listItemCode}
                );

                listViewCat.setAdapter(adapterCat);


                mapSort = new HashMap<String, String>();
                mapSort.put("nom", "Aucun tri");
                mapSort.put("code", "");
                mapSort.put("icon", String.valueOf(R.drawable.ic_filter_filter));
                arrayListSort.add(mapSort);

                for(Element sort : sorts) {
                    mapSort = new HashMap<String, String>();
                    mapSort.put("nom", sort.text());
                    mapSort.put("code", sort.attr("value"));
                    mapSort.put("icon", String.valueOf(R.drawable.ic_filter_filter));
                    arrayListSort.add(mapSort);
                }

                adapterSort = new SimpleAdapter(
                        getBaseContext(),
                        arrayListSort,
                        R.layout.item_list,
                        new String[]{"nom", "icon"},
                        new int[]{R.id.listItem, R.id.listItemIcon}
                );

                listViewSort.setAdapter(adapterSort);
            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Impossible de contacter le serveur.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            finally {
                progress.setVisibility(View.INVISIBLE);
            }
        }
    }
}
