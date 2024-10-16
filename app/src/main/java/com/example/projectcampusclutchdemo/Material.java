package com.example.projectcampusclutchdemo;

public class Material {
    private String title;
    private String description;
    private String fileUrl;
    private String facultyId;

    // Default constructor required for calls to DataSnapshot.getValue(Material.class)
    public Material() {
    }

    public Material(String title, String description, String fileUrl, String facultyId) {
        this.title = title;
        this.description = description;
        this.fileUrl = fileUrl;
        this.facultyId = facultyId;
    }

    // Getter and Setter methods
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }
}
