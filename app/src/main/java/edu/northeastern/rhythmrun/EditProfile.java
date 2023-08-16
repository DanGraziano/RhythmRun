package edu.northeastern.rhythmrun;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    FirebaseUser currentUser;
    private Button logout, saveProfileBtn;

    private Spinner spinner;
    private TextView userName,textEmail,textFirstName,textLastName,editAge,editWeight,
            editHeight;
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
        editAge = findViewById(R.id.editAge);
        editWeight = findViewById(R.id.editWeight);
        editHeight = findViewById(R.id.editHeight);
        spinner = findViewById(R.id.dropdown);


        // logged in user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // User is signed in
            String uid = currentUser.getUid(); // Get the user's unique ID
            String displayName = currentUser.getDisplayName(); // Get the user's display name
            userName.setText(displayName);
            displayUsersData(currentUser);
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

        UserProfile userProfile = new UserProfile();
        userProfile.setAge(editAge.getText().toString());
        userProfile.setWeight(editWeight.getText().toString());
        userProfile.setHeight(editHeight.getText().toString());
        userProfile.setCadenceGoal((String) spinner.getSelectedItem());

        String age = editAge.getText().toString();
        String height = editHeight.getText().toString();
        String weight = editWeight.getText().toString();
        String goal = (String) spinner.getSelectedItem();

        // Update only the changed fields
        Map<String, Object> updates = new HashMap<>();
        if (!TextUtils.isEmpty(userProfile.getAge())) {
            updates.put("age", userProfile.getAge());
            reference.child(userId).child("age").setValue(age);

        }
        if (!TextUtils.isEmpty(goal)) {
            reference.child(userId).child("cadenceGoal").setValue(goal);
            Log.d("GOAL", goal);
        }

        if (!TextUtils.isEmpty(userProfile.getWeight())) {
            updates.put("weight", userProfile.getWeight());
            reference.child(userId).child("weight").setValue(weight);
        }
        if (!TextUtils.isEmpty(userProfile.getHeight())) {
            updates.put("height", userProfile.getHeight());
            reference.child(userId).child("height").setValue(height);
        }
        if (!TextUtils.isEmpty(userProfile.getCadenceGoal())) {
            updates.put("cadenceGoal", userProfile.getCadenceGoal());
            reference.child(userId).child("cadenceGoal").setValue(goal);
        }
    }

    private void displayUsersData(FirebaseUser user) {

        String userId = user.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("Inside run snapshot", String.valueOf(snapshot));
                if (snapshot.exists()) {
                    String email = String.valueOf(snapshot.child("email").getValue());
                    String firstName = String.valueOf(snapshot.child("firstname").getValue());
                    String lastName = String.valueOf(snapshot.child("lastname").getValue());

                    // Set retrieved data to TextViews
                    textEmail.setText(email);
                    textFirstName.setText(firstName);
                    textLastName.setText(lastName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("EditProfile", "Error fetching user data", error.toException());
            }
        });
    }

}