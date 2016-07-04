package fh.mc.collaborativewriting;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fh.mc.collaborativewriting.models.Contribution;
import fh.mc.collaborativewriting.models.Story;
import fh.mc.collaborativewriting.models.User;

public class DetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "StoryDetailActivity";

    //stores the UID of the story passed by the MainActivity
    public static final String EXTRA_STORY_KEY = "story_key";

    //firebase references
    private static DatabaseReference mStoryReference;
    private static DatabaseReference mUserStoryReference;
    private DatabaseReference mUserStarredReference;
    private static DatabaseReference mContributionReference;
    private ValueEventListener mStoryListener;
    private String mStoryKey;
    private ContributionAdapter mAdapter;
    private static Story mStory;

    private static ArrayList<String> commentOptions=new ArrayList<>();

    //UI
    private TextView mAuthorView;
    private TextView mTitleView;
    private TextView mDescriptionView;
    private TextView mStarNumView;
    private EditText mContributionField;
    private ImageView mStarView;
    private ImageView mStoryAuthorPicView;
    private RecyclerView mContributionsRecycler;
    private static Button mContributionButton;
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
        mUserStoryReference = FirebaseDatabase.getInstance().getReference()
                .child("user-stories").child(getUid()).child(mStoryKey);
        mContributionReference = FirebaseDatabase.getInstance().getReference()
                .child("stories-comments").child(mStoryKey);
        mUserStarredReference = FirebaseDatabase.getInstance().getReference().
                child("user-starred-stories").child(getUid()).child(mStoryKey);

        //Init View
        mAuthorView = (TextView) findViewById(R.id.story_author);
        mDescriptionView = (TextView) findViewById(R.id.story_description);
        mTitleView = (TextView) findViewById(R.id.story_title);
        mStarNumView = (TextView) findViewById(R.id.story_detail_num_stars);
        mStarView = (ImageView) findViewById(R.id.stars_detail);
        mStoryAuthorPicView = (ImageView) findViewById(R.id.story_author_profile_pic);
        mContributionField = (EditText) findViewById(R.id.field_comment_text);

        mContributionButton = (Button) findViewById(R.id.button_contribute);
        mContributionsRecycler = (RecyclerView) findViewById(R.id.recycler_comments);


        mStarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStarClicked(mUserStoryReference);
                onStarClicked(mStoryReference);
                onStarClicked(mUserStarredReference);
            }
        });

        mContributionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContributionField.getText().length() >0)
                    postContribution();
            }
        });

        final GridLayoutManager manager = new GridLayoutManager(this, 6);
        final int multiplier=6;
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int length = mAdapter.mContributions.get(position).text.length();
                if (length < multiplier)
                    return 1;
                else if (length > multiplier * manager.getSpanCount())
                    return manager.getSpanCount();
                else
                    return (mAdapter.mContributions.get(position).text.length() / multiplier);
            }
        });

        mContributionsRecycler.setLayoutManager(manager);

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
                mDescriptionView.setText(mStory.description);

                mStarNumView.setText(String.valueOf(mStory.starCount));
                if (mStory.stars.containsKey(getUid())) {
                    mStarView.setImageResource(R.drawable.ic_star_black_36dp);
                } else {
                    mStarView.setImageResource(R.drawable.ic_star_border_black_36dp);
                }

                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users");
                Query searchForUserPic = myRef.child(mStory.uid).child("profilePic");
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

                                mStoryAuthorPicView.setImageBitmap(getCroppedBitmap(bm, 200));
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

    private void onStarClicked(DatabaseReference storiesRef) {
        storiesRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Story s = mutableData.getValue(Story.class);
                if (s == null) {
                    return Transaction.success(mutableData);
                }

                if (s.stars.containsKey(getUid())) {
                    //unstar story
                    s.starCount--;
                    s.stars.remove(getUid());
                    mUserStarredReference.removeValue();
                } else {
                    //star story
                    s.starCount++;
                    s.stars.put(getUid(), true);

                    //add user to the stars in the story before saving it in the DB
                    if (!mStory.stars.containsKey(getUid())) {
                        mStory.starCount++;
                        mStory.stars.put(getUid(), true);
                    }
                    //Create Story at /user-starred-stories/$userid/

                    Map<String, Object> storyValues = mStory.toMap();

                    mUserStarredReference.updateChildren(storyValues);
                }

                //Set value
                mutableData.setValue(s);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.d(TAG, "storyTransaction complete:" + databaseError);
            }
        });
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
                        Contribution contribution = new Contribution(authorName, uid, contributionText, user.userColor);

                        //push comment
                        mContributionReference.push().setValue(contribution);

                        //clear field
                        mContributionField.setText(null);

                        mContributionButton.setEnabled(false);
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

                        //enable commentButton if comment is not from the user
                        if ((contributionIndex==mContributionIds.size()-1) && mContributions.get(contributionIndex).uid.equals(getUid()))
                            mContributionButton.setEnabled(true);

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

            //is comment from current user? --> disable post-button
            if (!contribution.uid.equals(getUid()))
                mContributionButton.setEnabled(true);
            else
                mContributionButton.setEnabled(false);
            //is it a really good comment?
            //TODO:
            if (contribution.upvoteCount > 2) {
                holder.contributionText.setTextColor(Color.parseColor("#FFD600"));
                holder.contributionText.setTypeface(null, Typeface.BOLD);

            }
            else
                holder.contributionText.setTextColor(contribution.color);
            //check if the current user is the author of this story to enable moderation
            //TODO: add Moderators? (eher schon ein Wunschziel.. ^^)
            //if (mStory.uid.equals(getUid()) || contribution.uid.equals(getUid())) {
                holder.contributionText.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        commentOptions.clear();
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                        if (!contribution.upvotes.containsKey(getUid()))
                            commentOptions.add("Upvote");
                        else
                            commentOptions.add("Remove Upvote");
                        if (contribution.uid.equals(getUid()) || mStory.uid.equals(getUid())) {
                            commentOptions.add("Delete");
                        }



                        String[] options= commentOptions.toArray(new String[0]);
                        builder.setTitle("Author: "+ contribution.author)
                                .setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //comment upvoted
                                        if (which==0) {
                                            onUpvoteClicked(mContributionIds.get(mContributions.indexOf(contribution)));
                                        } else if (which==1) {
                                            //delete comment
                                            mContributionReference.child(mContributionIds.get(mContributions.indexOf(contribution))).removeValue();
                                        }
                                    }
                                });

                        // Create the AlertDialog
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return false;
                    }

                });
            //}
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

        private void onUpvoteClicked(String cuid) {
            mContributionReference.child(cuid).runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Contribution c = mutableData.getValue(Contribution.class);
                    if (c == null) {
                        return Transaction.success(mutableData);
                    }

                    if (c.upvotes.containsKey(getUid())) {
                        //unvote comment
                        c.upvoteCount--;
                        c.upvotes.remove(getUid());
                    } else {
                        //upvote comment
                        c.upvoteCount++;
                        c.upvotes.put(getUid(), true);
                    }

                    //Set value
                    mutableData.setValue(c);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    Log.d(TAG, "contributionTransaction complete:" + databaseError);
                }
            });
        }
    }
}
