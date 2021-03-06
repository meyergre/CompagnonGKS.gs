package fr.lepetitpingouin.android.gks;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Torrent {

    public String name, id, url;
    private Context context;
    private SharedPreferences prefs;
    private torrentFileGetter tDL;
    private AsyncDlLater dll;
    private AsyncDlLaterNot dllNot;
    private AsyncAddToAutoGet autoget;
    private AsyncDelAutoget autogetNot;
    public Handler handler;

    public Torrent(Context context, String name, String id) {
        this.context = context.getApplicationContext();
        this.name = name;
        this.id = id;
        this.url = id;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        handler = new Handler();
    }

    public void download() {
        tDL = new torrentFileGetter();
        try {
            tDL.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void autoget() {
        autoget = new AsyncAddToAutoGet();
        try {
            autoget.execute();
        } catch (Exception e) {
        }
    }

    public void unautoget(String id) {
        autogetNot = new AsyncDelAutoget();
        try {
            autogetNot.execute(id);
        } catch (Exception e) {
        }
    }

    public void bookmark() {
        dll = new AsyncDlLater();
        try {
            dll.execute();
        } catch (Exception e) {
        }
    }

    public void unbookmark(String id) {
        dllNot = new AsyncDlLaterNot();
        try {
            dllNot.execute(id);
        } catch (Exception e) {
        }
    }

    public void share() {

        String url = Default.URL_TORRENT_SHOW + this.id + "/";
        String signature = "\n\n-\n" + "envoyé depuis l'application Compagnon GKS";

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, url + signature);
        share.putExtra(Intent.EXTRA_SUBJECT, "[GKS.gs] " + this.name);

        context.startActivity(Intent.createChooser(share, "Partager...").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public void doNotify(int icon, String title, String subtitle, int id, PendingIntent pendingIntent) {
        try {
            if (pendingIntent == null)
                pendingIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(icon)
                            .setContentTitle(title)
                            .setContentText(subtitle);
            //TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            //stackBuilder.addParentStack(Torrent.class);
            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setAutoCancel(true);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(id, mBuilder.build());

            /*
            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            final Notification notification = new Notification(icon, title, System.currentTimeMillis());
            final String notificationTitle = title;
            final String notificationDesc = subtitle;
            notification.setLatestEventInfo(context, notificationTitle, notificationDesc, pendingIntent);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(id, notification);*/
        } finally {
            try {
                Toast.makeText(this.context, subtitle, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
            }
        }
    }

    public void cancelNotify(int id) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(id);
    }

    private String AddOrDelGet(String action, String tid, final String what) {
        // action : add | del
        // tid : id
        // what : type
        final String _what = what.contains("book") ? "Bookmarks" : "Autoget";
        final String _action = action.contains("add") ? "Ajouté à" : "Supprimé de";
        String ajax = Default.URL_INDEX + "/ajax.php?action=" + action + "&type=" + what + "&tid=" + tid;
        try {
            String username = prefs.getString("account_username", ""), password = prefs.getString("account_password", "");
            SuperGKSHttpBrowser browser = new SuperGKSHttpBrowser(context);

            String html = browser.login(username, password)
                .connect(ajax)
                .executeInAsyncTask();

            Log.e("html", html);

            final Integer returnCode = browser.getResponseCode();
            Log.e("retCode", returnCode+"");

            if (returnCode == 302 || returnCode == 200) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, _action + " la liste " + _what + "!", Toast.LENGTH_LONG).show();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private class AsyncAddToAutoGet extends AsyncTask<Void, String[], Void> {

        @Override
        protected Void doInBackground(Void... params) {
            AddOrDelGet("add", id, "autoget");
            //booktorrent
            return null;
        }
    }

    private class AsyncDelAutoget extends AsyncTask<String, String[], String> {
        @Override
        protected String doInBackground(String... params) {

            String username = prefs.getString("account_username", ""), password = prefs.getString("account_password", "");
            SuperGKSHttpBrowser browser = new SuperGKSHttpBrowser(context);

            String html = browser.login(username, password)
                    .connect("https://gks.gs/autoget/delete&id=" + params[0])
                    .executeInAsyncTask();
            //AddOrDelGet("del", params[0], "autoget");
            if (browser.getResponseCode() == 302 || browser.getResponseCode() == 200)
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Supprimé de la liste Autoget!", Toast.LENGTH_LONG).show();
                    }
                });
            return null;
        }
    }

    private class AsyncDlLaterNot extends AsyncTask<String, String[], String> {
        @Override
        protected String doInBackground(String... params) {
            AddOrDelGet("del", params[0], "delbookmark");
            return null;
        }
    }

    private class AsyncDlLater extends AsyncTask<Void, String[], Void> {
    // bookmark
        String msg;

        @Override
        protected Void doInBackground(Void... arg0) {
            AddOrDelGet("add", id, "booktorrent");
            return null;
        }


    }

    private class torrentFileGetter extends AsyncTask<Void, String[], Void> {

        Connection.Response resTorrent;
        byte[] torrentFileContent;

        @Override
        protected void onPreExecute() {
            doNotify(R.drawable.ic_notif_dl_start, name, "Téléchargement...", Integer.valueOf(id), null);
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String username = prefs.getString("account_username", ""), password = prefs.getString("account_password", "");

            try {
                resTorrent = Jsoup.connect(Default.URL_TORRENT_GET + id + "/")
                        .data("username", username, "password", password)
                        .method(Connection.Method.POST)
                        .maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        //.timeout(prefs.getInt("timeoutValue", Default.timeout) * 1000)
                        .cookies(Jsoup
                                .connect(Default.URL_LOGIN)
                                .data("username", username, "password", password)
                                .method(Connection.Method.POST)
                                .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                                //.timeout(prefs.getInt("timeoutValue", Default.timeout) * 1000)
                                .maxBodySize(0)
                                .followRedirects(true)
                                .ignoreContentType(true)
                                .execute().cookies())
                        .execute();

                Log.e("TORRENT ROTTENT",resTorrent.body());

                torrentFileContent = resTorrent.bodyAsBytes();
                Log.e("torrentFileContent's length", torrentFileContent.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {


            String path = prefs.getString("filePicker", Environment.getExternalStorageDirectory().getPath());

            File file = new File(path, name.replaceAll("/", "_") + ".torrent");
            file.setWritable(true, false);

            Log.e("PATH", file.getAbsolutePath());
            Log.e("PATH", Environment.getExternalStorageDirectory().getPath());

            try { file.createNewFile(); } catch (Exception e) {e.printStackTrace();}
            try {

                FileOutputStream fo = new FileOutputStream(file);
                fo.write(torrentFileContent);
                fo.close();

                Intent i = new Intent();
                i.setAction(Intent.ACTION_VIEW);
                //i.setDataAndType(Uri.fromFile(file), MimeTypeMap.getSingleton().getMimeTypeFromExtension("torrent"));
                if(prefs.getBoolean("addMimeType", false))
                    i.setDataAndType(Uri.fromFile(file), "application/x-bittorrent");
                else //auto-detect
                    //i.setDataAndType(Uri.fromFile(file), MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.getName().substring(file.getName().lastIndexOf(".")+1)));
                    i.setData(Uri.fromFile(file));

                PendingIntent pI = PendingIntent.getActivity(context, 0, i, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_UPDATE_CURRENT);
                doNotify(R.drawable.ic_notif_dl_done, name, "Téléchargement terminé !", Integer.valueOf(id), pI);
                if(prefs.getBoolean("openAfterDl", false)) {
                    //ouvrir le fichier
                    try {
                        context.getApplicationContext().startActivity(i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_UPDATE_CURRENT|Intent.FLAG_FROM_BACKGROUND));
                        if(prefs.getBoolean("openAfterDlCancelNotify", false))
                        cancelNotify(Integer.valueOf(id));
                    } catch(Exception e) {
                        doNotify(R.drawable.ic_notif_dl_failed, name, "Erreur d'ouverture du torrent\nAucune application trouvée.", Integer.valueOf(id), null);
                    }
                }

            } catch (IOException e) {
                Intent i = new Intent();
                i.setClass(context, UserSettingsActivity.class);
                PendingIntent pI = PendingIntent.getActivity(context, 0, i, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_UPDATE_CURRENT);
                doNotify(R.drawable.ic_notif_dl_failed, name, "Le téléchargement a échoué...\nAccès au répertoire choisi impossible.", Integer.valueOf(id), pI);
                e.printStackTrace();
            } catch (Exception e) {
                Intent i = new Intent();
                i.setClass(context, UserSettingsActivity.class);
                PendingIntent pI = PendingIntent.getActivity(context, 0, i, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_UPDATE_CURRENT);
                if (file.exists() && file.length() == 0) {
                    doNotify(R.drawable.ic_notif_dl_failed, name, "Le téléchargement a échoué...\nErreur réseau : impossible de télécharger le contenu du fichier. Veuillez réessayer.", Integer.valueOf(id), pI);
                    e.printStackTrace();
                } else if (!file.exists()) {
                    doNotify(R.drawable.ic_notif_dl_failed, name, "Le téléchargement a échoué...\nImpossible de créer le fichier.", Integer.valueOf(id), pI);
                } else {
                    doNotify(R.drawable.ic_notif_dl_failed, name, "Le téléchargement a échoué...\nErreur inconnue.", Integer.valueOf(id), pI);
                }
            }
        }
    }
}
