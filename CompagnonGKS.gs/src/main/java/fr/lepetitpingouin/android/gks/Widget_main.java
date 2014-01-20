package fr.lepetitpingouin.android.gks;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.widget.RemoteViews;

/**
 * Created by gregory on 07/10/13.
 */
public class Widget_main extends AppWidgetProvider {

    SharedPreferences prefs;

    @Override
    public void onReceive(Context context, Intent intent) {

        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), Widget_main.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_home);

        if (intent.getAction().equals(Default.Intent_endUpdate)) {
            for (int widgetId : appWidgetIds) {
                views.setImageViewResource(R.id.widget_updatebtn, R.drawable.ic_refresh);
                appWidgetManager.updateAppWidget(widgetId, views);
            }
        }
        if (intent.getAction().equals(Default.Intent_startUpdate)) {
            for (int widgetId : appWidgetIds) {
                views.setImageViewResource(R.id.widget_updatebtn, R.drawable.ic_refresh_blue);
                appWidgetManager.updateAppWidget(widgetId, views);
            }
        }
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_home);

            views.setTextViewText(R.id.widget_date, prefs.getString("lastUpdate", "Mise à jour nécessaire"));
            views.setTextViewText(R.id.widget_class, prefs.getString("classe", "-"));
            views.setTextViewText(R.id.widget_username, prefs.getString("username", "anonymous"));
            views.setTextViewText(R.id.widget_upload, prefs.getString("upload", "?,?? Go"));
            views.setTextViewText(R.id.widget_download, prefs.getString("download", "?,?? Go"));
            views.setTextViewText(R.id.widget_ratio, prefs.getString("ratio", "?,??"));
            views.setTextViewText(R.id.widget_karma, prefs.getString("karma", "?,???"));
            views.setTextViewText(R.id.widget_mp, prefs.getString("mails", "0")+" MP");

            String encodedImage = prefs.getString("avatar", "");
            if (!encodedImage.equalsIgnoreCase("")) {
                try {
                    byte[] b = Base64.decode(encodedImage, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
                    views.setImageViewBitmap(R.id.widget_avatar, bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    views.setImageViewResource(R.id.widget_avatar, R.drawable.default_avatar);
                }
            }

            Intent mainIntent = new Intent(context.getApplicationContext(), HomeActivity.class);
            PendingIntent oPi = PendingIntent.getActivity(context, 0, mainIntent, 0);
            views.setOnClickPendingIntent(R.id.widget_back, oPi);

            Intent updateIntent = new Intent(context.getApplicationContext(), gksUpdater.class);
            PendingIntent pI = PendingIntent.getService(context, 0, updateIntent, 0);
            views.setOnClickPendingIntent(R.id.widget_updatebtn, pI);

            Intent searchIntent = new Intent(context.getApplicationContext(), SearchActivity.class);
            PendingIntent sPi = PendingIntent.getActivity(context, 0, searchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget_btn_search, sPi);

            Intent mobileIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://gks.gs/mob/?k=" + prefs.getString("authkey", "")));
            PendingIntent mPi = PendingIntent.getActivity(context, 0, mobileIntent, 0);
            views.setOnClickPendingIntent(R.id.widget_btn_mobile, mPi);

            appWidgetManager.updateAppWidget(widgetId, views);

        }
    }

}
