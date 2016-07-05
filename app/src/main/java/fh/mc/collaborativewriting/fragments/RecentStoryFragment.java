package fh.mc.collaborativewriting.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by Mark on 12.06.2016.
 */

/**
 * displays stories sorted by their creation time
 */
public class RecentStoryFragment extends StoryListFragment {
    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        return databaseReference.child("stories").limitToFirst(30);
    }
}
