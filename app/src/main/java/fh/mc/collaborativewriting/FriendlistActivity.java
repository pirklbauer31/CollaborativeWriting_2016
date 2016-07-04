package fh.mc.collaborativewriting;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import fh.mc.collaborativewriting.models.Friend;
import fh.mc.collaborativewriting.models.User;

public class FriendlistActivity extends BaseActivity {

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

    /**
     * Stores the input (username of friend to add) and calls the method
     * "getFriendId" with the userId of the current user and the username of the friend to add.
     */
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

    /**
     * This method writes the friend entries to the given users in the database.
     * <br>
     * It will write the boolean value true to the friend entry of the current user and
     * the value false to the user to add as friend.
     * <br>
     * This will allow the other user to accept the friend request.
     * <br>
     * After that it notifies the RecyclerView adapter of the changes to update the UI.
     *
     * @param userId The userId of the current user
     * @param friendId The userId of the user to add as friend
     */
    public void writeNewFriend(final String userId, String friendId)
    {

        mDataBase.child("users").child(userId).child("friends").child(friendId).setValue(true);
        mDataBase.child("users").child(friendId).child("friends").child(userId).setValue(false);
        mAdapter.notifyItemInserted(friendList.size());
        mInputFriendAdd.setText("");

        //Toast.makeText(FriendlistActivity.this, friendId, Toast.LENGTH_LONG).show();
    }

    /**
     * This method checks if the user to add as friend has already been added to the friend-list.
     * <br>
     * If not, it will call the method "writeNewFriend" with the userIds of the current user
     * and of the user to add as friend.
     *
     * @param userId The userId of the current user
     * @param friendId The userId of the user to add as friend
     * @param friendUsername The username of the user to add as friend
     */
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

    /**
     * Fetches the userId of the user to add as friend with the given username.
     * <br>
     * Also checks if the user to add exists or is the user itself.
     * It then calls the method "friendExists" with the userIds of the current user and
     * the friend to add, as well as the username as parameters.
     *
     * @param userId The userId of the current user
     * @param friendUsername The username of the user to add as friend
     */
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

    /**
     * Fetches all friend entries for the user from the Firebase real-time database.
     * It also sets a ValueEventListener which fetches new friend entries
     * when the data structure has changed.
     * <br>
     * The method stores the Key and value of all friend entries and calls the
     * method "addFriendToList" with both values as parameters.
     *
     */
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


    /**
     * Fetches the user with the given userId from the Firebase real-time database,
     * stores its values in a "Friend" object and adds it to the ArrayList
     * which contains the friends to fill the RecyclerView with.
     *
     * @param friendId The userId of the friend to add
     * @param friendAccepted Boolean value which describes if the friend-request has been answered
     */
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

    /**
     * This internal class describes the RecyclerView Adapter for the friend-list.
     * It basically binds the values to the view-objects (TextViews, ImageViews, etc)
     * and sets onClickListeners for the Accept and Remove buttons.
     *
     * @author Kevin
     */
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
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            Friend friend = friendList.get(position);
            holder.usernameView.setText(friend.username);
            
            if (friend.acceptedFriend){
                holder.acceptFriendView.setVisibility(View.GONE);
            }
            else {
                holder.acceptFriendView.setVisibility(View.VISIBLE);
                holder.acceptFriendView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        acceptFriendRequest(holder.getAdapterPosition());
                    }
                });
            }

            holder.removeFriendView.setVisibility(View.VISIBLE);
            holder.removeFriendView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    removeFriend(holder.getAdapterPosition());
                }
            });

            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users");
            Query searchForUserPic = myRef.child(friend.userId).child("profilePic");
            searchForUserPic.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    StorageReference profileReference = FirebaseStorage.getInstance().getReferenceFromUrl(String.valueOf(dataSnapshot.getValue()));



                    final long ONE_MEGABYTE = 1024 * 1024;
                    profileReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void onSuccess(byte[] bytes) {
                            // Data for profilePic is returned
                            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            holder.profileView.setImageBitmap(getCroppedBitmap(bm, 100));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return friendList.size();
        }

    }

    /**
     * Updates the boolean value for friendAccepted in the database.
     * <br>
     * It sets the values on both users to true and updates the UI.
     * A boolean value true in that case means, that the friend-request has been accepted.
     *
     * @param positionOfFriend The position of the friend in the RecyclerView
     */
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


    /**
     * Removes the friend entries from both users from the database and updates
     * the UI.
     * @param positionOfFriend The position of the friend in the RecyclerView
     */
    public void removeFriend (int positionOfFriend)
    {
        Friend friendToRemove = friendList.get(positionOfFriend);
        String userId = getUid();

        mDataBase.child("users").child(userId).child("friends").child(friendToRemove.userId).removeValue();
        mDataBase.child("users").child(friendToRemove.userId).child("friends").child(userId).removeValue();

        friendList.remove(positionOfFriend);
        mAdapter.notifyItemRemoved(positionOfFriend);
    }

    /*public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
    */
}


