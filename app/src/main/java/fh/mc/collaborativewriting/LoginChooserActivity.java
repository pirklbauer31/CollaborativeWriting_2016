package fh.mc.collaborativewriting;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/**
 * View represents only 2 Buttons to choose whether you want to sign in or register a new User
 * If you were already logged in last time using the app it will redirect you to the MainActivity
 * @Author Felix Mauler
 */
public class LoginChooserActivity extends BaseActivity {


    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    private static final String TAG = "LoginChooserActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login_chooser);
        mAuth= FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance();

        //Firebase logged on
        mAuthListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Intent i= new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                } else {
                    // User is signed out
                    Log.d( TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        Button login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                Intent i= new Intent(getApplicationContext(), LoginActivity.class);
                i.putExtra("LoginChooser", "Login");
                startActivity(i);
            }
        });

        Button register = (Button) findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener(){
            @Override
                    public void onClick (View view){
            Intent i= new Intent(getApplicationContext(), LoginActivity.class);
                i.putExtra("LoginChooser", "Register");
            startActivity(i);
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        //check if device is connected to the internet
        View parentLayout = findViewById(R.id.login);
        if (!isOnline()) {
            Snackbar snackbar = Snackbar
                    .make(parentLayout, "No internet connection!", Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
