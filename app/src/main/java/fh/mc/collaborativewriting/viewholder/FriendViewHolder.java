package fh.mc.collaborativewriting.viewholder;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import fh.mc.collaborativewriting.R;
import fh.mc.collaborativewriting.models.Friend;

/**
 * Created by Kevin on 18.06.2016.
 */
public class FriendViewHolder extends RecyclerView.ViewHolder {

    public TextView usernameView;
    public ImageView profileView;
    public ImageButton acceptFriendView;
    public ImageButton removeFriendView;

    public FriendViewHolder (View itemView) {
        super(itemView);

        usernameView = (TextView) itemView.findViewById(R.id.story_author);
        profileView = (ImageView) itemView.findViewById(R.id.story_author_profile_pic);
        acceptFriendView = (ImageButton) itemView.findViewById(R.id.accept_friend);
        removeFriendView = (ImageButton) itemView.findViewById(R.id.remove_friend);

    }

    public void bindToFriend (Friend friend,View.OnClickListener acceptClickListener,
                              View.OnClickListener removeFriendOnClickListener) {
        usernameView.setText(friend.username);

        if (friend.acceptedFriend)
            acceptFriendView.setVisibility(View.INVISIBLE);
        else
        {
            acceptFriendView.setVisibility(View.VISIBLE);
            acceptFriendView.setOnClickListener(acceptClickListener);
        }

        removeFriendView.setOnClickListener(removeFriendOnClickListener);

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        DatabaseReference myRef = mDatabase.getReference("users");
        Query searchForUserPic = myRef.child(friend.userId).child("profilePic");

        searchForUserPic.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StorageReference profileReference = FirebaseStorage.getInstance().getReferenceFromUrl(String.valueOf(dataSnapshot.getValue()));



                final long ONE_MEGABYTE = 1024 * 1024;
                profileReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Data for "testprofile.png" is returned
                        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        profileView.setImageBitmap(bm);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
