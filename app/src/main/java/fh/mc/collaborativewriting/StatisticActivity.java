package fh.mc.collaborativewriting;

import android.os.Bundle;
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

/**
 * Activity Shows simple User-Statistics
 * @Author Felix Mauler
 */
public class StatisticActivity extends AppCompatActivity {

    private static final String TAG = "StatisticsActivity";

    private FirebaseUser mUser;

    /**
     *
     */
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

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        /*
      Firebase Parameters to connect to Database and get User
     */
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        mActiveUsers = (TextView) findViewById(R.id.activeUsers);
        mStories = (TextView) findViewById(R.id.stories);
        mTextBlocks = (TextView) findViewById(R.id.textBlocks);
        mmyStories = (TextView) findViewById(R.id.myStories);
        mmyTextBlocks = (TextView) findViewById (R.id.myTextBlocks);
        mUpVotes = (TextView) findViewById(R.id.upvotes);
        mStars = (TextView) findViewById(R.id.stars);

        /**
         * Count the number of registered Users
         */
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
        /**
         * Count the number of created Stories
         */
        mDatabase.child("stories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int storyCount=0;
                for (DataSnapshot storySnapshot : dataSnapshot.getChildren()){
                    storyCount++;
                }
                mStories.setText(""+storyCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /**
         * Count the number of Stories the logged-in User has created
         */
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


        /**
         * Count the number of Comments in general and per logged-in user + the upvotes of that user
         */
        Query myComments = FirebaseDatabase.getInstance().getReference("stories-comments");
        myComments.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int myCommentCount=0;
                int commentCount=0;
                long myUpvoteCount=0;
                for (DataSnapshot storiesSnapshot : dataSnapshot.getChildren()){
                    for (DataSnapshot myCommentSnapshot : storiesSnapshot.getChildren()) {
                        commentCount++;
                        if (myCommentSnapshot.child("uid").getValue().equals(mUser.getUid())) {
                            myCommentCount++;
                            myUpvoteCount += (long) myCommentSnapshot.child("upvoteCount").getValue();
                            Log.d(TAG, myCommentSnapshot.child("uid").toString());
                        }
                    }
                }
                mTextBlocks.setText(""+commentCount);
                mmyTextBlocks.setText(""+myCommentCount);
                mUpVotes.setText(""+myUpvoteCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /**
         * Count the number of stars the logged-in user has
         */
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
