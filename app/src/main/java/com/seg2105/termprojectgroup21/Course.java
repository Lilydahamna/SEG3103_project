package com.seg2105.termprojectgroup21;

public class Course {
    String id;
    String name;
    String code;

    public Course (String id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
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

}
