package com.seg2105.termprojectgroup21.Objects;

import java.util.List;
import java.util.ArrayList;

public class Course {
    private String id, name, code, description, instructor_username;
    private int capacity, enrolledStudents;

    public Course (String id, String name, String code, String instructor_username, int capacity, String description) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.instructor_username = instructor_username;
        this.capacity = capacity;
        this.description = description;
        this.enrolledStudents = 0;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getInstructor() { return instructor_username; }

    public String getDescription() { return description; }

    public boolean enrollSomeStudent() {
        if (enrolledStudents < capacity) {
            enrolledStudents++;
            return true;
        }

        return false;
    }

    public boolean unenrollSomeStudent() {
        if (enrolledStudents > 0) {
            enrolledStudents--;
            return true;
        }

        return false;
    }

    public int getEnrolledStudents() {
        return enrolledStudents;
    }

    public int getCapacity() { return capacity; }

    public void setCapacity(int capacity) { this.capacity = capacity; }

    public void setDescription(String description) { this.description = description; }

}
