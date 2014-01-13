package fr.lepetitpingouin.android.gks;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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

public class torrentsActivity extends ActionBarActivity {
    String connectUrl, searchTerms;

    String id;
    String catCode="";
    String[] cat_select_id, cat_select_name;

    HashMap<String, String> itemMmap;

    public ProgressDialog dialog;

    SharedPreferences prefs;
    Handler handler;

    String order, type, exact, prez;

    torrentFetcher mF;

    HashMap<String, String> map;

    ImageButton prev, next;
    LinearLayout dropdown_pages, navbar, dropdown_categories;

    String strNext, strPrev, paginator, pageName;

    Dialog pages_dialog, cat_dialog;

    GridView maListViewPerso;
    ListView PagesList, catList;
    ArrayList<HashMap<String, String>> listItem, pageListItem, catListItem;

    SimpleAdapter mSchedule, mTorrentsAdapter;

    @Override
    public void onDestroy() {
        mF = null;
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torrentslist);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getActiveNetworkInfo();
        if (!(netInfo != null && netInfo.isConnectedOrConnecting())) {
            Intent i = new Intent(Default.Intent_endUpdate);
            sendBroadcast(i);

            Toast.makeText(getApplicationContext(), "Pas de connexion", Toast.LENGTH_SHORT).show();
            finish();
        }

        handler = new Handler();

        connectUrl = getIntent().getStringExtra("url");
        searchTerms = getIntent().getStringExtra("keywords");
        catCode = getIntent().getStringExtra("catCode");

        exact = getIntent().getStringExtra("exact");
        prez = getIntent().getStringExtra("prez");

        try {
            String subTitle = getIntent().getStringExtra("subtitle");
            getSupportActionBar().setSubtitle(subTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }

        order = getIntent().getStringExtra("order");
        type = getIntent().getStringExtra("category");

        getSupportActionBar().setTitle(searchTerms);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        navbar = (LinearLayout) findViewById(R.id.navbar);
        navbar.setVisibility(View.GONE);

        dropdown_categories = (LinearLayout) findViewById(R.id.category_filter);
        dropdown_categories.setVisibility(getIntent().getStringExtra("catCode").equals("0") ? View.VISIBLE : View.GONE);

        // Hide for now...
        dropdown_categories.setVisibility(View.GONE);

        dropdown_categories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cat_dialog.show();
            }
        });

        cat_dialog = new Dialog(this);
        cat_dialog.setContentView(R.layout.dialog_listview);
        cat_dialog.setTitle("Filtrer...");

        catList = (ListView) cat_dialog.findViewById(R.id.dialoglistview);
        catListItem = new ArrayList<HashMap<String, String>>();

        catList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) catList
                        .getItemAtPosition(position);

                TextView tv = (TextView) findViewById(R.id.tvCatListFilter);
                tv.setText(map.get("name"));
                ImageView iv = (ImageView) findViewById(R.id.ivCatListFilter);
                iv.setImageResource(Integer.valueOf(map.get("icon")));
                cat_dialog.dismiss();
                switchList(map.get("code"));
            }
        });

        pages_dialog = new Dialog(this);
        pages_dialog.setContentView(R.layout.dialog_listview);
        pages_dialog.setTitle("Aller à...");

        PagesList = (ListView) pages_dialog.findViewById(R.id.dialoglistview);
        pageListItem = new ArrayList<HashMap<String, String>>();

        TextView tv = (TextView) findViewById(R.id.navbar_pagesText);
        tv.setText("1");

        PagesList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) PagesList
                        .getItemAtPosition(position);

                TextView tv = (TextView) findViewById(R.id.navbar_pagesText);
                tv.setText(map.get("name"));
                paginator = map.get("code");
                pages_dialog.dismiss();
                update();
            }
        });

        prev = (ImageButton) findViewById(R.id.navbtn_prev);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paginator = strPrev;
                update();
            }
        });

        next = (ImageButton) findViewById(R.id.navbtn_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paginator = strNext;
                update();
            }
        });

        dropdown_pages = (LinearLayout) findViewById(R.id.navbtn_list);
        dropdown_pages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pages_dialog.show();
            }
        });

        prev.setVisibility(View.INVISIBLE);
        next.setVisibility(View.INVISIBLE);

        maListViewPerso = (GridView) findViewById(R.id.malistviewperso);
        listItem = new ArrayList<HashMap<String, String>>();

        id = getIntent().getStringExtra("id");

        maListViewPerso.setOnItemClickListener(new OnItemClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> a, View v, int position,
                                    long id) {
                HashMap<String, String> map = (HashMap<String, String>) maListViewPerso.getItemAtPosition(position);

                Intent i;
                i = new Intent();
                i.setClass(getApplicationContext(), torrentDetailsActivity.class);
                i.putExtra("url", Default.URL_TORRENT_SHOW + map.get("ID") + "/");
                i.putExtra("nom", map.get("nomComplet"));
                i.putExtra("ID", map.get("ID"));
                i.putExtra("icon", Integer.valueOf(map.get("icon")));
                startActivity(i);

            }
        });
        registerForContextMenu(maListViewPerso);

        update();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.malistviewperso) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.torrent_context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        itemMmap = (HashMap<String, String>) maListViewPerso.getItemAtPosition(info.position);

        Torrent torrent = new Torrent(getApplicationContext(), itemMmap.get("nomComplet"), itemMmap.get("ID"));
        switch(item.getItemId()) {

            case R.id.torrent_context_menu_open:

                Intent i;
                i = new Intent();
                i.setClass(getApplicationContext(), torrentDetailsActivity.class);
                i.putExtra("url", Default.URL_TORRENT_SHOW + itemMmap.get("ID") + "/");
                i.putExtra("nom", itemMmap.get("nomComplet"));
                i.putExtra("ID", itemMmap.get("ID"));
                i.putExtra("icon", Integer.valueOf(itemMmap.get("icon")));
                startActivity(i);

                return true;
            case R.id.torrent_context_menu_download:
                torrent.download();
                return true;
            case R.id.torrent_context_menu_share:
                torrent.share();
                return true;
            case R.id.torrent_context_menu_autoget:
                torrent.autoget();
                return true;
            case R.id.torrent_context_menu_bookmark:
                torrent.bookmark();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void switchList(String catCode) {

        mSchedule = mTorrentsAdapter;
        ArrayList<HashMap<String, String>> filterListItem = new ArrayList<HashMap<String, String>>();

        for (int i = 0; i < mSchedule.getCount(); i++) {
            HashMap<String, String> nmap = (HashMap<String, String>) mSchedule.getItem(i);
            if (nmap.get("cat").contains(catCode))
                filterListItem.add(nmap);
        }

        SimpleAdapter mFilterAdapter = new SimpleAdapter(
                torrentsActivity.this.getBaseContext(), filterListItem,
                R.layout.item_torrent,
                new String[]{"share", "nomComplet", "age", "taille", "avis", "seeders", "leechers", "ratio", "completed", "ratioBase", "icon"},
                new int[]{R.id.share, R.id.tNom, R.id.tAge, R.id.tTaille, R.id.tComments, R.id.tSeeders, R.id.tLeechers, R.id.tRatio, R.id.tCompleted, R.id.tRatioBase, R.id.tIcon});

        maListViewPerso.setAdapter(mFilterAdapter);

    }

    public void update() {
        try {
            dialog = ProgressDialog.show(torrentsActivity.this, "GKS.gs", "", true, true);
        } catch(Exception e) {
            e.printStackTrace();
        }
        mF = new torrentFetcher();
        try {
            mF.execute();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Erreur inattendue", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private class torrentFetcher extends AsyncTask<Void, String, Void> {

        Connection.Response res = null;
        Document doc = null;
        int count = 0;

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Chargement de la page...");
            super.onPreExecute();
        }

        @Override
        protected void onCancelled() {
            this.cancel(true);
            super.onCancelled();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            listItem.clear();

            try {

                pageListItem.clear();

                String url = connectUrl;

                url += "?q=" + searchTerms.replaceAll("\\s", "+");

                url += "&order=" + order + "&category=" + catCode + "&page=" + paginator;

                //extras
                url += exact;
                url += prez;

                Log.e("URL", url);

                doc = Jsoup.parse(new SuperGKSHttpBrowser(getApplicationContext())
                        .login(prefs.getString("account_username", ""), prefs.getString("account_password", ""))
                        .connect(url)
                        .executeInAsyncTask());

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                map = new HashMap<String, String>();
                map.put("icon", String.valueOf(R.drawable.ic_cat_gks));
                map.put("name", "-- Tout --");
                map.put("code", "");
                catListItem.add(map);

                publishProgress("");
                count = 0;

                for (Element hCat : doc.select("h3")) {
                    map = new HashMap<String, String>();
                    //map.put("icon", String.valueOf(new CategoryIcon(hCat.nextElementSibling().select("img").first().attr("class").substring(4)).getIcon()));
                    map.put("name", hCat.text());
                    map.put("code", hCat.nextElementSibling().select("img").last().attr("class").substring(4));
                    catListItem.add(map);
                    publishProgress(++count + " ");
                }

                //publishProgress(getString(R.string.fetchingPages));
                count = 0;
                for (Element page : doc.select(".pager_align a:not(:has(img))")) {
                        map = new HashMap<String, String>();
                        map.put("icon", String.valueOf(R.drawable.ic_cat_gks));
                        map.put("name", page.text());
                        map.put("code", page.attr("href").replaceAll("^.*page=(.*)", "$1"));
                        pageListItem.add(map);
                    publishProgress(++count + " pages trouvées");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Element elmtPrev = doc.select(".pager_align a:has(img[alt=Prev])").first();
                strPrev = null;
                strPrev = elmtPrev.attr("href").substring(elmtPrev.attr("href").lastIndexOf("=") + 1);
                prev = (ImageButton) findViewById(R.id.navbtn_prev);
                prev.setVisibility(View.VISIBLE);
            } catch (Exception e){
                e.printStackTrace();
            }

            try {
                Element elmtNext = doc.select(".pager_align a:has(img[alt=Next])").last();
                strNext = null;
                strNext = elmtNext.attr("href").substring(elmtNext.attr("href").lastIndexOf("=") + 1);
                next = (ImageButton) findViewById(R.id.navbtn_next);
                next.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                e.printStackTrace();
            }

            int base = 0;//connectUrl.equals("") ? 1 : 0;

            count = 0;

            String mCatCode;

            for (Element table : doc.select("table#torrent_list tbody")) {
                for (Element row : table.select("tr:not(.head_torrent):nth-child(even)")) {
                    Elements tds = row.select("td");

                    try {
                        publishProgress(++count + " torrents trouvés");
                        mCatCode = tds.get(0).select("a").first().attr("class").replace("categorie-classic cat-", "");
                        map = new HashMap<String, String>();
                        map.put("nomComplet", tds.get(base + 1).select("a").first().attr("title"));
                        map.put("age", row.nextElementSibling().select("p.added").text().replaceAll("^.*.\\s([0-9]{2}.[0-9]{2}.[0-9]{2,4}.*[0-9]{2}.[0-9]{2}).*", "$1"));
                        map.put("taille", new BSize(tds.get(base + 4).text()).convert());

                        String miniIcon = String.valueOf(R.drawable.ic_torrent_null);
                        try {
                            miniIcon = tds.get(1).select("img:not([alt=+])").first().attr("alt")
                                .replace("FreeLeech", String.valueOf(R.drawable.ic_torrent_fl))
                                .replace("Scene", String.valueOf(R.drawable.ic_torrent_scene))
                                .replace("Double Upload", String.valueOf(R.drawable.ic_torrent_x2));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        map.put("miniIcon", miniIcon);

                        Integer share = Integer.valueOf(tds.get(base + 8).select("img").attr("class").replaceAll("^.*-([0-9]+)", "$1"));
                        Integer shareIcon = 0;
                        switch(share) {
                            case 0:
                                shareIcon = R.drawable.share_0;
                                break;
                            case 10:
                                shareIcon = R.drawable.share_10;
                                break;
                            case 20:
                                shareIcon = R.drawable.share_20;
                                break;
                            case 30:
                                shareIcon = R.drawable.share_30;
                                break;
                            case 40:
                                shareIcon = R.drawable.share_40;
                                break;
                            case 50:
                                shareIcon = R.drawable.share_50;
                                break;
                            case 60:
                                shareIcon = R.drawable.share_60;
                                break;
                            case 70:
                                shareIcon = R.drawable.share_70;
                                break;
                            case 80:
                                shareIcon = R.drawable.share_80;
                                break;
                            case 90:
                                shareIcon = R.drawable.share_90;
                                break;
                            case 100:
                                shareIcon = R.drawable.share_100;
                                break;
                            default:
                                shareIcon = R.drawable.share_0;
                                break;
                        }

                        map.put("share", String.valueOf(shareIcon));
                        map.put("avis", tds.get(base + 3).text());
                        map.put("seeders", tds.get(base + 6).text());
                        map.put("leechers", tds.get(base + 7).text());
                        //map.put("uploader", tds.get(base + 1).select("dd > a.profile").text());
                        map.put("ID", tds.get(base + 1).select("a").first().attr("href").replaceAll(".*/(.*)/.*", "$1"));
                        map.put("completed", tds.get(base + 5).text());
                        map.put("cat", mCatCode);
                        double estimatedDl = new BSize(prefs.getString("download", "0.00 Go")).getInMo() + new BSize(tds.get(base + 4).text()).getInMo();
                        String estimatedRatio = String.format("%.2f", (new BSize(prefs.getString("upload", "0.00 Go")).getInMo() / estimatedDl) - 0.01);
                        map.put("ratio", estimatedRatio);
                        map.put("icon", String.valueOf(new TorrentCategorie().getIcon(mCatCode)));
                        map.put("ratioBase", String.format("%.2f", Float.valueOf(prefs.getString("ratio", "0"))));
                        listItem.add(map);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... value) {
            try {
                dialog.setMessage(value[0]);
            } catch(Exception e) {
                e.printStackTrace();
            }

            super.onProgressUpdate();
        }

        @Override
        protected void onPostExecute(Void result) {

            try {
                mSchedule = new SimpleAdapter(
                        getBaseContext(), catListItem,
                        R.layout.item_searchoptions, new String[]{"icon", "name",
                        "code"}, new int[]{R.id.lso_icon,
                        R.id.lso_title, R.id.lso_code});

                catList.setAdapter(mSchedule);

            } catch(Exception e) {
                e.printStackTrace();
            }

            try {

                mSchedule = new SimpleAdapter(
                        getBaseContext(), pageListItem,
                        R.layout.item_searchoptions, new String[]{"icon", "name",
                        "code"}, new int[]{R.id.lso_icon,
                        R.id.lso_title, R.id.lso_code});

                PagesList.setAdapter(mSchedule);
                navbar.setVisibility(PagesList.getCount() > 0 ? View.VISIBLE : View.GONE);

                prev = (ImageButton) findViewById(R.id.navbtn_prev);
                prev.setVisibility(strPrev==null?View.INVISIBLE:View.VISIBLE);

                next = (ImageButton) findViewById(R.id.navbtn_next);
                next.setVisibility(strNext==null?View.INVISIBLE:View.VISIBLE);

            } catch(Exception e) {
                e.printStackTrace();
            }

            try {
                pageName = doc.select(".pager_align strong").first().text();
                TextView tv = (TextView) findViewById(R.id.navbar_pagesText);
                tv.setText(pageName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                mTorrentsAdapter = new SimpleAdapter(
                        torrentsActivity.this.getBaseContext(), listItem,
                        R.layout.item_torrent,
                        new String[]{"miniIcon", "share", "nomComplet", "age", "taille", "avis", "seeders", "leechers", "ratio", "completed", "ratioBase", "icon"},
                        new int[]{R.id.torrent_extra, R.id.share, R.id.tNom, R.id.tAge, R.id.tTaille, R.id.tComments, R.id.tSeeders, R.id.tLeechers, R.id.tRatio, R.id.tCompleted, R.id.tRatioBase, R.id.tIcon});

                maListViewPerso.setAdapter(mTorrentsAdapter);

                if (doc == null) {
                    //Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.no_data_from_server), Toast.LENGTH_LONG).show();
                }

                Handler handler = new Handler();
                if (mTorrentsAdapter.getCount() < 1) {
                    Toast.makeText(getApplicationContext(), "Aucun résultat :(", Toast.LENGTH_LONG).show();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            finish();
                        }
                    }, 1000);
                }
                handler.postDelayed(new Runnable() {
                    public void run() {
                        dialog.cancel();
                    }
                }, 500);
            }
            catch (Exception e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.no_data_from_server), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
