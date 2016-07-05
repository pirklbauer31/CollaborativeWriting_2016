package fh.mc.collaborativewriting.models;

import android.graphics.Color;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Mark on 23.05.2016.
 */
@IgnoreExtraProperties
public class User {


    public String username;
    public String email;
    public String firstname;
    public String lastname;
    public String profilePic;

    public int userColor = Color.BLUE;

    public User(String username, String email, String firstname, String lastname, int userColor) {
        this(username, email, firstname, lastname, "gs://project-cow.appspot.com/testProfile.png", userColor);
    }

    public User(String username, String email, String firstname, String lastname, String profilePic, int userColor) {
        this.username = username;
        this.email= email;
        this.firstname=firstname;
        this.lastname=lastname;
        this.profilePic=profilePic;
        this.userColor = userColor;
    }



    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, int userColor) {
        this(username, email, "", "", "gs://project-cow.appspot.com/testProfile.png", userColor);
    }


}

