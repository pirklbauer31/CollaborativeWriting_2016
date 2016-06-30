package fh.mc.collaborativewriting.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by Mark on 12.06.2016.
 */
public class StarredStoriesFragment extends StoryListFragment {
    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        //TODO: need to change database to work properly with only the stories "starred" by the user
        Query starredStories = databaseReference.child("user-starred-stories").child(getUid());
        return starredStories;
    }
}
