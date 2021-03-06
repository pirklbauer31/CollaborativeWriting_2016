package fh.mc.collaborativewriting.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mar k on 11.06.2016.
 */
// [START post_class]
@IgnoreExtraProperties
public class Story {

    public String uid;
    public String author;
    public String title;
    public String description;
    public boolean friendsOnly;

    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();
    public ArrayList<String> tags = new ArrayList<>();

    public Story() {
        // Default constructor required for calls to DataSnapshot.getValue(Story.class)
    }


    /**
     * @param userId  the unique ID of the author of this story
     * @param auth    Username of the author
     * @param t       title of this story
     * @param b       short description of the content
     * @param tagList some metatags
     * @param fOnly   is story public or friends-only?
     */
    public Story(String userId, String auth, String t, String b, List<String> tagList, boolean fOnly) {
        uid = userId;
        author = auth;
        title = t;
        description = b;
        tags = (ArrayList<String>) tagList;
        friendsOnly = fOnly;

    }



    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("description", description);
        result.put("starCount", starCount);
        result.put("stars", stars);
        result.put("friendsonly", friendsOnly);

        return result;
    }
    // [END post_to_map]

}
// [END post_class]
