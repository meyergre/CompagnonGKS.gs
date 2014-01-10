package fr.lepetitpingouin.android.gks;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Created by gregory on 07/10/13.
 */
public class Widget_search extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_search);

            Intent updateIntent = new Intent(context.getApplicationContext(), SearchActivity.class);
            PendingIntent pI = PendingIntent.getActivity(context, 0, updateIntent, 0);
            views.setOnClickPendingIntent(R.id.widget_search, pI);

            appWidgetManager.updateAppWidget(widgetId, views);

        }
    }

}
