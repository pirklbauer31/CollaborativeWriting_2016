package fh.mc.collaborativewriting.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by Mark on 12.06.2016.
 */
public class RecentStoryFragment extends StoryListFragment {
    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        //TODO: implement more Fragments
        //Query recentStories = databaseReference.child("stories").limitToFirst(30);
        Query recentStories = databaseReference.child("stories").orderByChild("friendsonly").equalTo(false);

        return recentStories;
    }
}
