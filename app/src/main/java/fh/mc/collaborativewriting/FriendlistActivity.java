package fh.mc.collaborativewriting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import fh.mc.collaborativewriting.models.User;

public class FriendlistActivity extends AppCompatActivity {

    private static final String TAG = "Friendlist";

    //Database Reference
    private DatabaseReference mDataBase;

    //UI
    private EditText mInputFriendAdd;
    private Button mSendFriendRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendlist);

        mInputFriendAdd = (EditText) findViewById(R.id.inputAddFriend);
        mSendFriendRequest = (Button) findViewById(R.id.cmdSendFriendRequest);

        //  initialize_database_ref

        mDataBase = FirebaseDatabase.getInstance().getReference();

        mSendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddFriend();
            }
        });

    }

    private void AddFriend ()
    {
        final String friendname = mInputFriendAdd.getText().toString();

        final String userId = getUid();
        mDataBase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(FriendlistActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                           //Add new friend
                            getFriendId(userId, friendname);
                        }

                        // Finish this Activity, back to the stream
                       // finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
    }

    public void writeNewFriend(final String userId, String friendId)
    {
        /*
        Query friendRef = mDataBase.child("users").orderByChild("username").equalTo(friendUsername);
        friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User friendUser = dataSnapshot.getValue(User.class);


                if(friendUser == null)
                {
                    //user does not exist, cannot be added as friend
                    Toast.makeText(FriendlistActivity.this,
                            "Error: could not fetch user.",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    String friendId = "";
                    for(DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                        friendId = friendSnapshot.getKey();
                        friendUser = friendSnapshot.getValue(User.class);
                    }

                    // todo Check if user is already added, maybe with array of added users which fills on activity start
                    //check if friend is already added
                    if (friendExists(userId, friendId) == true)
                        Toast.makeText(FriendlistActivity.this, "user already as friend added!", Toast.LENGTH_LONG).show();
                    else
                    {
                        System.out.println(friendUser.username + friendUser.email);
                        //String friendId = dataSnapshot.getValue().toString();
                        Toast.makeText(FriendlistActivity.this, friendId, Toast.LENGTH_LONG).show();

                        mDataBase.child("users").child(userId).child("friends").child(friendId).setValue(true);
                        mDataBase.child("users").child(friendId).child("friends").child(userId).setValue(false);
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
            }
        });

        */
        mDataBase.child("users").child(userId).child("friends").child(friendId).setValue(true);
        mDataBase.child("users").child(friendId).child("friends").child(userId).setValue(false);

        //Toast.makeText(FriendlistActivity.this, friendId, Toast.LENGTH_LONG).show();
    }

    public void friendExists (final String userId, final String friendId, final String friendUsername)
    {
       // Query friendExists = mDataBase.child("users").child(userId).child("friends").orderByChild(friendId);
        Query friendExists = mDataBase.child("users").child(userId).child("friends").child(friendId);
        friendExists.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                   // if (dataSnapshot.getKey().equals(friendId))
                    if(dataSnapshot.exists())
                        Toast.makeText(FriendlistActivity.this,"Friend " + friendUsername + " already exists!", Toast.LENGTH_LONG).show();
                    else
                    {
                        //user is not yet added as friend, call writeNewFriend
                        Toast.makeText(FriendlistActivity.this, "Friend " + friendUsername + " does not exist yet, will be added!", Toast.LENGTH_LONG).show();
                        writeNewFriend(userId, friendId);
                    }

                    //System.out.println("Friendexistskey:" + friendSnapshot.getKey());


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getFriendId (final String userId, final String friendUsername)
    {
        Query friendRef = mDataBase.child("users").orderByChild("username").equalTo(friendUsername);
        friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User friendUser = dataSnapshot.getValue(User.class);


                if(friendUser == null)
                {
                    //user does not exist, cannot be added as friend
                    Toast.makeText(FriendlistActivity.this,
                            "Error: could not fetch user.",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    String friendId = "";
                    for(DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                        friendId = friendSnapshot.getKey();
                        friendUser = friendSnapshot.getValue(User.class);
                    }

                    if(friendId.equals(userId))
                        Toast.makeText(FriendlistActivity.this, "You can't add yourself as friend! (As much as you wish)", Toast.LENGTH_LONG).show();
                    else
                        friendExists(userId, friendId, friendUsername);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
            }
        });
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
