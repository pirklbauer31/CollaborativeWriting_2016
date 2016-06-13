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


    public User(String username, String email,String firstname, String lastname) {
        this.username = username;
        this.email= email;
        this.firstname=firstname;
        this.lastname=lastname;
    }



    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email) {
        this(username, email, "", "");
    }


}

