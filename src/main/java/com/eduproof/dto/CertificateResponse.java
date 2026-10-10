package com.eduproof.dto;

import com.eduproof.model.Certificate;

import java.time.LocalDate;

public class CertificateResponse {
    private Long certificateId;
    private String uniqueCertificateCode;
    private Long studentId;
    private String studentName;
    private String studentRollNumber;
    private String departmentName;
    private Long certificateTypeId;
    private String certificateTypeName;
    private String title;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Double cgpa;
    private String academicRemarks;
    private String status;
    private String revokeReason;
    private String qrCodeBase64;

    public static CertificateResponse from(Certificate cert) {
        CertificateResponse r = new CertificateResponse();
        r.certificateId = cert.getCertificateId();
        r.uniqueCertificateCode = cert.getUniqueCertificateCode();
        if (cert.getStudent() != null) {
            r.studentId = cert.getStudent().getStudentId();
            r.studentName = cert.getStudent().getName();
            r.studentRollNumber = cert.getStudent().getRollNumber();
            if (cert.getStudent().getDepartment() != null) {
                r.departmentName = cert.getStudent().getDepartment().getDepartmentName();
            }
        }
        if (cert.getCertificateType() != null) {
            r.certificateTypeId = cert.getCertificateType().getCertificateTypeId();
            r.certificateTypeName = cert.getCertificateType().getTypeName();
        }
        r.title = cert.getTitle();
        r.issueDate = cert.getIssueDate();
        r.expiryDate = cert.getExpiryDate();
        r.cgpa = cert.getCgpa();
        r.academicRemarks = cert.getAcademicRemarks();
        r.status = cert.getStatus();
        r.revokeReason = cert.getRevokeReason();
        r.qrCodeBase64 = cert.getQrCodeBase64();
        return r;
    }

    public Long getCertificateId() { return certificateId; }
    public String getUniqueCertificateCode() { return uniqueCertificateCode; }
    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getStudentRollNumber() { return studentRollNumber; }
    public String getDepartmentName() { return departmentName; }
    public Long getCertificateTypeId() { return certificateTypeId; }
    public String getCertificateTypeName() { return certificateTypeName; }
    public String getTitle() { return title; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public Double getCgpa() { return cgpa; }
    public String getAcademicRemarks() { return academicRemarks; }
    public String getStatus() { return status; }
    public String getRevokeReason() { return revokeReason; }
    public String getQrCodeBase64() { return qrCodeBase64; }
}
