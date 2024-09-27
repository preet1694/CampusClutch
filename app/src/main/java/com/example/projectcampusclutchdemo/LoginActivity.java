package com.example.projectcampusclutchdemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private FirebaseAuth auth;
    private TextView textViewRegister;
    private DatabaseReference databaseReference;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        btnLogin.setOnClickListener(v -> loginUser());
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate email and password fields
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Authentication to sign in the user
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Get user's role from Firebase Database
                            String userId = user.getUid();
                            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String role = dataSnapshot.child("role").getValue(String.class);
                                        Log.d(TAG, "User role: " + role);

                                        // Redirect user based on role
                                        if (role != null) {
                                            if (role.equals("student")) {
                                                Intent intent = new Intent(LoginActivity.this, StudentActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
                                                startActivity(intent);
                                                finish();
                                            } else if (role.equals("faculty")) {
                                                Intent intent = new Intent(LoginActivity.this, FacultyActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Log.e(TAG, "Unknown role: " + role);
                                                Toast.makeText(LoginActivity.this, "Unknown user role.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } else {
                                        Log.e(TAG, "User data does not exist in the database.");
                                        Toast.makeText(LoginActivity.this, "Error: user data not found.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                                    Toast.makeText(LoginActivity.this, "Database error occurred.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Log.e(TAG, "Login failed: " + task.getException().getMessage());
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
