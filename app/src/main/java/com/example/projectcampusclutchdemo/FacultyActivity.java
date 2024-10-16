package com.example.projectcampusclutchdemo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FacultyActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private Uri selectedFileUri;
    private EditText etAssignmentTitle, etAssignmentDescription;
    private TextView tvDeadline;
    private Button btnUploadMaterial, btnSelectFile, btnSetDeadline;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private RecyclerView coursesRecyclerView;
    private List<Course> courseList;
    private CourseAdapter courseAdapter;
    private DatabaseReference coursesRef;
    private String deadline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty);

        etAssignmentTitle = findViewById(R.id.etAssignmentTitle);
        etAssignmentDescription = findViewById(R.id.etAssignmentDescription);
        tvDeadline = findViewById(R.id.tvDeadline);
        btnUploadMaterial = findViewById(R.id.btnUploadMaterial);
        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnSetDeadline = findViewById(R.id.btnSetDeadline);
        coursesRecyclerView = findViewById(R.id.coursesRecyclerView);
        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("materials");
        storageReference = FirebaseStorage.getInstance().getReference("materials");

        btnSelectFile.setOnClickListener(v -> openFileChooser());

        btnSetDeadline.setOnClickListener(v -> openDatePicker());

        btnUploadMaterial.setOnClickListener(v -> {
            if (validateInput()) {
                uploadMaterial();
            }
        });
        courseList = new ArrayList<>();
        courseAdapter = new CourseAdapter(courseList);
        coursesRecyclerView.setAdapter(courseAdapter);

        // Reference to 'courses' in Firebase
        coursesRef = FirebaseDatabase.getInstance().getReference("courses");

        fetchCourses();
        findViewById(R.id.btnAddCourse).setOnClickListener(view -> openAddCourseDialog());
        findViewById(R.id.btnEditCourse).setOnClickListener(view -> openEditCourseDialog());
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(etAssignmentTitle.getText().toString().trim())) {
            Toast.makeText(this, "Please enter assignment title.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file to upload.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(deadline)) {
            Toast.makeText(this, "Please set a deadline.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    private void openDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            deadline = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            tvDeadline.setText(deadline);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void uploadMaterial() {
        String fileName = UUID.randomUUID().toString();
        StorageReference fileReference = storageReference.child(fileName);

        fileReference.putFile(selectedFileUri).addOnSuccessListener(taskSnapshot -> {
            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String fileUrl = uri.toString();
                saveMaterialData(fileUrl); // Save metadata to Firebase Database
            }).addOnFailureListener(e -> Toast.makeText(FacultyActivity.this, "Failed to get file URL.", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(FacultyActivity.this, "File upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveMaterialData(String fileUrl) {
        String assignmentTitle = etAssignmentTitle.getText().toString().trim();
        String assignmentDescription = etAssignmentDescription.getText().toString().trim();

        String uploadId = databaseReference.push().getKey();
        Map<String, Object> materialData = new HashMap<>();
        materialData.put("title", assignmentTitle);
        materialData.put("description", assignmentDescription);
        materialData.put("fileUrl", fileUrl);
        materialData.put("deadline", deadline);
        materialData.put("uploadedBy", auth.getCurrentUser().getUid());

        assert uploadId != null;
        databaseReference.child(uploadId).setValue(materialData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(FacultyActivity.this, "Material uploaded successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(FacultyActivity.this, "Failed to upload material.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedFileUri = data.getData();
        }
    }
    private void fetchCourses() {
        coursesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseList.clear();
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    Course course = courseSnapshot.getValue(Course.class);
                    if (course != null) {
                        courseList.add(course);
                    }
                }
                courseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FacultyActivity.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openAddCourseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_course, null);
        builder.setView(dialogView);

        final EditText etCourseName = dialogView.findViewById(R.id.etCourseName);
        final EditText etCourseDescription = dialogView.findViewById(R.id.etCourseDescription);

        builder.setTitle("Add Course")
                .setPositiveButton("Save", (dialogInterface, i) -> {
                    String courseName = etCourseName.getText().toString().trim();
                    String courseDescription = etCourseDescription.getText().toString().trim();
                    if (!TextUtils.isEmpty(courseName) && !TextUtils.isEmpty(courseDescription)) {
                        saveCourse(new Course(courseName, courseDescription));
                    } else {
                        Toast.makeText(FacultyActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openEditCourseDialog() {
        // Assuming we're editing the first course (for demonstration), this can be improved with course selection logic
        if (courseList.isEmpty()) {
            Toast.makeText(this, "No courses available to edit", Toast.LENGTH_SHORT).show();
            return;
        }

        Course courseToEdit = courseList.get(0);  // Replace with course selection logic if needed

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_course, null);
        builder.setView(dialogView);

        final EditText etCourseName = dialogView.findViewById(R.id.etCourseName);
        final EditText etCourseDescription = dialogView.findViewById(R.id.etCourseDescription);

        // Pre-fill the fields with existing data
        etCourseName.setText(courseToEdit.getCourseName());
        etCourseDescription.setText(courseToEdit.getCourseDescription());

        builder.setTitle("Edit Course")
                .setPositiveButton("Save", (dialogInterface, i) -> {
                    String updatedName = etCourseName.getText().toString().trim();
                    String updatedDescription = etCourseDescription.getText().toString().trim();

                    if (!TextUtils.isEmpty(updatedName) && !TextUtils.isEmpty(updatedDescription)) {
                        courseToEdit.setCourseName(updatedName);
                        courseToEdit.setCourseDescription(updatedDescription);
                        updateCourse(courseToEdit);
                    } else {
                        Toast.makeText(FacultyActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveCourse(Course course) {
        String courseId = coursesRef.push().getKey();
        if (courseId != null) {
            coursesRef.child(courseId).setValue(course);
            Toast.makeText(this, "Course added", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCourse(Course course) {
        // Assuming we're updating the course's key
        DatabaseReference courseRef = coursesRef.child(course.getId());
        courseRef.setValue(course);
        Toast.makeText(this, "Course updated", Toast.LENGTH_SHORT).show();
    }
}