package fh.mc.collaborativewriting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Mark on 21.06.2016.
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Log.d("app", "Network connectivity change");
        if (intent.getExtras() != null) {
            NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                Log.i("app", "Network " + ni.getTypeName() + " connected");
            }
        }
        if (intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
            //display message when user loses connection to the internet
            Toast toast = Toast.makeText(context, "No internet connection!", Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
