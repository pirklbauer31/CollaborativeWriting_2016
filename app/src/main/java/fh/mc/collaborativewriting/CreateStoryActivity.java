package fh.mc.collaborativewriting;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import fh.mc.collaborativewriting.models.Story;
import fh.mc.collaborativewriting.models.User;

public class CreateStoryActivity extends BaseActivity {


    private static final String TAG = "CreateStoryActivity";

    //Database Reference
    private DatabaseReference mDataBase;


    //GUI
    private EditText mTitle;
    private EditText mDescription;
    private EditText mTags;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_story);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);


        //Setting up privacy input spinner
        Spinner spinner = (Spinner) findViewById(R.id.inputPrivacy);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.privacySpinner, R.layout.spinner_item_privacy);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_privacy);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


        mTags = (EditText) findViewById(R.id.inputTags);
        mTitle = (EditText) findViewById(R.id.inputTitle);
        mDescription = (EditText) findViewById(R.id.inputDescription);


        //  initialize_database_ref
        mDataBase = FirebaseDatabase.getInstance().getReference();
        Button mEmailRegisterButton = (Button) findViewById(R.id.inputCreate);

        mEmailRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createStory();
            }
        });

    }

    private void createStory() {
        final String tags = mTags.getText().toString();
        final String title = mTitle.getText().toString();
        final String description = mDescription.getText().toString();


        // [START single_value_read]
        final String userId = getUid();
        mDataBase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(CreateStoryActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            writeNewStory(userId, user.username, title, description, tags);
                        }

                        // Finish this Activity, back to the stream
                        finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
        // [END single_value_read]

    }

    private void writeNewStory(String userId, String username, String title, String description, String tags) {
        //Create Story at /user-stories/$userid/$storyid and /stories/$storyid
        String key = mDataBase.child("posts").push().getKey();

        //Split tags and store into a list
        ArrayList<String> tagList = new ArrayList<String>(Arrays.asList(tags.split("\\s*;\\s*")));


        Story story = new Story(userId, username, title, description, tagList);
        Map<String, Object> storyValues = story.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/stories/" + key, storyValues);
        childUpdates.put("/user-stories/" + userId + "/" + key, storyValues);

        mDataBase.updateChildren(childUpdates);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.storycreation_menu, menu);

        return true;
    }
}
