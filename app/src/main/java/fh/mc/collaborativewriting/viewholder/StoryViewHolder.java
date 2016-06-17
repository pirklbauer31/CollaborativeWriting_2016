package fh.mc.collaborativewriting.viewholder;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import fh.mc.collaborativewriting.R;
import fh.mc.collaborativewriting.models.Story;

/**
 * Created by Mark on 11.06.2016.
 */
public class StoryViewHolder extends RecyclerView.ViewHolder {

    public TextView titleView;
    public TextView descriptionView;
    public TextView authorView;
    public TextView numberOfStarsView;
    public ImageView starView;
    public ImageView profileView;

    public StoryViewHolder(View itemView) {
        super(itemView);

        titleView = (TextView) itemView.findViewById(R.id.story_title);
        descriptionView = (TextView) itemView.findViewById(R.id.story_description);
        authorView = (TextView) itemView.findViewById(R.id.story_author);
        numberOfStarsView = (TextView) itemView.findViewById(R.id.story_num_stars);
        starView = (ImageView) itemView.findViewById(R.id.star);
        profileView = (ImageView) itemView.findViewById(R.id.story_author_profile_pic);

    }

    public void bindToStory(Story story, View.OnClickListener starClickListener) {
        titleView.setText(story.title);
        authorView.setText(story.author);
        numberOfStarsView.setText(String.valueOf(story.starCount));
        descriptionView.setText(story.body);

        starView.setOnClickListener(starClickListener);




        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        DatabaseReference myRef = mDatabase.getReference("users");
        Query searchForUserPic = myRef.child(story.uid).child("profilePic");
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
