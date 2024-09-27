package com.example.projectcampusclutchdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UploadAssignmentActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private Button btnSelectAssignmentFile, btnUploadAssignment;
    private TextView tvAssignmentFileName;
    private EditText etAssignmentTitle, etAssignmentDescription;
    private Uri selectedFileUri;
    private ProgressDialog progressDialog;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_assignment);

        btnSelectAssignmentFile = findViewById(R.id.btnSelectAssignmentFile);
        btnUploadAssignment = findViewById(R.id.btnUploadAssignment);
        tvAssignmentFileName = findViewById(R.id.tvAssignmentFileName);
        etAssignmentTitle = findViewById(R.id.etAssignmentTitle);
        etAssignmentDescription = findViewById(R.id.etAssignmentDescription);

        // Initialize Firebase
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("assignments");

        // Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");

        btnSelectAssignmentFile.setOnClickListener(v -> selectFile());

        btnUploadAssignment.setOnClickListener(v -> {
            if (selectedFileUri != null) {
                uploadFile();
            } else {
                Toast.makeText(UploadAssignmentActivity.this, "Please select a file first.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Select file from device storage
    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    // Upload the selected file to Firebase Storage
    private void uploadFile() {
        if (selectedFileUri != null) {
            progressDialog.show();

            // Generate a unique file name using UUID
            String fileName = UUID.randomUUID().toString();

            // Reference to Firebase Storage location
            StorageReference fileReference = storageReference.child("assignments/" + fileName);

            // Upload the file to Firebase Storage
            Toast.makeText(UploadAssignmentActivity.this, "Uploading", Toast.LENGTH_SHORT).show();
            fileReference.putFile(selectedFileUri).addOnSuccessListener(taskSnapshot -> {
                // Get the file's download URL after upload succeeds
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String fileUrl = uri.toString();
                    // Call the method to save the file URL and metadata to the database
                    saveAssignmentData(fileUrl);
                    progressDialog.dismiss();
                    Toast.makeText(UploadAssignmentActivity.this, "File uploaded successfully!", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(UploadAssignmentActivity.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(UploadAssignmentActivity.this, "File upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(UploadAssignmentActivity.this, "No file selected.", Toast.LENGTH_SHORT).show();
        }
    }


    // Save the assignment data in Firebase Realtime Database
    private void saveAssignmentData(String fileUrl) {
        String assignmentId = databaseReference.push().getKey(); // Generate a unique assignment ID
        String studentId = "studentId_1"; // Replace with the actual student ID (from authentication or intent)

        Map<String, Object> assignmentData = new HashMap<>();
        assignmentData.put("title", etAssignmentTitle.getText().toString());
        assignmentData.put("description", etAssignmentDescription.getText().toString());
        assignmentData.put("fileUrl", fileUrl);
        assignmentData.put("timestamp", System.currentTimeMillis());

        // Save the assignment data under the student's ID
        databaseReference.child(studentId).child(assignmentId).setValue(assignmentData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(UploadAssignmentActivity.this, "Assignment uploaded successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(UploadAssignmentActivity.this, "Failed to upload assignment data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedFileUri = data.getData();
            String fileName = selectedFileUri.getLastPathSegment();
            tvAssignmentFileName.setText(fileName);
        }
    }
}
