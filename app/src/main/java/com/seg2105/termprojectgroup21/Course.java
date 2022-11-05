package com.seg2105.termprojectgroup21;

public class Course {
    String id, name, code, instructor_username;

    public Course (String id, String name, String code, String instructor_username) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.instructor_username = instructor_username;
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

}
