package fr.lepetitpingouin.android.gks;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Calendar;
import java.util.Map;

/**
 * Created by gregory on 25/09/13.
 */
public class gksUpdater extends Service {

    SharedPreferences prefs;
    SharedPreferences.Editor edit;

    Intent startUpdateIntent, endUpdateIntent;

    AlarmManager alarmManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startUpdateIntent = new Intent(Default.Intent_startUpdate);
        endUpdateIntent = new Intent(Default.Intent_endUpdate);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        edit = prefs.edit();

        if(prefs.getString("account_username", "")=="") {
            getApplication().startActivity(new Intent(getBaseContext(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
        else {
            new asyncUpdater().execute();
            if(prefs.getBoolean("pref_auto_update", false))
                planRefresh();
        }

        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    public void planRefresh() {
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        int freq = Integer.valueOf(prefs.getString("pref_freq_update", "60"));
        freq = (freq < 1) ? 1 : freq;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, freq);

        // RTC = 1 (AlarmManager.RTC)
        // RTC_WAKEUP = 0 (AlarmManager.RTC_WAKEUP)
        int RTC_mode = (prefs.getBoolean("pref_rtc_wakeup", false)?0:1);
        alarmManager.set(RTC_mode,
        calendar.getTimeInMillis(), PendingIntent.getService(gksUpdater.this, 0, new Intent(gksUpdater.this, gksUpdater.class), PendingIntent.FLAG_CANCEL_CURRENT));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void doNotify(int id, int icon, String title, String message, int flags, boolean onlyAlertOnce) {
        if(!prefs.getBoolean("pref_notif", false)) return;
        NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(getApplicationContext())
        .setSmallIcon(icon)
        .setAutoCancel(true)
        .setDefaults(Notification.DEFAULT_ALL | flags)
        .setContentTitle(title)
        .setOnlyAlertOnce(onlyAlertOnce)
        .setContentText(message);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(getBaseContext(), HomeActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getBaseContext());
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(HomeActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
        stackBuilder.getPendingIntent(
        0,
        PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(id, mBuilder.build());
    }

    private class asyncUpdater extends AsyncTask<String, String, String> {

        Document doc;
        Connection.Response res;
        String ratio="0", karma="0", upload="0.00 MB", download="0.00 MB", username="", classe="", avatar = "", chart="", messages="", aura="", twit="", hitrun="", freeleech_type="";
        Long freeleech = 0L;

        @Override
        protected void onPreExecute() {
            sendBroadcast(startUpdateIntent);
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

                res = Jsoup.connect(Default.URL_PROFILE)
                        .followRedirects(true)
                        .cookies(cookies)
                        .execute();

                doc = res.parse();


                //doc = Jsoup.parse(new SuperGKSHttpBrowser(getApplicationContext()).login(prefs.getString("account_username", ""), prefs.getString("account_password", "")).connect(Default.URL_PROFILE).executeInAsyncTask());

                username= doc.select("#userlink li").first().select("span").first().text();
                ratio   = doc.select("#userlink li").get(2).select("span").first().text();
                upload  = doc.select("span.uploaded").first().text().replace(".",",");
                download= doc.select("span.downloaded").first().text().replace(".", ",");
                karma   = doc.select("span.karma").first().text();
                classe  = doc.select("#userlink li").first().text().split(" \\| ")[1];
                try {
                    messages= doc.select("#new_messages").first().text().split(" ")[0];
                } catch(NullPointerException e) {
                    messages="0";
                }
                aura    = doc.select("#userlink li").get(3).text().split(" \\| +")[0].split(" ")[1];
                twit    = doc.select("#userlink li").get(3).text().split(" \\| +")[1].split(" ")[0];
                hitrun  = doc.select("#userlink li").get(3).text().split(" \\| +")[2].split(" ")[0];

                try {
                    freeleech = Long.valueOf(doc.select(".fl-activated #countdown_fl").attr("data-time"));
                    freeleech_type = doc.select(".fl-activated").text();
                }
                catch (Exception e) {
                    freeleech = 0L;
                }


                try {
                //graphique
                String graphScript_url = doc.select("#contenu script").first().attr("src");
                    Log.e("chart url", graphScript_url);


                /*chart = Jsoup.connect(graphScript_url)
                        .cookies(cookies)
                        .ignoreContentType(true)
                        .execute().body();
                        */

                chart = new SuperGKSHttpBrowser(getApplicationContext()).login(prefs.getString("account_username", ""), prefs.getString("account_password", "")).connect(graphScript_url).executeInAsyncTask();

                    Log.e("chart code", chart);

                } catch(Exception e) {
                    e.printStackTrace();
                }

                try {
                //avatar
                String avatar_url = doc.select("#avatar img").first().attr("src");
                Log.e("avatar", avatar_url);
                avatar = Base64.encodeToString(Jsoup.connect(avatar_url)
                        .cookies(cookies)
                        .ignoreContentType(true)
                        .execute().bodyAsBytes(), Base64.DEFAULT);

                } catch(Exception e) {}

            } catch (Exception e) {
                e.printStackTrace();
                sendBroadcast(endUpdateIntent);
                Log.d(prefs.getString("account_username", "?"), prefs.getString("account_password", "?"));
            }
            return null;
        }

        @Override
        protected void onPostExecute(String value) {

            try {

                edit.putString("username", username);
                edit.putString("upload", upload);
                edit.putString("download", download);
                edit.putString("ratio", ratio);
                edit.putString("karma", karma.replace(",",""));
                edit.putString("classe", classe);
                edit.putString("hitrun", hitrun);
                edit.putString("twit", twit);
                edit.putString("aura", aura.replace(",",""));
                edit.putString("mails", messages);

                Time today = new Time(Time.getCurrentTimezone());
                today.setToNow();

                edit.putString("lastUpdate", today.format("%d/%m/%Y %k:%M:%S"));

                edit.putString("avatar", avatar);

                if(!chart.equals(""))
                    edit.putString("chart", chart);

                if(prefs.getBoolean("pref_notif_messages", false)) {
                    if(Integer.valueOf(messages) > Integer.valueOf(prefs.getString("mails", "0")))
                        doNotify(1, R.drawable.ic_notif_message_enveloppe, "Nouveaux messages non lus", "Touchez pour les lire", 0, false);
                }

                if(!ratio.equals("0"))
                    edit.commit();

                if(prefs.getBoolean("pref_notif_freeleech", false)) {
                    if(freeleech > 0) {
                        Time date = new Time(Time.getCurrentTimezone());
                        date.set(freeleech*1000L);
                        doNotify(999, R.drawable.ic_notif_fl, freeleech_type, "Expire le " + date.format("%d/%m/%Y à %k:%M"), Notification.FLAG_ONGOING_EVENT | Notification.FLAG_ONLY_ALERT_ONCE, true);
                    }
                }

            } catch(Exception e) {
                e.printStackTrace();
            }

            sendBroadcast(endUpdateIntent);
        }
    }
}
