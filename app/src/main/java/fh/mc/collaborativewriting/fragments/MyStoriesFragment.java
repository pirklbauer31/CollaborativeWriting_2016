package fh.mc.collaborativewriting.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by Mark on 12.06.2016.
 */

/**
 * displays the stories created by the currently signed in user
 */
public class MyStoriesFragment extends StoryListFragment {
    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        //my stories
        return databaseReference.child("user-stories").child(getUid());
    }
}
