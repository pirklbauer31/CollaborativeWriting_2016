package fh.mc.collaborativewriting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import fh.mc.collaborativewriting.models.Contribution;
import fh.mc.collaborativewriting.models.Story;
import fh.mc.collaborativewriting.models.User;

public class DetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "StoryDetailActivity";

    public static final String EXTRA_STORY_KEY = "story_key";

    //firebase references
    private static DatabaseReference mStoryReference;
    private static DatabaseReference mContributionReference;
    private ValueEventListener mStoryListener;
    private String mStoryKey;
    private ContributionAdapter mAdapter;
    private static Story mStory;


    //UI
    private TextView mAuthorView;
    private TextView mTitleView;
    private TextView mDescriptionView;
    private EditText mContributionField;
    private RecyclerView mContributionsRecycler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        // Get post key from intent
        mStoryKey = getIntent().getStringExtra(EXTRA_STORY_KEY);
        if (mStoryKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_STRING_KEY");
        }

        // Initialize Database
        mStoryReference = FirebaseDatabase.getInstance().getReference()
                .child("stories").child(mStoryKey);
        mContributionReference = FirebaseDatabase.getInstance().getReference()
                .child("stories-comments").child(mStoryKey);


        //Init View
        mAuthorView = (TextView) findViewById(R.id.story_author);
        mDescriptionView = (TextView) findViewById(R.id.story_description);
        mTitleView = (TextView) findViewById(R.id.story_title);
        mContributionField = (EditText) findViewById(R.id.field_comment_text);

        Button mContributionButton = (Button) findViewById(R.id.button_contribute);
        mContributionsRecycler = (RecyclerView) findViewById(R.id.recycler_comments);

        mContributionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postContribution();
            }
        });
        mContributionsRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener storyListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                mStory = dataSnapshot.getValue(Story.class);
                // [START_EXCLUDE]
                mAuthorView.setText(mStory.author);
                mTitleView.setText(mStory.title);
                mDescriptionView.setText(mStory.body);
                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(DetailActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mStoryReference.addValueEventListener(storyListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mStoryListener = storyListener;

        // Listen for comments
        mAdapter = new ContributionAdapter(this, mContributionReference);
        mContributionsRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {

    }

    private void postContribution() {
        final String uid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //user info
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.username;

                        //new contribution object
                        String contributionText = mContributionField.getText().toString();
                        Contribution contribution = new Contribution(uid, authorName, contributionText);

                        //push comment
                        mContributionReference.push().setValue(contribution);

                        //clear field
                        mContributionField.setText(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private static class CommentViewHolder extends RecyclerView.ViewHolder {

        public TextView contributionText;

        public CommentViewHolder(View itemView) {
            super(itemView);

            contributionText = (TextView) itemView.findViewById(R.id.contribution_text);


        }
    }


    private static class ContributionAdapter extends RecyclerView.Adapter<CommentViewHolder> {

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mContributionIds = new ArrayList<>();
        private List<Contribution> mContributions = new ArrayList<>();

        public ContributionAdapter(Context context, DatabaseReference ref) {
            this.mContext = context;
            this.mDatabaseReference = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new comment has been added, add it to the displayed list
                    Contribution contribution = dataSnapshot.getValue(Contribution.class);

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    mContributionIds.add(dataSnapshot.getKey());
                    mContributions.add(contribution);
                    notifyItemInserted(mContributions.size() - 1);
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    Contribution newContribution = dataSnapshot.getValue(Contribution.class);
                    String contributionKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int commentIndex = mContributionIds.indexOf(contributionKey);
                    if (commentIndex > -1) {
                        // Replace with the new data
                        mContributions.set(commentIndex, newContribution);

                        // Update the RecyclerView
                        notifyItemChanged(commentIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + contributionKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    String contributionKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int contributionIndex = mContributionIds.indexOf(contributionKey);
                    if (contributionIndex > -1) {
                        // Remove data from the list
                        mContributionIds.remove(contributionIndex);
                        mContributions.remove(contributionIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(contributionIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + contributionKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    //TODO:moving Comments needed?
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_contribution, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CommentViewHolder holder, final int position) {
            final Contribution contribution = mContributions.get(position);
            holder.contributionText.setText(contribution.text);
            //check if the current user is the author of this story to enable moderation
            //TODO: add Moderators? (eher schon ein Wunschziel.. ^^)
            if (mStory.uid.equals(getUid())) {
                holder.contributionText.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setMessage("Delete this contribution?");
                        // Add the buttons
                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK button
                                //remove Contribution
                                mContributionReference.child(mContributionIds.get(mContributions.indexOf(contribution))).removeValue();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });

                        // Create the AlertDialog
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return false;
                    }

                });
            }
        }

        @Override
        public int getItemCount() {
            return mContributions.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }
    }
}
