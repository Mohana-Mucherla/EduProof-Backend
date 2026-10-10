package com.eduproof.dto;

import com.eduproof.model.Student;

public class StudentResponse {
    private Long studentId;
    private String rollNumber;
    private String name;
    private String email;
    private String phone;
    private Long departmentId;
    private String departmentName;
    private String course;
    private String year;
    private Integer admissionYear;
    private Integer graduationYear;
    private Double cgpa;
    private String status;

    public static StudentResponse from(Student student) {
        StudentResponse r = new StudentResponse();
        r.studentId = student.getStudentId();
        r.rollNumber = student.getRollNumber();
        r.name = student.getName();
        r.email = student.getEmail();
        r.phone = student.getPhone();
        if (student.getDepartment() != null) {
            r.departmentId = student.getDepartment().getDepartmentId();
            r.departmentName = student.getDepartment().getDepartmentName();
        }
        r.course = student.getCourse();
        r.year = student.getYear();
        r.admissionYear = student.getAdmissionYear();
        r.graduationYear = student.getGraduationYear();
        r.cgpa = student.getCgpa();
        r.status = student.getStatus();
        return r;
    }

    public Long getStudentId() { return studentId; }
    public String getRollNumber() { return rollNumber; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public String getCourse() { return course; }
    public String getYear() { return year; }
    public Integer getAdmissionYear() { return admissionYear; }
    public Integer getGraduationYear() { return graduationYear; }
    public Double getCgpa() { return cgpa; }
    public String getStatus() { return status; }
}
