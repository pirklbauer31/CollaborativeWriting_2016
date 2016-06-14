package fh.mc.collaborativewriting;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Felix on 30.05.2016.
 *
 */
@IgnoreExtraProperties
public class Story {
    public String uid;
    public String creator;
    public String title;
    public String description;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public Story(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Story(String uid, String creator, String title, String description){
        this.uid=uid;
        this.creator=creator;
        this.title=title;
        this.description=description;
    }
    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object>result = new HashMap<>();
        result.put("uid", uid);
        result.put("creator", creator);
        result.put("title", title);
        result.put("description", description);
        result.put("starCount",starCount);
        result.put("stars",stars);

        return result;
    }
}
