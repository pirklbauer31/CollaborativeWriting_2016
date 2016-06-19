package fh.mc.collaborativewriting.models;

/**
 * Created by Kevin on 18.06.2016.
 */
public class Friend {

    public String username;
    public String profilePic;
    public boolean acceptedFriend;
    public String userId;

    public Friend (){

    }

    public Friend(String username, String profilePic, boolean acceptedFriend, String userId)
    {
        this.userId = userId;
        this.username = username;
        this.profilePic = profilePic;
        this.acceptedFriend = acceptedFriend;
    }
}
