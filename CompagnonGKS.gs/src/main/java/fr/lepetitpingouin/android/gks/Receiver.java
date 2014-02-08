package fr.lepetitpingouin.android.gks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent i) {

        try {
            Intent intent = new Intent(ctx, gksUpdater.class);
            ctx.startService(intent);
        } finally {

        }
    }
}