package fh.mc.collaborativewriting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import fh.mc.collaborativewriting.models.Friend;
import fh.mc.collaborativewriting.models.User;

public class FriendlistActivity extends AppCompatActivity {

    private static final String TAG = "Friendlist";

    //Database Reference
    private DatabaseReference mDataBase;

    //UI
    private EditText mInputFriendAdd;
    private Button mSendFriendRequest;

    private List<Friend> friendList = new ArrayList<>();
    private RecyclerView recyclerView;
    private FriendsAdapter mAdapter;


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

        // prepare Recyclerview
        recyclerView = (RecyclerView) findViewById(R.id.friend_recycler_view);

        mAdapter = new FriendsAdapter(friendList);
        RecyclerView.LayoutManager mLayoutManger = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManger);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        prepareFriendData();
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
        mAdapter.notifyItemInserted(friendList.size());
        mInputFriendAdd.setText("");

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

    public void prepareFriendData()
    {
        String userId = getUid();
        Query friendRef = mDataBase.child("users").child(userId).child("friends");
        friendRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot != null)
                {
                    friendList.clear();

                    boolean friendAccepted = false;
                    String friendId = "";
                    for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                        friendId = friendSnapshot.getKey();
                        friendAccepted = (boolean)friendSnapshot.getValue();

                        addFriendToList(friendId, friendAccepted);
                        System.out.println();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
            }
        });
    }

    public void addFriendToList (final String friendId, final boolean friendAccepted)
    {
        Query singleFriendRef = mDataBase.child("users").child(friendId);
        singleFriendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null)
                {
                    User friendUser = dataSnapshot.getValue(User.class);
                    String friendUsername = friendUser.username;
                    String friendProfilepic = friendUser.profilePic;
                    Friend friendToAdd = new Friend(friendUsername, friendProfilepic, friendAccepted, friendId);

                    friendList.add(friendToAdd);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
            }
        });
    }

    public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.MyViewHolder> {
        private List<Friend> friendList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView usernameView;
            public ImageView profileView;
            public ImageButton acceptFriendView;
            public ImageButton removeFriendView;

            public MyViewHolder(View view) {
                super(view);
                usernameView = (TextView) itemView.findViewById(R.id.story_author);
                profileView = (ImageView) itemView.findViewById(R.id.story_author_profile_pic);
                acceptFriendView = (ImageButton) itemView.findViewById(R.id.accept_friend);
                removeFriendView = (ImageButton) itemView.findViewById(R.id.remove_friend);
            }
        }

        public FriendsAdapter(List<Friend> friendList) {
            this.friendList = friendList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_friend, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            Friend friend = friendList.get(position);
            holder.usernameView.setText(friend.username);
            // todo set profile picture
            if (friend.acceptedFriend){
                holder.acceptFriendView.setVisibility(View.GONE);
            }
            else {
                holder.acceptFriendView.setVisibility(View.VISIBLE);
                //todo add OnClickListeners to Buttons
                holder.acceptFriendView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        acceptFriendRequest(position);
                    }
                });
            }

            holder.removeFriendView.setVisibility(View.VISIBLE);
            holder.removeFriendView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    removeFriend(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return friendList.size();
        }

    }

    public void acceptFriendRequest (int positionOfFriend)
    {
        Friend friendToAccept = friendList.get(positionOfFriend);
        String userId = getUid();

        //write changes in database and in list item
        mDataBase.child("users").child(userId).child("friends").child(friendToAccept.userId).setValue(true);
        friendToAccept.acceptedFriend = true;

        friendList.set(positionOfFriend, friendToAccept);
        mAdapter.notifyItemChanged(positionOfFriend);
    }

    public void removeFriend (int positionOfFriend)
    {
        Friend friendToRemove = friendList.get(positionOfFriend);
        String userId = getUid();

        mDataBase.child("users").child(userId).child("friends").child(friendToRemove.userId).removeValue();
        mDataBase.child("users").child(friendToRemove.userId).child("friends").child(userId).removeValue();

        friendList.remove(positionOfFriend);
        mAdapter.notifyItemRemoved(positionOfFriend);
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}


