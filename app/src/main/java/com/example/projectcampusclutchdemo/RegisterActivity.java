package com.example.projectcampusclutchdemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword, etName;
    private RadioGroup radioGroup;
    private Button btnRegister;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    private static final String TAG = "RegisterActivity"; // TAG for logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etName = findViewById(R.id.etName);
        radioGroup = findViewById(R.id.radioGroupRole);
        btnRegister = findViewById(R.id.btnRegister);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String name = etName.getText().toString().trim();
        int selectedRoleId = radioGroup.getCheckedRadioButtonId();

        // Basic validation
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name) || selectedRoleId == -1) {
            Toast.makeText(RegisterActivity.this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Attempting to register user...");

        // Register user in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Save user details in Firebase Database
                            String userId = user.getUid();
                            String role = (selectedRoleId == R.id.radioStudent) ? "student" : "faculty";

                            Map<String, Object> userDetails = new HashMap<>();
                            userDetails.put("name", name);
                            userDetails.put("email", email);
                            userDetails.put("role", role);

                            // Store user details in Realtime Database
                            databaseReference.child(userId).setValue(userDetails).addOnCompleteListener(dbTask -> {
                                if (dbTask.isSuccessful()) {
                                    Log.d(TAG, "User registration successful, storing details...");
                                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                                    // Redirect to LoginActivity
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the back stack
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Log.e(TAG, "Failed to store user details: " + dbTask.getException().getMessage());
                                    Toast.makeText(RegisterActivity.this, "Failed to store user details.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.e(TAG, "User registration failed, FirebaseUser is null");
                            Toast.makeText(RegisterActivity.this, "Registration failed, please try again.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Registration failed: " + task.getException().getMessage());
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
