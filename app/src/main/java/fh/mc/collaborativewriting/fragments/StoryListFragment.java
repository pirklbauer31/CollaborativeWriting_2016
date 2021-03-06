package fh.mc.collaborativewriting.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import fh.mc.collaborativewriting.DetailActivity;
import fh.mc.collaborativewriting.R;
import fh.mc.collaborativewriting.models.Story;
import fh.mc.collaborativewriting.viewholder.StoryViewHolder;

/**
 * Created by mar k on 11.06.2016.
 */


public abstract class StoryListFragment extends Fragment {

    private static final String TAG = "StoryListFragment";
    private FirebaseRecyclerAdapter<Story, StoryViewHolder> mAdapter;

    private DatabaseReference mDatabase;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public StoryListFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecycler = (RecyclerView) rootView.findViewById(R.id.story_list);
        mRecycler.setHasFixedSize(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        final Query storyQuery = getQuery(mDatabase);
        mAdapter = new FirebaseRecyclerAdapter<Story, StoryViewHolder>(Story.class, R.layout.item_story,
                StoryViewHolder.class, storyQuery) {
            @Override
            protected void populateViewHolder(final StoryViewHolder viewHolder, final Story model, final int position) {
                final DatabaseReference storyRef = getRef(position);

                //Set click listener
                final String storyKey = storyRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), DetailActivity.class);
                        i.putExtra(DetailActivity.EXTRA_STORY_KEY, storyKey);
                        startActivity(i);
                    }
                });
                //has the story been "liked"
                if (model.stars.containsKey(getUid())) {
                    viewHolder.starView.setImageResource(R.drawable.ic_star_black_36dp);
                } else {
                    viewHolder.starView.setImageResource(R.drawable.ic_star_border_black_36dp);
                }

                //bind story to viewholder and set OnClickListener
                viewHolder.bindToStory(model);
            }
        };
        mRecycler.setAdapter(mAdapter);


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    /**
     * Get the unique ID from the currently signed in user
     * @return returns UID from current user
     */
    String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    /**
     * Define the DatabaseReference from where you want to get your stories to fill the fragment
     *
     * @param databaseReference The path of the stories to be displayed
     * @return the Query to be used for your fragment
     */
    protected abstract Query getQuery(DatabaseReference databaseReference);
}
