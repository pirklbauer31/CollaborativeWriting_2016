package fh.mc.collaborativewriting;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import fh.mc.collaborativewriting.models.User;

import static fh.mc.collaborativewriting.R.string.error_registration_failed;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    private static final String TAG = "LoginActivity";
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */


    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private EditText mUsernameView;
    private EditText mFirstnameView;
    private EditText mLastnameView;


    private Resources res;
    private int[] userColors;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    private CallbackManager mCallBackManager;

    private boolean userExists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FacebookSdk.sdkInitialize(getApplicationContext());

        //get predefined userColors
        res = getResources();
        userColors = res.getIntArray(R.array.usercolors);


        setContentView(R.layout.activity_login);

        Intent i = getIntent();
        String regOrLog=i.getStringExtra("LoginChooser");

        mAuth= FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance();

        mAuthListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    showProgress(false);
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Intent i= new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                } else {
                    // User is signed out
                    Log.d( TAG, "onAuthStateChanged:signed_out");
                }
            }
        };


        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mUsernameView= (EditText) findViewById(R.id.username);
        mLastnameView= (EditText) findViewById(R.id.lastname);
        mFirstnameView= (EditText) findViewById(R.id.firstname);

        Button mEmailRegisterButton = (Button) findViewById(R.id.email_register_button);
        Button mEmailSearchButton = (Button) findViewById(R.id.email_search_button);

        mEmailRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIfUserExists();
            }
        });



        mEmailSearchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIfUserExists();
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_signin_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                checkIfMailOrUser();
            }
        });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


        /*
        //Facebook Login
        mCallBackManager = CallbackManager.Factory.create();
        final LoginButton loginButton = (LoginButton) findViewById(R.id.fb_login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }
            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel:");
            }
            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError:", error);
            }
        });
        */
        if(regOrLog.contentEquals("Login")){
            mEmailRegisterButton.setVisibility(View.GONE);
            mEmailSearchButton.setVisibility(View.GONE);
            mLastnameView.setVisibility(View.GONE);
            mFirstnameView.setVisibility(View.GONE);
            mUsernameView.setVisibility(View.GONE);
            mEmailView.setHint("Email or Username");
        }else{
            mEmailSignInButton.setVisibility(View.GONE);
        }


    }

    void checkIfMailOrUser(){
        if (Patterns.EMAIL_ADDRESS.matcher(mEmailView.getText().toString()).matches())
            signInUserWithEmail(mEmailView.getText().toString());
        else {
            getEmailFromUser();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallBackManager.onActivityResult(requestCode, resultCode, data);
    }
    /*
    private void handleFacebookAccessToken(AccessToken accessToken) {
        Log.d(TAG, "handleFacebookAccessToken: " + accessToken);

        showProgress(true);
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        writeNewUser();
                        showProgress(false);
                    }
                });
    }*/

    /**
     * Validates if email and passwort are not empty and password-length > 4 characters
     * @return true if correct, false if not.
     */
    private boolean validateEmailPassword () {
        View focusView = null;
        boolean valid=true;
        String email= mEmailView.getText().toString();
        String password= mPasswordView.getText().toString();
        String username=mUsernameView.getText().toString();


        if (email.isEmpty()) {
            mEmailView.setError(getString( R.string.error_field_required));
            valid = false;
            focusView=mEmailView;
        }

        else if (password.isEmpty() || password.length() < 4 ) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            valid = false;
        }

        else {
            mPasswordView.setError(null);
        }
        return valid;
    }

    /**
     * Signs in User into Firebase if email and password are correct
     * @param givenMail The mail-address of the user to sign in
     */
    private void signInUserWithEmail(String givenMail) {

        final String email= givenMail;

        if (validateEmailPassword()) {

                mAuth.signInWithEmailAndPassword(givenMail, mPasswordView.getText().toString())
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "signInWithEmail", task.getException());
                                    Toast.makeText(LoginActivity.this, "Email doesnt exist",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }

    /**
     * Function gets email-address from username and logs user in if the name is correct
     */
    private void getEmailFromUser(){
        DatabaseReference myRef = mDatabase.getReference("users");
        Query userSearch = myRef.orderByChild("email");

        userSearch.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User searchedUser = dataSnapshot.getValue(User.class);


                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()
                     ) {

                    if (userSnapshot.child("username").getValue().toString().contentEquals(mEmailView.getText().toString())) {

                        signInUserWithEmail(userSnapshot.child("email").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getUser:onCancelled", databaseError.toException());

            }
        });
    }

    /**
     * Function creates new user in the database if given data is correct
     * calls writeNewUser()
     */
    private void createUserwithEmail() {

        final String email= mEmailView.getText().toString();
        String password= mPasswordView.getText().toString();

        if (!validateEmailPassword()) {
            return;
        }


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), error_registration_failed,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            writeNewUser();

                        }
                    }
                });

    }

    /**
     * Checks if user already exists in database (email and username are checked)
     * if it doesn't exist createUserWithEmail() is called
     */
    private void checkIfUserExists() {
        String email=mEmailView.getText().toString();
        String username= mUsernameView.getText().toString();

        DatabaseReference myRef = mDatabase.getReference("users");

        Query searchForUserName=myRef.orderByChild("username").equalTo(username);
        searchForUserName.addListenerForSingleValueEvent(new ValueEventListener () {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0) {
                    Toast.makeText(getApplicationContext(), "User already exists! (Username)",
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "User exists");
                    showProgress(false);
                } else {
                    DatabaseReference myRef = mDatabase.getReference("users");
                    Query searchForUserMail= myRef.orderByChild("email").equalTo(mEmailView.getText().toString());

                    showProgress(true);

                    searchForUserMail.addListenerForSingleValueEvent(new ValueEventListener () {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getChildrenCount() != 0) {
                                Toast.makeText(getApplicationContext(), "User already exists! (email)",
                                        Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "User exists");
                                showProgress(false);
                            } else {
                                createUserwithEmail();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }


                    });
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });

    }

    /**
     *Creates new User in the Firebase Database
     */
    private void writeNewUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        if (user != null) {
            // Name, email address, and profile photo Url
            String email = user.getEmail();
            String username= mUsernameView.getText().toString();
            String uid = user.getUid();

            String firstname= mFirstnameView.getText().toString();
            String lastname= mLastnameView.getText().toString();

            UserProfileChangeRequest setDisplayName= new UserProfileChangeRequest.Builder()
                    .setDisplayName(username).setPhotoUri(Uri.parse("gs://project-cow.appspot.com/testProfile.png")).build();

            user.updateProfile(setDisplayName)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User profile updated.");
                            }
                        }
                    });


            User userObject= new User(username, email,firstname,lastname
                    , "gs://project-cow.appspot.com/testProfile.png", userColors[(int) (Math.random() * userColors.length)]);
            DatabaseReference myRef = mDatabase.getReference("users");
            myRef.child(uid).setValue(userObject);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    /**
     * Shows the progress UI and hides the login form.
     */
    //TODO: nd so schirche Animation finden, wenn möglich
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
