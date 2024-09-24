package com.example.projectcampusclutchdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StudentActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivProfilePic;
    private EditText etName, etEmail;
    private Button btnUpdateProfile, btnUploadAssignment, btnViewCourses;
    private Uri profilePicUri;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        ivProfilePic = findViewById(R.id.ivProfilePic);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnUploadAssignment = findViewById(R.id.btnUploadAssignment);
        btnViewCourses = findViewById(R.id.btnViewCourses);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference("profile_pics");



        ivProfilePic.setOnClickListener(v -> openImageChooser());
        btnUpdateProfile.setOnClickListener(v -> updateProfile());
        btnUploadAssignment.setOnClickListener(v -> startActivity(new Intent(StudentActivity.this, UploadAssignmentActivity.class)));
        btnViewCourses.setOnClickListener(v -> startActivity(new Intent(StudentActivity.this, CoursesActivity.class)));
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            Toast.makeText(StudentActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("email", email);

            if (profilePicUri != null) {
                uploadProfilePic(userId);
            } else {
                databaseReference.child(userId).updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(StudentActivity.this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(StudentActivity.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void uploadProfilePic(String userId) {
        if (profilePicUri != null) {
            StorageReference profilePicRef = storageReference.child(userId + ".jpg");
            profilePicRef.putFile(profilePicUri).addOnSuccessListener(taskSnapshot -> {
                profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String profilePicUrl = uri.toString();

                    // Update user profile with new profile picture URL
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("profilePicUrl", profilePicUrl);

                    databaseReference.child(userId).updateChildren(updates).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(StudentActivity.this, "Profile picture updated successfully.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(StudentActivity.this, "Failed to update profile picture.", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(StudentActivity.this, "Failed to upload profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            profilePicUri = data.getData();
            ivProfilePic.setImageURI(profilePicUri);
        }
    }
}
