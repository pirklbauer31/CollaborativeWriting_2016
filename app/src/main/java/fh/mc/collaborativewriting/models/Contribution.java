package fh.mc.collaborativewriting.models;

import android.graphics.Color;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Mark on 13.06.2016.
 */
//TODO: Find better name LOL
@IgnoreExtraProperties
public class Contribution {
    public String author;
    public String uid;
    public String text;
    //TODO: maybe useful for moderation?
    public int color;

    public Contribution() {
        color = Color.BLACK;
    }

    public Contribution(String author, String uid, String text) {
        this.author = author;
        this.uid = uid;
        this.text = text;

        color = Color.BLACK;
    }
}
