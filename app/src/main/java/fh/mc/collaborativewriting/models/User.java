package fh.mc.collaborativewriting.models;

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


    public User(String username, String email,String firstname, String lastname, String profilePic) {
        this.username = username;
        this.email= email;
        this.firstname=firstname;
        this.lastname=lastname;
        this.profilePic=profilePic;
    }



    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email) {
        this(username, email, "", "", "gs://project-cow.appspot.com/testProfile.png");
    }


}

