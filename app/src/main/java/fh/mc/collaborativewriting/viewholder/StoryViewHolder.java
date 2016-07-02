package fh.mc.collaborativewriting.viewholder;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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

import org.w3c.dom.Text;

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

    public TextView privacyView;

    public StoryViewHolder(View itemView) {
        super(itemView);

        titleView = (TextView) itemView.findViewById(R.id.story_title);
        descriptionView = (TextView) itemView.findViewById(R.id.story_description);
        authorView = (TextView) itemView.findViewById(R.id.story_author);
        numberOfStarsView = (TextView) itemView.findViewById(R.id.story_num_stars);
        starView = (ImageView) itemView.findViewById(R.id.star);
        profileView = (ImageView) itemView.findViewById(R.id.story_author_profile_pic);

        privacyView = (TextView) itemView.findViewById(R.id.story_privacy);

    }

    public void bindToStory(Story story, View.OnClickListener starClickListener) {
        titleView.setText(story.title);
        authorView.setText(story.author);
        numberOfStarsView.setText(String.valueOf(story.starCount));
        descriptionView.setText(story.body);

        if(story.friendsOnly == true)
            privacyView.setText("Friends only");
        else
            privacyView.setText("Public");

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
                        profileView.setImageBitmap(getCroppedBitmap(bm, 150));

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


    private static Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
        Bitmap sbmp;
        if (bmp.getWidth() != radius || bmp.getHeight() != radius)
            sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
        else
            sbmp = bmp;
        Bitmap output = Bitmap.createBitmap(sbmp.getWidth(),
                sbmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xffa19774;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(sbmp.getWidth() / 2 + 0.7f, sbmp.getHeight() / 2 + 0.7f,
                sbmp.getWidth() / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);


        return output;
    }
}
