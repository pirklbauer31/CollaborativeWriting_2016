package fh.mc.collaborativewriting;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class StatisticActivity extends AppCompatActivity {

    private static final String TAG = "StatisticsActivity";

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private FirebaseUser mUser;

    private TextView mActiveUsers;
    private TextView mStories;
    private TextView mTextBlocks;
    private TextView mmyStories;
    private TextView mmyTextBlocks;
    private TextView mUpVotes;
    private TextView mStars;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent i = new Intent(getApplicationContext(), LoginChooserActivity.class);
                    startActivity(i);
                }
            }
        };

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mActiveUsers = (TextView) findViewById(R.id.activeUsers);
        mStories = (TextView) findViewById(R.id.stories);
        mTextBlocks = (TextView) findViewById(R.id.textBlocks);
        mmyStories = (TextView) findViewById(R.id.myStories);
        mmyTextBlocks = (TextView) findViewById (R.id.myTextBlocks);
        mUpVotes = (TextView) findViewById(R.id.upvotes);
        mStars = (TextView) findViewById(R.id.stars);

        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int userCount = 0;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    userCount++;
                }
                mActiveUsers.setText("" + userCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("stories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int storyCount=0;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    storyCount++;
                }
                mStories.setText(""+storyCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
       mDatabase.child("user-stories").child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int myStoriesCount=0;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    myStoriesCount++;
                }
                mmyStories.setText(""+myStoriesCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Query myComments = FirebaseDatabase.getInstance().getReference("stories-comments");
        myComments.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int myTextBlocksCount=0;
                int textBlockCount=0;
                long myUpvoteCount=0;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    for (DataSnapshot myCommentSnapshot : userSnapshot.getChildren()) {
                        textBlockCount++;
                        if (myCommentSnapshot.child("uid").getValue().equals(mUser.getUid())) {
                            myTextBlocksCount++;
                            myUpvoteCount += (long) myCommentSnapshot.child("upvoteCount").getValue();
                            Log.d(TAG, myCommentSnapshot.child("uid").toString());
                        }
                    }
                }
                mTextBlocks.setText(""+textBlockCount);
                mmyTextBlocks.setText(""+myTextBlocksCount);
                mUpVotes.setText(""+myUpvoteCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Query myStars = FirebaseDatabase.getInstance().getReference("user-stories");
        myStars.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int myStarsCount=0;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    for (DataSnapshot myStoriesSnapshot : userSnapshot.getChildren()) {
                        if (myStoriesSnapshot.child("uid").getValue().equals(mUser.getUid())) {
                            for(DataSnapshot myStarsSnapshot : myStoriesSnapshot.child("stars").getChildren())
                                myStarsCount++;
                        }
                    }
                }
                mStars.setText(""+myStarsCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
    private void setUserTextBlock(int userCount){
        mActiveUsers.setText(userCount);
    }


}
