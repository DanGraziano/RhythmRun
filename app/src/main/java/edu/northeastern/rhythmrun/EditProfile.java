package edu.northeastern.rhythmrun;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfile extends AppCompatActivity {

    FirebaseUser currentUser;
    private Button logout, saveProfileBtn;

    private Spinner spinner;
    private TextView userName,textEmail,textFirstName,textLastName,textAge,textWeight,
            textHeight;
    private BottomNavigationView bottomNavigationView;

    FloatingActionButton fabButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //views
        userName = findViewById(R.id.userName);
        logout = findViewById(R.id.logoutBtn);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fabButton = findViewById(R.id.startWorkoutFab);
        saveProfileBtn = findViewById(R.id.saveProfileBtn);
        textEmail = findViewById(R.id.textEmail);
        textFirstName = findViewById(R.id.textFirstName);
        textLastName = findViewById(R.id.textLastName);
        textAge = findViewById(R.id.textAge);
        textWeight = findViewById(R.id.textWeight);
        textHeight = findViewById(R.id.textHeight);
        spinner = findViewById(R.id.dropdown);




        // logged in user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // User is signed in
            String uid = currentUser.getUid(); // Get the user's unique ID
            String displayName = currentUser.getDisplayName(); // Get the user's display name
            userName.setText(displayName);
            // make save button handler
            saveProfileBtn.setOnClickListener(v->{
                updateUsersInformation(currentUser);
                startActivity(new Intent(EditProfile.this,Settings.class));
            });


        }

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(EditProfile.this, Login.class));
        });


        fabButton.setOnClickListener(v -> {startActivity(new Intent(EditProfile.this, StartWorkout.class));
        });


        setupNavBar();
    }

    private void setupNavBar(){
        bottomNavigationView.setOnItemSelectedListener(
                (item) -> {
                    // Open Home screen on click
                    if (item.getItemId() == R.id.homeNavBar) {
                        Intent homeIntent = new Intent(EditProfile.this, Home.class);
                        startActivity(homeIntent);
                    }
					// Open Setting screen on click
					else if (item.getItemId() == R.id.settingsNavBar) {
						Intent settingsIntent = new Intent(EditProfile.this, Settings.class);
						startActivity(settingsIntent);
					}
                    return false;
                }
        );
        // KEEP -- Used for bottom navigation bar appearance and functionality
        bottomNavigationView.setBackground(null); // Set background to null to make it transparent
        bottomNavigationView.getMenu().getItem(1).setEnabled(false); // Disable the second menu item
    }

    private void updateUsersInformation(FirebaseUser user) {
        String userId = user.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        String email = textEmail.getText().toString();
        String age = textAge.getText().toString();
        String firstName = textFirstName.getText().toString();
        String lastName = textLastName.getText().toString();
        String height = textHeight.getText().toString();
        String weight = textWeight.getText().toString();
        String goal = (String) spinner.getSelectedItem();


        if (!TextUtils.isEmpty(age)) {
            reference.child(userId).child("age").setValue(age);
            Log.d("AGE", age);
        }
        if (!TextUtils.isEmpty(goal)) {
            reference.child(userId).child("cadenceGoal").setValue(goal);
            Log.d("GOAL", goal);
        }
        if (!TextUtils.isEmpty(email)) {
            reference.child(userId).child("email").setValue(email);
            Log.d("EMAIL", email);
        }
        if (!TextUtils.isEmpty(firstName)) {
            reference.child(userId).child("firstname").setValue(firstName);
            Log.d("FIRSTNAME", firstName);
        }
        if (!TextUtils.isEmpty(lastName)) {
            reference.child(userId).child("lastname").setValue(lastName);
            Log.d("LASTNAME", lastName);
        }
        if (!TextUtils.isEmpty(height)) {
            reference.child(userId).child("height").setValue(height);
            Log.d("HEIGHT", height);
        }
        if (!TextUtils.isEmpty(weight)) {
            reference.child(userId).child("weight").setValue(weight);
            Log.d("WEIGHT", weight);
        }


    }


}