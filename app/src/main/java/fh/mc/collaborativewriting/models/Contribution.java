package fh.mc.collaborativewriting.models;

import android.graphics.Color;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mark on 13.06.2016.
 */
@IgnoreExtraProperties
public class Contribution {
    public String author;
    public String uid;
    public String text;
    public int color;
    public Map<String, Boolean> upvotes = new HashMap<>();
    public int upvoteCount = 0;


    public Contribution() {
        color = Color.BLACK;
    }

    public Contribution(String author, String uid, String text, int color) {
        this.author = author;
        this.uid = uid;
        this.text = text;

        this.color = color;
    }


}
