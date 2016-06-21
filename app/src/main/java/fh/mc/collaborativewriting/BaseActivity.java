package fh.mc.collaborativewriting;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by mar k on 11.06.2016.
 */
public class BaseActivity extends AppCompatActivity {
    public static String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
