package fh.mc.collaborativewriting;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import fh.mc.collaborativewriting.models.User;

/**
 * SettingsActivity that lets the user choose his/her font-colot for contributions
 */
public class ToolsActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {


    //array for color-values of the selectable User Colors
    private int[] userColors;
    private int mUserColor;
    //Index of the Color in the arrays
    private int mUserColorPos = -1;

    //Firebasereferences
    DatabaseReference mUserRef;

    //UI references
    Spinner spinner;

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // update userColor in DB and save itemPos
        mUserRef.child("userColor").setValue(userColors[pos]);
        mUserColorPos = pos;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);


        userColors = getResources().getIntArray(R.array.usercolors);
        spinner = (Spinner) findViewById(R.id.spinner_user_color);
        spinner.setOnItemSelectedListener(this);


        mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(getUid());
        // Create an ArrayAdapter using the string array and a default spinner layout
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_colors, android.R.layout.simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                mUserColor = user.userColor;

                //find the index of the color in the array
                //to select it in the Spinner
                for (int i = 0; i < userColors.length; i++) {
                    if (mUserColor == userColors[i])
                        mUserColorPos = i;
                }
                if (mUserColorPos != -1) {
                    spinner.setAdapter(adapter);
                    spinner.setSelection(mUserColorPos);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
