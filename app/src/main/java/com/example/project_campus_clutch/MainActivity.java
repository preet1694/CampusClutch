package com.example.project_campus_clutch;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnStudents, btnCourses, btnFaculty, btnDepartments, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize buttons
        btnStudents = findViewById(R.id.button_students);
        btnCourses = findViewById(R.id.button_courses);
        btnFaculty = findViewById(R.id.button_faculty);
        btnDepartments = findViewById(R.id.button_departments);
        btnLogout = findViewById(R.id.button_logout);

        // Set click listeners for buttons
        btnStudents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Students Activity
                Intent intent = new Intent(MainActivity.this, StudentsActivity.class);
                startActivity(intent);
            }
        });

        btnCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Courses Activity
                Intent intent = new Intent(MainActivity.this, CoursesActivity.class);
                startActivity(intent);
            }
        });

        btnFaculty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Faculty Activity
                Intent intent = new Intent(MainActivity.this, FacultyActivity.class);
                startActivity(intent);
            }
        });

        btnDepartments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Departments Activity
                Intent intent = new Intent(MainActivity.this, DepartmentsActivity.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform logout action, e.g., clear session, navigate to login screen
                Toast.makeText(MainActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
                // Example: finish() to close the current activity
                finish();
            }
        });
    }
}